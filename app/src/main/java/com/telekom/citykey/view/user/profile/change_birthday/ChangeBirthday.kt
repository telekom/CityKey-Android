package com.telekom.citykey.view.user.profile.change_birthday

import android.os.Bundle
import android.view.View
import androidx.core.view.isNotEmpty
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.telekom.citykey.R
import com.telekom.citykey.custom.views.inputfields.FieldValidation
import com.telekom.citykey.databinding.ProfileChangeBirthdayFragmentBinding
import com.telekom.citykey.utils.DialogUtil
import com.telekom.citykey.utils.extensions.disable
import com.telekom.citykey.utils.extensions.enable
import com.telekom.citykey.utils.extensions.isInPast
import com.telekom.citykey.utils.extensions.toDateString
import com.telekom.citykey.utils.extensions.viewBinding
import com.telekom.citykey.view.user.profile.ProfileActivity
import org.koin.androidx.viewmodel.ext.android.viewModel

class ChangeBirthday : Fragment(R.layout.profile_change_birthday_fragment) {

    private val viewModel: ChangeBirthdayViewModel by viewModel()
    private val binding: ProfileChangeBirthdayFragmentBinding by viewBinding(ProfileChangeBirthdayFragmentBinding::bind)
    private val args: ChangeBirthdayArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.removeBirthdayBtn.setupOutlineStyle()

        if (args.currentBirthday != null) {
            (activity as? ProfileActivity)?.setPageTitle(R.string.p_003_change_dob_title)
            binding.oldBirthdayInput.text = args.currentBirthday!!.toDateString()
        } else {
            binding.oldBirthdayInput.visibility = View.GONE
            binding.removeBirthdayBtn.visibility = View.GONE
            (activity as? ProfileActivity)?.setPageTitle(R.string.p_003_add_date_of_birth_title)
            binding.newBirthdayInput.hint = getString(R.string.p_003_change_dob_add_date)
        }
        binding.saveBirthdayBtn.disable()
        binding.oldBirthdayInput.disable()
        binding.oldBirthdayInput.deactivateChildren()
        binding.newBirthdayInput.setOnClickListener {
            DialogUtil.showDatePickerDialog(
                fragmentManager = childFragmentManager,
                onDateSelected = { date ->
                    binding.newBirthdayInput.text = date.toDateString()
                    if (date.isInPast) {
                        viewModel.onBirthdaySelected(date)
                        binding.newBirthdayInput.validation = FieldValidation(FieldValidation.IDLE, null)
                    } else {
                        binding.newBirthdayInput.validation = FieldValidation(
                            FieldValidation.ERROR,
                            null,
                            R.string.p_003_change_dob_future_date_error
                        )
                        binding.newBirthdayInput.requestFocus()
                    }

                    updateSaveBtnState()
                },
                onCancel = {
                    binding.newBirthdayInput.clear()
                    binding.newBirthdayInput.validation = FieldValidation(FieldValidation.IDLE, null)
                }
            )
        }

        binding.removeBirthdayBtn.setOnClickListener {
            viewModel.onBirthdaySelected(null)
            binding.newBirthdayInput.clear()
            viewModel.onSaveClicked()
            binding.removeBirthdayBtn.startLoading()
        }

        binding.saveBirthdayBtn.setOnClickListener {
            viewModel.onSaveClicked()
            binding.saveBirthdayBtn.startLoading()
        }
        binding.newBirthdayInput.onTextChanged { updateSaveBtnState() }

        subscribeUi()
    }

    private fun subscribeUi() {
        viewModel.technicalError.observe(viewLifecycleOwner) {
            DialogUtil.showTechnicalError(requireContext())
            binding.saveBirthdayBtn.stopLoading()
        }

        viewModel.validationError.observe(viewLifecycleOwner) {
            binding.newBirthdayInput.validation = it
            binding.saveBirthdayBtn.stopLoading()
            updateSaveBtnState()
        }

        viewModel.logUserOut.observe(viewLifecycleOwner) {
            (activity as ProfileActivity).logOut()
        }

        viewModel.showRetryDialog.observe(viewLifecycleOwner) {
            DialogUtil.showRetryDialog(requireContext(), viewModel::onRetryRequired) {
                viewModel.onRetryCanceled()
                binding.saveBirthdayBtn.stopLoading()
            }
        }

        viewModel.saveSuccessful.observe(viewLifecycleOwner) {
            findNavController().navigateUp()
        }
    }

    private fun updateSaveBtnState() {
        if (binding.newBirthdayInput.isNotEmpty() && !binding.newBirthdayInput.hasErrors) {
            binding.saveBirthdayBtn.enable()
        } else {
            binding.saveBirthdayBtn.disable()
        }
    }
}
