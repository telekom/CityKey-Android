package com.telekom.citykey.view.services.appointments.details

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.CalendarContract
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.content.FileProvider
import androidx.core.text.HtmlCompat
import androidx.core.view.forEach
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.telekom.citykey.BuildConfig
import com.telekom.citykey.DetailedMapGraphArgs
import com.telekom.citykey.R
import com.telekom.citykey.databinding.AppointmentsDetailsFragmentBinding
import com.telekom.citykey.domain.city.CityInteractor
import com.telekom.citykey.models.appointments.Appointment
import com.telekom.citykey.models.appointments.Location
import com.telekom.citykey.utils.BitmapUtil
import com.telekom.citykey.utils.DialogUtil
import com.telekom.citykey.utils.extensions.AccessibilityRole
import com.telekom.citykey.utils.extensions.disable
import com.telekom.citykey.utils.extensions.getColor
import com.telekom.citykey.utils.extensions.getHoursAndMins
import com.telekom.citykey.utils.extensions.getLongWeekDay
import com.telekom.citykey.utils.extensions.getShortMonthName
import com.telekom.citykey.utils.extensions.getShortWeekDay
import com.telekom.citykey.utils.extensions.isInPast
import com.telekom.citykey.utils.extensions.loadFromOSCA
import com.telekom.citykey.utils.extensions.openMapApp
import com.telekom.citykey.utils.extensions.setAccessibilityRole
import com.telekom.citykey.utils.extensions.setVisible
import com.telekom.citykey.utils.extensions.showInfoSnackBar
import com.telekom.citykey.utils.extensions.toCalendar
import com.telekom.citykey.utils.extensions.toDateString
import com.telekom.citykey.utils.extensions.viewBinding
import com.telekom.citykey.utils.isDarkMode
import com.telekom.citykey.utils.tryLoadingNightStyle
import com.telekom.citykey.view.MainFragment
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Calendar

