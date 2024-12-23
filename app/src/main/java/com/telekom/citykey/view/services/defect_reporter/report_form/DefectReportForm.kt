package com.telekom.citykey.view.services.defect_reporter.report_form

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.view.accessibility.AccessibilityNodeInfo
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.telekom.citykey.BuildConfig
import com.telekom.citykey.R
import com.telekom.citykey.custom.views.inputfields.FieldValidation
import com.telekom.citykey.custom.views.inputfields.OscaInputLayout
import com.telekom.citykey.databinding.DefectReportFormFragmentBinding
import com.telekom.citykey.domain.city.CityInteractor
import com.telekom.citykey.utils.*
import com.telekom.citykey.utils.extensions.*
import com.telekom.citykey.view.MainFragment
import com.telekom.citykey.view.services.defect_reporter.location_selection.DefectLocationSelection
import com.telekom.citykey.view.services.defect_reporter.report_form.defecttermsdialogs.DefectDataPrivacyNotice
import com.telekom.citykey.view.services.defect_reporter.report_form.defecttermsdialogs.DefectRulesOfUse
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File
import java.io.IOException

class DefectReportForm : MainFragment(R.layout.defect_report_form_fragment, true), OnMapReadyCallback {
    private val viewModel: DefectReportFormViewModel by viewModel()
    private val binding by viewBinding(DefectReportFormFragmentBinding::bind)
    private val args: DefectReportFormArgs by navArgs()
    private var googleMap: GoogleMap? = null
    private var defectLocation: LatLng = LatLng(0.0, 0.0)
    private var marker: Marker? = null
    private var mandatoryFields = mutableListOf<OscaInputLayout>()
    private lateinit var photoUri: Uri
    private var isImageMandatory = false
    private var cityName = ""
    private val imageFile by lazy { File.createTempFile("defect_image", ".jpg", storageDir) }
    private val storageDir by lazy { File(requireContext().filesDir, "images") }
    private var showContactDetailsLabel: Boolean = false
    private var isTermsRequired = false
    private val getCapturedContent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            val bitmap = BitmapUtil.getRequiredBitmap(it.data?.data, requireContext(), imageFile, photoUri)
            if (bitmap != null) {
                binding.photoUploadTab.visibility = View.GONE
                binding.visibleImage.visibility = View.VISIBLE
                binding.image.setImageBitmap(bitmap)
                updateButtonStatus()
            } else {
                showDefectErrorDialog(getString(R.string.dialog_technical_error_message))
            }
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {
                createIntentChooser()
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar(binding.toolbarDefectForm)
        initViews()
        initMap()
        subscribeUi()
    }

    private fun initMap() {
        binding.mapView.attachLifecycleToFragment(this@DefectReportForm)
        binding.mapView.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        if (resources.isDarkMode) {
            map.tryLoadingNightStyle(requireContext())
        }
        googleMap = map
        setupDefectLocation(args.location)
    }

