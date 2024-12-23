package com.telekom.citykey.view.user.profile.change_residence

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.telekom.citykey.R
import com.telekom.citykey.custom.views.inputfields.FieldValidation
import com.telekom.citykey.databinding.ProfileChangeResidenceFragmentBinding
import com.telekom.citykey.utils.DialogUtil
import com.telekom.citykey.utils.extensions.disable
import com.telekom.citykey.utils.extensions.enable
import com.telekom.citykey.utils.extensions.viewBinding
import com.telekom.citykey.view.user.profile.ProfileActivity
import com.telekom.citykey.view.user.profile.ProfileBackActions
import org.koin.androidx.viewmodel.ext.android.viewModel

class ChangeResidence : Fragment(R.layout.profile_change_residence_fragment) {

    private val binding by viewBinding(ProfileChangeResidenceFragmentBinding::bind)
    private val viewModel: ChangeResidenceViewModel by viewModel()
    private val args: ChangeResidenceArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? ProfileActivity)?.run {
            setPageTitle(R.string.p_003_profile_new_postcode_title)
            backAction = ProfileBackActions.BACK
            adaptToolbarForBack()
        }
        subscribeUi()
        initViews()
    }

    fun initViews() {
        binding.newPostcodeInput.onTextChanged {
            handleProgressButton()
        }
        binding.saveResidenceBtn.disable()
        binding.oldResidenceInput.text = args.postalCode
        binding.oldResidenceInput.disable()
        binding.saveResidenceBtn.setOnClickListener {
            binding.saveResidenceBtn.startLoading()
            viewModel.onSaveClicked(binding.newPostcodeInput.text)
        }
    }

    fun subscribeUi() {
        viewModel.showRetryDialog.observe(viewLifecycleOwner) {
            DialogUtil.showRetryDialog(
                context = requireContext(),
                onRetry = {
                    binding.saveResidenceBtn.startLoading()
                    viewModel.onRetryRequired()
                },
                onCancel = viewModel::onRetryCanceled
            )
            binding.saveResidenceBtn.stopLoading()
        }

        viewModel.technicalError.observe(viewLifecycleOwner) {
            binding.saveResidenceBtn.stopLoading()
            DialogUtil.showTechnicalError(requireContext())
        }

        viewModel.onlineErrors.observe(viewLifecycleOwner) { errorMessage ->
            binding.saveResidenceBtn.stopLoading()
            binding.newPostcodeInput.validation = FieldValidation(FieldValidation.ERROR, errorMessage.first)
            handleProgressButton()
        }

        viewModel.generalErrors.observe(viewLifecycleOwner) {
            binding.saveResidenceBtn.stopLoading()
            binding.newPostcodeInput.validation = FieldValidation(FieldValidation.ERROR, null, it)
        }

        viewModel.logUserOut.observe(viewLifecycleOwner) {
            (activity as ProfileActivity).logOut()
        }

        viewModel.saveSuccessful.observe(viewLifecycleOwner) {
            binding.saveResidenceBtn.stopLoading()
            findNavController().navigateUp()
        }

        viewModel.inputValidation.observe(viewLifecycleOwner) { error ->
            binding.saveResidenceBtn.stopLoading()
            binding.newPostcodeInput.validation = FieldValidation(FieldValidation.ERROR, null, error)
            handleProgressButton()
        }

        viewModel.requestSent.observe(viewLifecycleOwner) {
            binding.newPostcodeInput.validation = FieldValidation(FieldValidation.SUCCESS, it.postalCodeMessage)
        }
    }

    private fun handleProgressButton() {
        if (!binding.newPostcodeInput.hasErrors && binding.newPostcodeInput.text.isNotBlank()) {
            binding.saveResidenceBtn.enable()
        } else {
            binding.saveResidenceBtn.disable()
        }
    }
}