class AppointmentDetails :
    MainFragment(R.layout.appointments_details_fragment),
    OnMapReadyCallback {
    private val args: AppointmentDetailsArgs by navArgs()
    private val viewModel: AppointmentDetailsViewModel by viewModel()
    private val binding by viewBinding(AppointmentsDetailsFragmentBinding::bind)

    private var googleMap: GoogleMap? = null
    private var menuRes = R.menu.share_menu

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.onViewCreated(args.appointment)
        setupToolbar(binding.toolbarAppointmentsDetail)
        setupAddresses()
        setupDates()
        setupInfoBlock()
        setAccessibilityRoles()

        binding.appointmentName.text = args.appointment.title
        findNavController().previousBackStackEntry?.savedStateHandle?.set("DeletionSuccessful", false)
        binding.cancelButton.setupOutlineStyle(CityInteractor.cityColorInt)

        if (args.appointment.apptStatus == Appointment.STATE_CANCELED || args.appointment.apptStatus == Appointment.STATE_REJECTED || args.appointment.endTime.isInPast) {
            binding.addToCalendarBtn.disable()
            binding.showQrCodeBtn.disable()
            menuRes = R.menu.infobox_detailed_menu
        }
        if (args.appointment.apptStatus == Appointment.STATE_PENDING && !args.appointment.endTime.isInPast) {
            binding.showQrCodeBtn.disable()
        }
        if (args.appointment.isCancellable) {
            binding.cancelButtonContainer.setVisible(true)
        }
        initListeners()
        subscribeUi()
    }

    private fun initListeners() {
        binding.addToCalendarBtn.setOnClickListener {
            try {
                startActivity(createCalendarIntent(args.appointment))
            } catch (ex: ActivityNotFoundException) {
                DialogUtil.showInfoDialog(
                    requireContext(),
                    R.string.e_005_dialog_no_calendar_title,
                    R.string.e_005_dialog_no_calendar_message
                )
            }
        }

        binding.showQrCodeBtn.setOnClickListener {
            findNavController()
                .navigate(
                    AppointmentDetailsDirections.actionAppointmentDetailsToAppointmentQR(
                        args.appointment.waitingNo,
                        args.appointment.uuid
                    )
                )
        }
    }

    private fun subscribeUi() {
        viewModel.image.observe(viewLifecycleOwner, binding.image::loadFromOSCA)

        viewModel.deletionSuccessful.observe(viewLifecycleOwner) {
            findNavController().previousBackStackEntry?.savedStateHandle?.set("DeletionSuccessful", true)
            findNavController().navigateUp()
        }

        viewModel.qrBitmap.observe(viewLifecycleOwner) {
            val cachePath = File(requireContext().filesDir, "images")

            try {
                cachePath.mkdirs()
                val stream = FileOutputStream("$cachePath/QRCode_${args.appointment.uuid}.png")
                it.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                stream.close()
            } catch (e: IOException) {
                Timber.e(e)
            }

            val file = File(cachePath, "QRCode_${args.appointment.uuid}.png")
            val contentUri: Uri? = try {
                FileProvider.getUriForFile(requireContext(), "${BuildConfig.APPLICATION_ID}.fileprovider", file)
            } catch (e: IllegalArgumentException) {
                Timber.e(e)
                null
            }

            val title = getString(R.string.apnmt_003_share_text_title, args.appointment.title)

            startActivity(
                Intent.createChooser(
                    Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, getShareText())
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        putExtra(Intent.EXTRA_SUBJECT, title)
                        putExtra(Intent.EXTRA_TITLE, title) // relevant for >= Android 10
                        contentUri?.let { uri -> putExtra(Intent.EXTRA_STREAM, uri) }
                        type = "text/plain"
                    },
                    null
                )
            )
        }

        viewModel.userLoggedOut.observe(viewLifecycleOwner) {
            findNavController().popBackStack(R.id.services, false)
        }
        viewModel.showNoInternetDialog.observe(viewLifecycleOwner) {
            DialogUtil.showNoInternetDialog(requireContext())
        }
        viewModel.showErrorSnackbar.observe(viewLifecycleOwner) {
            showInfoSnackBar(R.string.appnmt_delete_error_snackbar)
        }

        viewModel.cancelSuccessful.observe(viewLifecycleOwner) {
            binding.cancelButton.stopLoading()
            if (it) findNavController().popBackStack()
        }

        viewModel.showRetryDialog.observe(viewLifecycleOwner) {
            DialogUtil.showRetryDialog(requireContext(), viewModel::onRetryRequired) {
                binding.cancelButton.stopLoading()
                viewModel.onRetryCanceled()
            }
        }

        viewModel.cancellationAllowed.observe(viewLifecycleOwner) { isCancellationAllowed ->
            binding.cancelButton.setOnClickListener {
                if (isCancellationAllowed) {
                    MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogMaterialTheme)
                        .setTitle(R.string.apnmt_003_cancel_appointment_btn)
                        .setMessage(R.string.apnmt_003_cancel_error_apnmt_info)
                        .setNegativeButton(R.string.apnmt_003_cancel_apnmt_cancel, null)
                        .show()
                } else {
                    MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogMaterialTheme)
                        .setTitle(R.string.apnmt_003_cancel_appointment_btn)
                        .setMessage(R.string.apnmt_003_cancel_apnmt_info)
                        .setPositiveButton(R.string.apnmt_003_cancel_apnmt_ok) { _, _ ->
                            binding.cancelButton.startLoading()
                            viewModel.onCancelRequested(args.appointment.uuid)
                        }
                        .setNegativeButton(R.string.apnmt_003_cancel_apnmt_cancel, null)
                        .show()
                }
            }
        }
    }

    private fun setupInfoBlock() {
        var participantsString = ""
        var documentsString = ""
        var concernsString = ""
        var responsibleString = ""
        var additionalInfoString = ""

        args.appointment.reasons?.forEach {
            concernsString += "&#8250; ${it.sNumber}x ${it.description}<br/>"
        }

        if (args.appointment.contacts.contactDesc.isNotEmpty())
            responsibleString += "&#8250; ${args.appointment.contacts.contactDesc}<br/>"

        if (args.appointment.contacts.telefon.isNotEmpty())
            responsibleString += "&#8250; ${
                getString(
                    R.string.apnmt_003_apnmt_telefon_label,
                    args.appointment.contacts.telefon
                )
            }<br/>"

        if (args.appointment.contacts.email.isNotEmpty())
            responsibleString += "&#8250; ${
                getString(
                    R.string.apnmt_003_apnmt_email,
                    args.appointment.contacts.email
                )
            }<br/>"

        if (args.appointment.contacts.contactNotes.isNotEmpty())
            responsibleString += "&#8250; ${args.appointment.contacts.contactNotes}<br/>"

        if (args.appointment.notes.isNotEmpty())
            additionalInfoString += "&#8250; ${args.appointment.notes}<br/>"

        args.appointment.attendee?.forEach {
            if (it.firstName.isNotBlank())
                participantsString += "&#8250; ${it.firstName} ${it.lastName}<br/>"
        }

        args.appointment.documents.forEach {
            documentsString += "&#8250; $it<br/>"
        }
        binding.participantsLabel.setVisible(participantsString.isNotEmpty())
        binding.participantsList.setVisible(participantsString.isNotEmpty())

        binding.dontForgetLabel.setVisible(args.appointment.documents.isNotEmpty())
        binding.dontForgetList.setVisible(args.appointment.documents.isNotEmpty())

        binding.responsibleInstanceLabel.setVisible(responsibleString.isNotEmpty())
        binding.responsibleInstanceList.setVisible(responsibleString.isNotEmpty())

        binding.additionalInfoLabel.setVisible(args.appointment.notes.isNotEmpty())
        binding.additionalInfo.setVisible(args.appointment.notes.isNotEmpty())
        binding.waitingNumber.setVisible(args.appointment.waitingNo.isNotEmpty())

        binding.requestsList.text = HtmlCompat.fromHtml(concernsString, HtmlCompat.FROM_HTML_MODE_LEGACY)
        binding.participantsList.text = HtmlCompat.fromHtml(participantsString, HtmlCompat.FROM_HTML_MODE_LEGACY)
        binding.dontForgetList.text = HtmlCompat.fromHtml(documentsString, HtmlCompat.FROM_HTML_MODE_LEGACY)
        binding.responsibleInstanceList.text = HtmlCompat.fromHtml(responsibleString, HtmlCompat.FROM_HTML_MODE_LEGACY)
        binding.additionalInfo.text = HtmlCompat.fromHtml(additionalInfoString, HtmlCompat.FROM_HTML_MODE_LEGACY)
        binding.waitingNumber.text = getString(R.string.apnmt_003_waiting_no_format, args.appointment.waitingNo)
        binding.locationNavigation.setTextColor(CityInteractor.cityColorInt)
    }

    private fun setupDates() {
        binding.appointmentCreationInfo.text =
            getString(R.string.apnmt_003_appointment_creation_format, args.appointment.createdTime.toDateString())
        binding.appointmentCreationInfo.contentDescription = getString(
            R.string.apnmt_003_appointment_creation_format,
            args.appointment.createdTime.toDateString().replace(".", "")
        )
        val calendar = args.appointment.startTime.toCalendar()
        val date = calendar.getShortWeekDay() + ", " +
                calendar.getShortMonthName().replace(".", "")
        val dateDay = calendar.get(Calendar.DAY_OF_MONTH)

        binding.dateView.dateTextS.text = date
        binding.dateView.dateNumberS.text = dateDay.toString()

        binding.dateView.dateCardS.setCardBackgroundColor(CityInteractor.cityColorInt)
        binding.dateView.dateCardS.visibility = View.VISIBLE

        binding.dateView.dateCardS.contentDescription = calendar.getLongWeekDay() + ", " +
                calendar.getShortMonthName().replace(".", "") + dateDay

        binding.endTime.setTextColor(getColor(R.color.onSurface))
        binding.endTime.text = args.appointment.endTime.getHoursAndMins()

        binding.startTime.setTextColor(getColor(R.color.onSurface))
        binding.startTime.text = args.appointment.startTime.getHoursAndMins()
    }

    @SuppressLint("SetTextI18n")
    private fun setupAddresses() {
        val location = args.appointment.location
        binding.locationAddress.text = getAppointmentAddress(location)

        binding.placeName.text = location.addressDesc
        binding.placeAddress.text = "${location.street} ${location.houseNumber}"
        binding.placeTown.text = "${location.postalCode} ${location.place}"

        initMap()
    }

    private fun initMap() {
        binding.mapView.attachLifecycleToFragment(this@AppointmentDetails)
        binding.mapView.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        if (resources.isDarkMode) {
            map.tryLoadingNightStyle(requireContext())
        }
        googleMap = map
        viewModel.latLng.observe(viewLifecycleOwner) { location ->
            location?.let(::setupEventLocation)
        }
    }

    private fun setupEventLocation(location: LatLng) {
        googleMap?.addMarker(
            MarkerOptions().position(location)
                .icon(BitmapDescriptorFactory.fromBitmap(BitmapUtil.createMarker(requireContext())))
        )
        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 17f))
        googleMap?.uiSettings?.isScrollGesturesEnabled = false
        googleMap?.uiSettings?.isZoomGesturesEnabled = false
        googleMap?.uiSettings?.isRotateGesturesEnabled = false

        googleMap?.setOnMapClickListener {
            val appointmentLocation = args.appointment.location

            findNavController().navigate(
                R.id.action_appointmentDetails_to_detailedMapGraph,
                DetailedMapGraphArgs.Builder(
                    null,
                    args.appointment.title,
                    "${appointmentLocation.street} ${appointmentLocation.houseNumber}, ${appointmentLocation.postalCode} ${appointmentLocation.place}",
                    location
                ).build().toBundle()
            )
        }

        binding.mapView.setMapsAccessibility {
            val appointmentLocation = args.appointment.location

            findNavController().navigate(
                R.id.action_appointmentDetails_to_detailedMapGraph,
                DetailedMapGraphArgs.Builder(
                    null,
                    args.appointment.title,
                    "${appointmentLocation.street} ${appointmentLocation.houseNumber}, ${appointmentLocation.postalCode} ${appointmentLocation.place}",
                    location
                ).build().toBundle()
            )
        }

        binding.locationNavigation.setOnClickListener {
            openMapApp(location.latitude, location.longitude)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(menuRes, menu)
        menu.forEach {
            val spannableTitle = SpannableString(it.title)
            spannableTitle.setSpan(ForegroundColorSpan(CityInteractor.cityColorInt), 0, spannableTitle.length, 0)
            it.title = spannableTitle
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun createCalendarIntent(appointment: Appointment) = Intent(Intent.ACTION_INSERT)
        .setData(CalendarContract.Events.CONTENT_URI)
        .putExtra(CalendarContract.Events.TITLE, appointment.title)
        .putExtra(CalendarContract.Events.DESCRIPTION, getAppointmentDescription(args.appointment))
        .putExtra(CalendarContract.Events.EVENT_LOCATION, getAppointmentAddress(args.appointment.location))
        .putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY)
        .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, appointment.startTime.time)
        .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, appointment.endTime.time)

    private fun getAppointmentAddress(location: Location) =
        "${location.street} ${location.houseNumber}, ${location.postalCode} ${location.place}"

    private fun getAppointmentDescription(appointment: Appointment) = StringBuilder().apply {
        append(getString(R.string.apnmt_003_cal_export_location_format, appointment.contacts.contactDesc))
        append(System.lineSeparator())
        append(System.lineSeparator())
        append(binding.concernsLabel.text)
        append(System.lineSeparator())
        append(binding.requestsList.text)
        if (binding.participantsLabel.isVisible) {
            append(System.lineSeparator())
            append(binding.participantsLabel.text)
            append(System.lineSeparator())
            append(binding.participantsList.text)
        }
        if (binding.dontForgetLabel.isVisible) {
            append(System.lineSeparator())
            append(binding.dontForgetLabel.text)
            append(System.lineSeparator())
            append(binding.dontForgetList.text)
        }
    }.toString()

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.actionDelete -> {
                viewModel.onDelete(args.appointment)
                return true
            }

            R.id.actionShare -> {
                viewModel.onShareClicked(args.appointment.uuid)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun getShareText(): String = getString(
        R.string.apnmt_003_share_text_format,
        args.appointment.title,
        args.appointment.startTime.toDateString(),
        args.appointment.startTime.getHoursAndMins(),
        args.appointment.reasons
            ?.joinToString(separator = "") { "${Typography.bullet} ${it.sNumber}${Typography.times} ${it.description}${System.lineSeparator()}" }
            ?: "",
        args.appointment.waitingNo,
        getAppointmentAddress(args.appointment.location).replace(" ", "%20")
    ) + System.lineSeparator() + getString(R.string.share_store_header)

    override fun onDestroyView() {
        super.onDestroyView()
        googleMap?.clear()
        googleMap = null
    }

    private fun setAccessibilityRoles() {
        binding.appointmentName.setAccessibilityRole(
            AccessibilityRole.Heading,
            getString(R.string.accessibility_heading_level_2)
        )
        binding.addToCalendarBtn.setAccessibilityRole(AccessibilityRole.Button)
        binding.showQrCodeBtn.setAccessibilityRole(AccessibilityRole.Button)
        binding.locationNavigation.setAccessibilityRole(AccessibilityRole.Button)
        binding.mapView.setAccessibilityRole(AccessibilityRole.Link)
    }
}