    private fun setupDefectLocation(location: LatLng) {
        defectLocation = location
        marker?.remove()
        marker =
            googleMap?.addMarker(
                MarkerOptions().position(location)
                    .icon(BitmapDescriptorFactory.fromBitmap(BitmapUtil.createMarker(requireContext())))
            )
        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 17f))
        googleMap?.uiSettings?.isZoomGesturesEnabled = false
        googleMap?.uiSettings?.isScrollGesturesEnabled = false
        googleMap?.uiSettings?.isRotateGesturesEnabled = false
    }

    private fun initViews() {
        setupCheckboxes()
        setUpFields()
        updateButtonStatus()
        binding.progressSendReportBtn.setupNormalStyle(CityInteractor.cityColorInt)
        binding.toolbarDefectForm.title = args.subServiceName ?: args.serviceName
        binding.changeLocationButton.setupOutlineStyle(CityInteractor.cityColorInt)
        binding.addIcon.setColorFilter(CityInteractor.cityColorInt)
        binding.addLabel.setTextColor(CityInteractor.cityColorInt)
        binding.photoUploadTab.strokeColor = CityInteractor.cityColorInt
        binding.removeImage.setOnClickListener {
            binding.image.setImageDrawable(null)
            binding.visibleImage.visibility = View.GONE
            binding.photoUploadTab.visibility = View.VISIBLE
            updateButtonStatus()
        }

        binding.locationLabel.setAccessibilityRole(
            AccessibilityRole.Heading,
            getString(R.string.accessibility_heading_level_2)
        )
        binding.noteLabel.setAccessibilityRole(
            AccessibilityRole.Heading,
            getString(R.string.accessibility_heading_level_2)
        )
        binding.descriptionLabel.setAccessibilityRole(
            AccessibilityRole.Heading,
            getString(R.string.accessibility_heading_level_2)
        )
        binding.contactDetailsLabel.setAccessibilityRole(
            AccessibilityRole.Heading,
            getString(R.string.accessibility_heading_level_2)
        )
        binding.photoUploadTab.setAccessibilityRole(AccessibilityRole.Button)

        binding.changeLocationButton.setOnClickListener {
            DefectLocationSelection(
                defectLocation,
                locationResultListener = {
                    setupDefectLocation(it)
                }
            ).showDialog(childFragmentManager)
        }
        binding.progressSendReportBtn.setOnClickListener {
            showErrorTerms(binding.checkTerms.isChecked)
            showErrorPrivacy(binding.checkPrivacy.isChecked)
            if ((!binding.checkTerms.isChecked && isTermsRequired) || !binding.checkPrivacy.isChecked) return@setOnClickListener
            if (binding.emailAddressInput.hasErrors &&
                areMandatoryFieldsFilled()
            ) return@setOnClickListener
            binding.defectReportContainer.disable()
            binding.progressSendReportBtn.startLoading()
            val capturedImage = binding.image.drawable?.toBitmap()
            viewModel.onSendReportClicked(
                email = binding.emailAddressInput.text,
                firstName = binding.firstName.text,
                lastName = binding.lastName.text,
                wasteBinId = binding.wasteBinId.text,
                yourConcern = binding.yourConcern.text,
                serviceCode = args.serviceCode,
                subServiceCode = args.subServiceCode ?: "",
                latLng = defectLocation,
                image = capturedImage
            )
        }
        binding.checkPrivacy.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) showErrorPrivacy(isChecked)
            else {
                binding.checkPrivacy.buttonTintList = ColorStateList.valueOf(getColor(R.color.onSurface))
                binding.privacysErrorHint.setVisible(false)
                binding.privacyErrorIcon.setVisible(false)
            }
            binding.checkPrivacy.requestFocus()
        }

        binding.checkTerms.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) showErrorTerms(isChecked)
            else {
                binding.checkTerms.buttonTintList = ColorStateList.valueOf(getColor(R.color.onSurface))
                binding.termsErrorHint.setVisible(false)
                binding.termsErrorIcon.setVisible(false)
            }
            binding.checkTerms.requestFocus()
        }

        binding.photoUploadTab.setOnClickListener {
            if (arePermissionsGranted()) {
                createIntentChooser()
            } else {
                requestPermission()
            }
        }
        if (args.subServiceDescription.isNullOrBlank().not()) {
            binding.yourNoteLayout.setVisible(true)
            binding.noteDescriptionWebView.apply {
                webViewClient = pageLinkHandlerWebViewClient
                loadBasicHtml(args.subServiceDescription!!)
            }
        }
        setBehaviorListeners()
        binding.wasteBinId.setVisible(args.hasAdditionalInfo)
    }

    private val pageLinkHandlerWebViewClient by lazy {
        object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                attemptOpeningWebViewUri(request?.url)
                return true
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        if (!storageDir.exists()) {
            storageDir.mkdir()
        }
        return imageFile
    }

    private fun setUpFields() {
        val serviceParams = args.service.serviceParams ?: emptyMap()
        when (serviceParams.get("field_yourConcern")) {
            "OPTIONAL" -> {
                binding.yourConcern.hint =
                    getString(R.string.dr_003_your_concern_hint, getString(R.string.dr_003_optional_field_label))
                binding.yourConcern.validation =
                    FieldValidation(FieldValidation.HELPER, null, R.string.dr_003_your_concern_disclaimer)
            }

            "REQUIRED" -> {
                mandatoryFields.add(binding.yourConcern)
                binding.yourConcern.hint = getString(R.string.dr_003_your_concern_hint, "")
                binding.yourConcern.validation =
                    FieldValidation(FieldValidation.HELPER, null, R.string.dr_003_your_concern_disclaimer)
            }

            else ->
                binding.yourConcern.visibility = View.GONE
        }
        when (serviceParams["field_email"]) {
            "OPTIONAL" -> {
                binding.emailAddressInput.hint =
                    getString(R.string.dr_003_email_address_hint, getString(R.string.dr_003_optional_field_label))
                showContactDetailsLabel = true
            }

            "REQUIRED" -> {
                mandatoryFields.add(binding.emailAddressInput)
                binding.emailAddressInput.hint = getString(R.string.dr_003_email_address_hint, "")
                showContactDetailsLabel = true
            }

            else ->
                binding.emailAddressInput.visibility = View.GONE
        }
        when (serviceParams["field_wasteBinId"]) {
            "OPTIONAL" ->
                binding.wasteBinId.hint =
                    getString(R.string.dr_003_waste_bin_id_hint, getString(R.string.dr_003_optional_field_label))

            "REQUIRED" -> {
                mandatoryFields.add(binding.wasteBinId)
                binding.wasteBinId.hint = getString(R.string.dr_003_waste_bin_id_hint, "")
            }

            else ->
                binding.wasteBinId.visibility = View.GONE
        }
        when (serviceParams["field_firstName"]) {
            "OPTIONAL" -> {
                binding.firstName.hint =
                    getString(R.string.dr_003_first_name_hint, getString(R.string.dr_003_optional_field_label))
                showContactDetailsLabel = true
            }

            "REQUIRED" -> {
                mandatoryFields.add(binding.firstName)
                binding.firstName.hint = getString(R.string.dr_003_first_name_hint, "")
                showContactDetailsLabel = true
            }

            else ->
                binding.firstName.visibility = View.GONE
        }
        when (serviceParams["field_lastName"]) {
            "OPTIONAL" -> {
                binding.lastName.hint =
                    getString(R.string.dr_003_last_name_hint, getString(R.string.dr_003_optional_field_label))
                showContactDetailsLabel = true
            }

            "REQUIRED" -> {
                mandatoryFields.add(binding.lastName)
                binding.lastName.hint = getString(R.string.dr_003_last_name_hint, "")
                showContactDetailsLabel = true
            }

            else ->
                binding.lastName.visibility = View.GONE
        }
        when (serviceParams["field_uploadImage"]) {
            "OPTIONAL" ->
                binding.uploadPhotoLabel.text =
                    getString(R.string.dr_003_add_photo_label, getString(R.string.dr_003_optional_field_label))

            "REQUIRED" -> {
                isImageMandatory = true
                binding.uploadPhotoLabel.text = getString(R.string.dr_003_add_photo_label, "")
            }

            else -> {
                binding.uploadPhotoLabel.visibility = View.GONE
                binding.photoTab.visibility = View.GONE
            }
        }
        when (serviceParams["field_checkBoxTerms2"]) {
            "OPTIONAL" -> {
                binding.containerAcceptTerms.visibility = View.VISIBLE
            }

            "REQUIRED" -> {
                isTermsRequired = true
                binding.containerAcceptTerms.visibility = View.VISIBLE
            }

            else -> {
                binding.containerAcceptTerms.visibility = View.GONE
                binding.checkTerms.isChecked = true
            }
        }
        if (showContactDetailsLabel.not()) {
            binding.contactDetailsSeparator.visibility = View.GONE
            binding.contactDetailsLabel.visibility = View.GONE
        }
    }

    private fun showErrorPrivacy(isChecked: Boolean) {
        val colorId = if (isChecked) R.color.onSurface else R.color.red
        binding.checkPrivacy.buttonTintList = ColorStateList.valueOf(getColor(colorId))
        binding.privacyErrorIcon.setImageResource(R.drawable.ic_icon_val_error)
        binding.privacysErrorHint.setVisible(!isChecked)
        binding.privacyErrorIcon.setVisible(!isChecked)
        if (!isChecked) {
            binding.containerConfirmPrivacy.requestFocus()
            binding.containerConfirmPrivacy.performAccessibilityAction(
                AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS,
                null
            )
        }
    }

    private fun showErrorTerms(isChecked: Boolean) {
        val colorId = if (isChecked || isTermsRequired) R.color.onSurface else R.color.red
        binding.checkTerms.buttonTintList = ColorStateList.valueOf(getColor(colorId))
        binding.termsErrorIcon.setImageResource(R.drawable.ic_icon_val_error)
        binding.termsErrorHint.setVisible(!isChecked && isTermsRequired)
        binding.termsErrorIcon.setVisible(!isChecked && isTermsRequired)
        if (!isChecked && isTermsRequired) {
            binding.containerAcceptTerms.requestFocus()
            binding.containerAcceptTerms.performAccessibilityAction(
                AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS,
                null
            )
        }
    }

    private fun setBehaviorListeners() {
        val textWatcher = object : EmptyTextWatcher() {
            override fun afterTextChanged(s: Editable?) {
                updateButtonStatus()
            }
        }
        binding.emailAddressInput.editText.addTextChangedListener(textWatcher)
        binding.firstName.editText.addTextChangedListener(textWatcher)
        binding.lastName.editText.addTextChangedListener(textWatcher)
        binding.yourConcern.editText.addTextChangedListener(textWatcher)
    }

    private fun updateButtonStatus() {
        if (viewModel.isPreview()) {
            binding.progressSendReportBtn.disable()
        } else {
            if (!binding.emailAddressInput.hasErrors &&
                areMandatoryFieldsFilled()
            ) {
                binding.progressSendReportBtn.enable()
            } else {
                binding.progressSendReportBtn.disable()
            }
        }
    }

    private fun areMandatoryFieldsFilled(): Boolean {
        var areFieldsFilled = true
        mandatoryFields.forEach { if (it.text.isEmpty()) areFieldsFilled = false }
        if (isImageMandatory && !binding.visibleImage.isVisible) {
            areFieldsFilled = false
        }
        return areFieldsFilled
    }

    private fun subscribeUi() {
        viewModel.inputValidation.observe(viewLifecycleOwner) {
            binding.progressSendReportBtn.stopLoading()
            binding.defectReportContainer.enable()
            binding.emailAddressInput.validation = FieldValidation(
                FieldValidation.ERROR,
                null,
                R.string.r_001_registration_error_incorrect_email
            )
            updateButtonStatus()
        }

        viewModel.userEmail.observe(viewLifecycleOwner) {
            binding.emailAddressInput.text = it
        }
        viewModel.defectSubmitted.observe(viewLifecycleOwner) {
            binding.progressSendReportBtn.stopLoading()
            binding.defectReportContainer.enable()
            findNavController().navigate(
                DefectReportFormDirections.toDefectSubmitted(
                    it.uniqueId,
                    args.subServiceName ?: args.serviceName,
                    cityName
                )
            )
        }

        viewModel.showRetryDialog.observe(viewLifecycleOwner) {
            DialogUtil.showRetryDialog(requireContext(), viewModel::onRetryRequired) {
                binding.defectReportContainer.enable()
                binding.progressSendReportBtn.stopLoading()
                viewModel.onRetryCanceled()
            }
        }
        viewModel.defectSubmissionError.observe(viewLifecycleOwner) { errorMessage ->
            binding.defectReportContainer.enable()
            binding.progressSendReportBtn.stopLoading()
            showDefectErrorDialog(errorMessage)
        }
        viewModel.technicalError.observe(viewLifecycleOwner) {
            binding.defectReportContainer.enable()
            binding.progressSendReportBtn.stopLoading()
            showDefectErrorDialog(getString(R.string.dr_003_defect_submission_error))
        }
        viewModel.cityName.observe(viewLifecycleOwner) { cityName ->
            this.cityName = cityName
        }
    }

    private fun showDefectErrorDialog(message: String) {
        MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogMaterialTheme)
            .setTitle(R.string.dr_003_defect_submission_error_title)
            .setMessage(message)
            .setPositiveButton(android.R.string.ok, null)
            .setCancelable(false)
            .show()
    }

    private fun setupCheckboxes() {
        val rulesOfUseLink = args.service.serviceParams?.get("rulesOfUseLink")
        val dataPrivacyLink = args.service.serviceParams?.get("dataPrivacyNoticeLink")
        val rules = getString(R.string.dr_003_rules_of_use_link)
        val terms = getString(R.string.dr_003_terms_end_text)
        val termsText = if (rulesOfUseLink.isNullOrEmpty()) {
            getString(R.string.dr_003_terms_text, terms, "")
        } else {
            getString(R.string.dr_003_terms_text, terms, rules)
        }
        val indexOfRules = termsText.indexOf(rules)
        val indexOfTerms = termsText.indexOf(terms)
        val termsSpan = SpannableString(termsText)

        termsSpan.setSpan(
            object : ClickableSpan() {
                override fun onClick(widget: View) {
                    DefectDataPrivacyNotice(dataPrivacyLink)
                        .showDialog(childFragmentManager)
                }
            },
            indexOfTerms, indexOfTerms + terms.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        if (!rulesOfUseLink.isNullOrEmpty()) {
            termsSpan.setSpan(
                object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        DefectRulesOfUse(rulesOfUseLink)
                            .showDialog(childFragmentManager)
                    }
                },
                indexOfRules + 4, indexOfRules + rules.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        binding.checkPrivacy.apply {
            text = termsSpan
            movementMethod = LinkMovementMethod.getInstance()
            setLinkTextColor(CityInteractor.cityColorInt)
        }
        binding.checkTerms.apply {
            movementMethod = LinkMovementMethod.getInstance()
            setLinkTextColor(CityInteractor.cityColorInt)
        }
        binding.checkTerms.setAccessibilityBasedOnViewStateSelection(binding.checkTerms.isChecked)
        binding.checkPrivacy.setAccessibilityBasedOnViewStateSelection(binding.checkPrivacy.isChecked)
        binding.checkTerms.setOnClickListener { it.setAccessibilityBasedOnViewStateSelection(binding.checkTerms.isChecked) }
        binding.checkPrivacy.setOnClickListener { it.setAccessibilityBasedOnViewStateSelection(binding.checkPrivacy.isChecked) }
    }

    @SuppressLint("MissingPermission")
    private fun arePermissionsGranted() =
        requireContext().hasPermission(Manifest.permission.CAMERA)

    private fun requestPermission() {
        requestPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    private fun createIntentChooser() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(requireContext().packageManager)?.also {
                lateinit var photoFile: File
                try {
                    photoFile = createImageFile()
                } catch (ex: IOException) {
                    showDefectErrorDialog(getString(R.string.dialog_technical_error_message))
                }
                photoUri =
                    FileProvider.getUriForFile(
                        requireContext(),
                        "${BuildConfig.APPLICATION_ID}.fileprovider",
                        photoFile
                    )
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
            }
        }
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).also {
            it.putExtra(MediaStore.EXTRA_PICK_IMAGES_MAX, 1)
        }
        val chooser = Intent.createChooser(galleryIntent, getString(R.string.dr_003_defect_image_option_title))
        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(cameraIntent))
        getCapturedContent.launch(chooser)
    }

    override fun onDestroyView() {
        googleMap?.clear()
        googleMap = null
        storageDir.deleteRecursively()
        super.onDestroyView()
    }

}
