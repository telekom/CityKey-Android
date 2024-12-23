package com.telekom.citykey.view.user.profile.profile

import android.annotation.SuppressLint
import android.appwidget.AppWidgetManager
import android.os.Bundle
import android.view.View
import android.widget.RadioGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.telekom.citykey.R
import com.telekom.citykey.databinding.ProfileFragmentBinding
import com.telekom.citykey.utils.DialogUtil
import com.telekom.citykey.utils.extensions.AccessibilityRole
import com.telekom.citykey.utils.extensions.setAccessibilityRole
import com.telekom.citykey.utils.extensions.setVisible
import com.telekom.citykey.utils.extensions.showDialog
import com.telekom.citykey.utils.extensions.toDateString
import com.telekom.citykey.utils.extensions.updateWasteCalendarWidget
import com.telekom.citykey.utils.extensions.viewBinding
import com.telekom.citykey.view.dialogs.AccessibilityDisclaimerDialog
import com.telekom.citykey.view.dialogs.DataPrivacyNoticeDialog
import com.telekom.citykey.view.dialogs.DataPrivacySettingsDialog
import com.telekom.citykey.view.dialogs.HelpSectionDialog
import com.telekom.citykey.view.dialogs.ImprintBottomSheetDialog
import com.telekom.citykey.view.dialogs.SoftwareLicenseDialog
import com.telekom.citykey.view.user.profile.ProfileActivity
import com.telekom.citykey.view.user.profile.ProfileBackActions
import com.telekom.citykey.view.user.profile.feedback.Feedback
import org.koin.androidx.viewmodel.ext.android.viewModel

class Profile : Fragment(R.layout.profile_fragment) {
    private val viewModel: ProfileViewModel by viewModel()
    private val binding by viewBinding(ProfileFragmentBinding::bind)

