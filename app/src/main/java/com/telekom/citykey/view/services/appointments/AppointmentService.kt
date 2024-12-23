package com.telekom.citykey.view.services.appointments

import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.telekom.citykey.BuildConfig
import com.telekom.citykey.R
import com.telekom.citykey.common.GlideApp
import com.telekom.citykey.databinding.ServicePageAppointmentFragmentBinding
import com.telekom.citykey.domain.city.CityInteractor
import com.telekom.citykey.utils.DialogUtil
import com.telekom.citykey.utils.NetworkConnection
import com.telekom.citykey.utils.extensions.AccessibilityRole
import com.telekom.citykey.utils.extensions.setAccessibilityRole
import com.telekom.citykey.utils.extensions.setVisible
import com.telekom.citykey.utils.extensions.viewBinding
import com.telekom.citykey.view.MainFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class AppointmentService : MainFragment(R.layout.service_page_appointment_fragment) {

    private val viewModel: AppointmentServiceViewModel by viewModel()
    private val args: AppointmentServiceArgs by navArgs()
    private val binding by viewBinding(ServicePageAppointmentFragmentBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbarAppointmentServices.title = args.service.service
        setupToolbar(binding.toolbarAppointmentServices)
        setupViews()
        subscribeUI()
    }

    private fun subscribeUI() {
        viewModel.updates.observe(viewLifecycleOwner) {
            if (it > 0) binding.badge.text = it.toString()
            binding.badge.setVisible(it > 0)
        }
    }

    private fun setupViews() {
        binding.makeAppointmentButton.button.setBackgroundColor(CityInteractor.cityColorInt)
        (binding.badge.background as LayerDrawable)
            .findDrawableByLayerId(R.id.mainLayer)
            .colorFilter = PorterDuffColorFilter(CityInteractor.cityColorInt, PorterDuff.Mode.SRC_IN)

        GlideApp.with(this)
            .load(BuildConfig.IMAGE_URL + args.service.image)
            .centerCrop()
            .into(binding.image)

        binding.myAppointmentsLabel.setAccessibilityRole(AccessibilityRole.Button)
        binding.fullDescription.loadData(args.service.description, "text/html", "UTF-8")
        val myAppointmentsLabel = args.service.helpLinkTitle ?: getString(R.string.apnmt_001_my_appointments_button)
        binding.myAppointmentsLabel.text = myAppointmentsLabel
        // There will be only one action in case of appointments (double checked it with SOL team)
        val appointmentUri = args.service.serviceAction?.first()?.androidUri ?: ""
        binding.makeAppointmentButton.text = args.service.serviceAction?.first()?.visibleText
        binding.makeAppointmentButton.setOnClickListener {
            if (!NetworkConnection.checkInternetConnection(requireContext())) {
                DialogUtil.showNoInternetDialog(requireContext())
            } else {
                findNavController().navigate(
                    AppointmentServiceDirections
                        .actionAppointmentServiceToAppointmentWeb(appointmentUri, args.service)
                )
            }
        }

        binding.existingAppointments.setOnClickListener {
            findNavController().navigate(AppointmentServiceDirections.actionAppointmentServiceToAppointmentsOverview())
        }
    }
}
