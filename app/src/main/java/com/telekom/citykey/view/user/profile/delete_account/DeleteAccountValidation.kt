package com.telekom.citykey.view.user.profile.delete_account

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.telekom.citykey.R
import com.telekom.citykey.databinding.ProfileDeleteAccountValidationFragmentBinding
import com.telekom.citykey.utils.DialogUtil
import com.telekom.citykey.utils.DialogUtil.showTechnicalError
import com.telekom.citykey.utils.extensions.disable
import com.telekom.citykey.utils.extensions.enable
import com.telekom.citykey.utils.extensions.viewBinding
import com.telekom.citykey.view.user.profile.ProfileActivity
import com.telekom.citykey.view.user.profile.ProfileBackActions
import org.koin.androidx.viewmodel.ext.android.viewModel

class DeleteAccountValidation : Fragment(R.layout.profile_delete_account_validation_fragment) {

    private val viewModel: DeleteAccountValidationViewModel by viewModel()
    private val binding by viewBinding(ProfileDeleteAccountValidationFragmentBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as ProfileActivity).run {
            setPageTitle(R.string.d_002_delete_account_validation_title)
            adaptToolbarForBack()
        }

        if (binding.currentPassword.text.isEmpty()) binding.deleteAccountButton.disable()

        binding.deleteAccountButton.setOnClickListener {
            binding.deleteAccountButton.startLoading()
            (activity as? ProfileActivity)?.apply {
                adaptToolbarForClose()
                backAction = ProfileBackActions.LOGOUT
            }
            viewModel.onConfirmClicked(binding.currentPassword.text)
        }

        binding.currentPassword.onTextChanged {
            if (it.isEmpty()) binding.deleteAccountButton.disable() else binding.deleteAccountButton.enable()
        }

        subscribeUi()
    }

    private fun subscribeUi() {
        viewModel.error.observe(viewLifecycleOwner) {
            (activity as? ProfileActivity)?.apply {
                adaptToolbarForBack()
                backAction = ProfileBackActions.BACK
            }
            binding.deleteAccountButton.stopLoadingAfterError()
            binding.currentPassword.error = it
        }

        viewModel.success.observe(viewLifecycleOwner) {
            findNavController()
                .navigate(R.id.action_deleteAccountValidation_to_deleteAccountConfirmation)
        }

        viewModel.technicalError.observe(viewLifecycleOwner) {
            binding.deleteAccountButton.stopLoading()
            showTechnicalError(requireContext())
        }

        viewModel.showRetryDialog.observe(viewLifecycleOwner) {
            binding.deleteAccountButton.stopLoading()
            DialogUtil.showRetryDialog(
                context = requireContext(),
                onRetry = {
                    binding.deleteAccountButton.startLoading()
                    viewModel.onRetryRequired()
                },
                onCancel = {
                    viewModel.onRetryCanceled()
                    findNavController().popBackStack(R.id.accountSettings, false)
                }
            )
        }
    }
}
