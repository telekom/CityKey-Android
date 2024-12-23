package com.telekom.citykey.view.services.appointments.qr

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.telekom.citykey.R
import com.telekom.citykey.databinding.AppointmentsQrFragmentBinding
import com.telekom.citykey.utils.extensions.viewBinding
import com.telekom.citykey.view.MainFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class AppointmentQR : MainFragment(R.layout.appointments_qr_fragment) {

    private val viewModel: AppointmentQRViewModel by viewModel()
    private val args: AppointmentQRArgs by navArgs()
    private val binding by viewBinding(AppointmentsQrFragmentBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.onViewCreated(args.uuid)
        setupToolbar(binding.toolbarAppointmentQR)

        binding.waitingNumber.text = args.waitingNo

        subscribeUi()
    }

    private fun subscribeUi() {
        viewModel.qrBitmap.observe(viewLifecycleOwner) {
            binding.qrCode.setImageBitmap(it)
        }

        viewModel.userLoggedOut.observe(viewLifecycleOwner) {
            findNavController().popBackStack(R.id.services, false)
        }
    }
}
