package com.telekom.citykey.view.user.profile.feedback

import android.os.Bundle
import android.text.Editable
import android.view.View
import com.telekom.citykey.R
import com.telekom.citykey.custom.views.inputfields.FieldValidation
import com.telekom.citykey.databinding.FeedbackDialogBinding
import com.telekom.citykey.utils.DialogUtil
import com.telekom.citykey.utils.EmptyTextWatcher
import com.telekom.citykey.utils.extensions.disable
import com.telekom.citykey.utils.extensions.enable
import com.telekom.citykey.utils.extensions.getColor
import com.telekom.citykey.utils.extensions.setAccessibilityRoleForToolbarTitle
import com.telekom.citykey.utils.extensions.viewBinding
import com.telekom.citykey.view.FullScreenBottomSheetDialogFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class Feedback : FullScreenBottomSheetDialogFragment(R.layout.feedback_dialog) {
    private val binding: FeedbackDialogBinding by viewBinding(FeedbackDialogBinding::bind)
    private val viewModel: FeedbackViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        subscribeUi()
    }

    fun subscribeUi() {
        viewModel.feedbackSubmitted.observe(viewLifecycleOwner) {
            binding.feedbackForm.visibility = View.GONE
            binding.feedbackSuccess.visibility = View.VISIBLE
        }
        viewModel.technicalError.observe(viewLifecycleOwner) {
            binding.sendBtn.stopLoading()
            DialogUtil.showTechnicalError(requireContext())
        }
        viewModel.showRetryDialog.observe(viewLifecycleOwner) {
            binding.sendBtn.stopLoading()
            DialogUtil.showRetryDialog(requireContext())
        }

        viewModel.inputValidation.observe(viewLifecycleOwner) {
            if (it.second.stringRes != 0 && binding.contactEmailSwitch.isChecked) {
                binding.contactEmailInput.validation = it.second
            }
            updateButtonStatus()
        }

        viewModel.profileContent.observe(viewLifecycleOwner) {
            if (it.isNotEmpty())
                binding.contactEmailInput.text = it
        }
    }

    fun initViews() {
        binding.sendBtn.disable()
        binding.toolbar.setNavigationIcon(R.drawable.ic_profile_close)
        binding.toolbar.setNavigationIconTint(getColor(R.color.onSurface))
        binding.toolbar.setNavigationContentDescription(R.string.accessibility_btn_close)
        binding.toolbar.setNavigationOnClickListener { dismiss() }
        setAccessibilityRoleForToolbarTitle(binding.toolbar)
        binding.sendBtn.setOnClickListener {
            binding.sendBtn.startLoading()
            val contactEmailId = if (binding.contactEmailSwitch.isChecked) binding.contactEmailInput.text else ""
            viewModel.onSendClicked(
                binding.feedBackTextInput.text.toString(),
                binding.feedbackTextInput1.text.toString(),
                contactEmailId
            )
        }
        binding.okButton.setOnClickListener {
            dismiss()
        }

        binding.contactEmailInput.onTextChanged { text ->
            if (binding.contactEmailSwitch.isChecked)
                viewModel.onEmailReady(text)
        }

        binding.contactEmailSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && binding.contactEmailInput.text.isNotEmpty()) {
                viewModel.onEmailReady(binding.contactEmailInput.text)
            } else {
                binding.contactEmailInput.validation = FieldValidation(FieldValidation.IDLE, null)
            }
            binding.contactEmailInput.isEnabled = isChecked
            updateButtonStatus()
        }

        setBehaviorListeners()
    }

    private fun setBehaviorListeners() {
        val textWatcher = object : EmptyTextWatcher() {
            override fun afterTextChanged(s: Editable?) {
                updateButtonStatus()
            }
        }
        binding.feedBackTextInput.addTextChangedListener(textWatcher)
        binding.feedbackTextInput1.addTextChangedListener(textWatcher)
    }

    private fun updateButtonStatus() {
        var isUserEmailEnable =
            binding.contactEmailSwitch.isChecked && binding.contactEmailInput.text.isNotEmpty() && !binding.contactEmailInput.hasErrors

        if ((binding.feedBackTextInput.text?.isNotBlank() == true || binding.feedbackTextInput1.text?.isNotEmpty() == true) && ((isUserEmailEnable) || !binding.contactEmailSwitch.isChecked)) {
            binding.sendBtn.enable()
        } else {
            binding.sendBtn.disable()
        }
    }
}