    private val modeOnCheckedChangeListener: RadioGroup.OnCheckedChangeListener by lazy {
        RadioGroup.OnCheckedChangeListener { _, checkedId ->
            setCheckedButtonTextAppearance(checkedId)
            viewModel.togglePreviewMode(checkedId == R.id.radioButtonPreviewMode)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        subscribeUi()

        (activity as ProfileActivity).run {
            setPageTitle(R.string.p_001_profile_title)
            backAction = ProfileBackActions.BACK
            adaptToolbarForClose()
        }
    }

    private fun initViews() {
        binding.logoutBtn.setOnClickListener {
            DialogUtil.showDialogPositiveNegative(
                context = requireContext(),
                title = R.string.dialog_confirm_logout_title,
                message = R.string.dialog_confirm_logout_message,
                positiveBtnLabel = R.string.dialog_confirm_logout_btn_yes,
                negativeBtnLabel = R.string.dialog_confirm_logout_btn_cancel,
                positiveClickListener = {
                    viewModel.onLogoutBtnClicked()
                    AppWidgetManager.getInstance(requireContext()).updateWasteCalendarWidget(requireContext())
                }
            )
        }

        binding.privacyBtn.setOnClickListener {
            DataPrivacyNoticeDialog().showDialog(childFragmentManager)
        }

        binding.imprintButton.setOnClickListener {
            ImprintBottomSheetDialog()
                .showDialog(childFragmentManager)
        }
        binding.feedbackButton.setOnClickListener {
            Feedback()
                .showDialog(childFragmentManager)
        }
        binding.privacySettingsBtn.setOnClickListener {
            DataPrivacySettingsDialog()
                .showDialog(childFragmentManager)
        }
        binding.personalSettingButton.setOnClickListener {
            findNavController().navigate(
                ProfileDirections.toPersonalDetailSettings(
                    binding.profileBirthday.text.toString(),
                    binding.profileAddress.text.toString()
                )
            )
        }
        binding.accountSettingsButton.setOnClickListener {
            it.findNavController().navigate(ProfileDirections.toAccountSettings(binding.userEmail.text.toString()))
        }
        binding.helpLinkContainer.setOnClickListener { HelpSectionDialog().showDialog(childFragmentManager) }
        binding.disclaimerButton.setOnClickListener { AccessibilityDisclaimerDialog().showDialog(childFragmentManager) }
        binding.softwareLicense.setOnClickListener { SoftwareLicenseDialog().showDialog(childFragmentManager) }
        loadPreviewSettingsIfApplicable()
        setAccessibilityRoles()
    }

    private fun loadPreviewSettingsIfApplicable() {
        val isCspUser = viewModel.isCspUser()
        binding.containerMode.setVisible(isCspUser)
        binding.radioGroupModeSelection.apply {
            setOnCheckedChangeListener(null)
            if (isCspUser) {
                val checkedButtonId =
                    if (viewModel.isPreviewMode()) R.id.radioButtonPreviewMode else R.id.radioButtonLiveMode
                check(checkedButtonId)
                setCheckedButtonTextAppearance(checkedButtonId)
                setOnCheckedChangeListener(modeOnCheckedChangeListener)
            }
        }
    }

    private fun setCheckedButtonTextAppearance(checkedId: Int) {
        when (checkedId) {
            R.id.radioButtonLiveMode -> {
                binding.radioButtonLiveMode.setTextAppearance(R.style.RobotoBold)
                binding.radioButtonPreviewMode.setTextAppearance(R.style.RobotoRegular)
            }

            R.id.radioButtonPreviewMode -> {
                binding.radioButtonLiveMode.setTextAppearance(R.style.RobotoRegular)
                binding.radioButtonPreviewMode.setTextAppearance(R.style.RobotoBold)
            }
        }
    }

    @SuppressLint("SetTextI18n", "ClickableViewAccessibility")
    private fun subscribeUi() {
        viewModel.profileContent.observe(viewLifecycleOwner) {
            binding.userEmail.text = it.email
            binding.containerMail.contentDescription =
                getString(R.string.p_001_profile_label_email) + "," + it.email
            if (it.dateOfBirth != null) {
                binding.profileBirthday.text = it.dateOfBirth.toDateString()
                binding.containerBirthday.contentDescription =
                    getString(R.string.p_001_profile_label_birthday) + it.dateOfBirth.toDateString()
            } else {
                binding.profileBirthday.setText(R.string.p_001_profile_no_date_of_birth_added)
                binding.containerBirthday.contentDescription =
                    getString(R.string.p_001_profile_label_birthday) + getString(R.string.p_001_profile_no_date_of_birth_added)
            }
            binding.profileAddress.text = "${it.postalCode} ${it.cityName}"
            binding.containerResidence.contentDescription =
                getString(R.string.p_001_profile_label_residence) + "${it.postalCode} ${it.cityName}"
            loadPreviewSettingsIfApplicable()
        }
        viewModel.refreshStarted.observe(viewLifecycleOwner) {
            binding.loaderContainer.apply {
                setVisible(true)
                setOnClickListener { }
                setOnTouchListener { _, _ -> return@setOnTouchListener true }
            }
        }
        viewModel.refreshFinished.observe(viewLifecycleOwner) {
            if (binding.containerMode.isVisible) {
                binding.radioGroupModeSelection.apply {
                    val checkId =
                        if (viewModel.isPreviewMode()) R.id.radioButtonPreviewMode else R.id.radioButtonLiveMode
                    if (checkedRadioButtonId != checkId) {
                        setOnCheckedChangeListener(null)
                        check(checkId)
                        setCheckedButtonTextAppearance(checkId)
                        setOnCheckedChangeListener(modeOnCheckedChangeListener)
                    }
                }
            }
            binding.loaderContainer.setVisible(false)
        }
        viewModel.logOutUser.observe(viewLifecycleOwner) {
            (activity as? ProfileActivity)?.logOut()
        }
        viewModel.previewModeToggleFailed.observe(viewLifecycleOwner) {
            context?.let {
                Toast.makeText(it, getString(R.string.dialog_technical_error_message), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setAccessibilityRoles() {
        binding.helpLinkContainer.setAccessibilityRole(AccessibilityRole.Button)
        binding.imprintButton.setAccessibilityRole(AccessibilityRole.Link)
        binding.privacyBtn.setAccessibilityRole(AccessibilityRole.Link)
        binding.privacySettingsBtn.setAccessibilityRole(AccessibilityRole.Link)
        binding.feedbackButton.setAccessibilityRole(AccessibilityRole.Link)
        binding.disclaimerButton.setAccessibilityRole(AccessibilityRole.Link)
        binding.softwareLicense.setAccessibilityRole(AccessibilityRole.Link)
        binding.labelPersonalData.setAccessibilityRole(
            AccessibilityRole.Heading,
            (getString(R.string.accessibility_heading_level_2))
        )
        binding.labelAccountSettings.setAccessibilityRole(
            AccessibilityRole.Heading,
            (getString(R.string.accessibility_heading_level_2))
        )
        binding.helpSectionHeaderLabel.setAccessibilityRole(
            AccessibilityRole.Heading,
            (getString(R.string.accessibility_heading_level_2))
        )
    }
}
