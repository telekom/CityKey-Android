package com.telekom.citykey.view.dialogs

import android.animation.LayoutTransition
import android.os.Bundle
import android.view.View
import com.telekom.citykey.R
import com.telekom.citykey.databinding.DataPrivacySettingsDialogBinding
import com.telekom.citykey.domain.track.AdjustManager
import com.telekom.citykey.utils.extensions.AccessibilityRole
import com.telekom.citykey.utils.extensions.getColor
import com.telekom.citykey.utils.extensions.setAccessibilityRole
import com.telekom.citykey.utils.extensions.setAccessibilityRoleForToolbarTitle
import com.telekom.citykey.utils.extensions.showDialog
import com.telekom.citykey.utils.extensions.viewBinding
import com.telekom.citykey.view.FullScreenBottomSheetDialogFragment
import org.koin.android.ext.android.inject

class DataPrivacySettingsDialog(
    private val acceptedListener: (() -> Unit)? = null,
    private val isLaunchedFromNotice: Boolean = false
) : FullScreenBottomSheetDialogFragment(R.layout.data_privacy_settings_dialog) {

    private val adjustManager: AdjustManager by inject()
    private val binding: DataPrivacySettingsDialogBinding by viewBinding(DataPrivacySettingsDialogBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.scrollView.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        binding.root.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        binding.requiredToolsBlock.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        binding.optionalToolsBlock.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)

        binding.btnChangeSettings.setupOutlineStyle()
        binding.optionalToolsToggle.isChecked = adjustManager.isAnalyticsEventTrackingAllowed

        binding.dataSecurityNoticeLink.setOnClickListener {
            if (isLaunchedFromNotice) dismiss()
            else DataPrivacyNoticeDialog(isLaunchedFromSettings = true)
                .showDialog(parentFragmentManager, "DataPrivacyNoticeDialog")
        }

        binding.toolbar.setNavigationIcon(R.drawable.ic_profile_close)
        binding.toolbar.setNavigationIconTint(getColor(R.color.onSurface))
        binding.toolbar.setNavigationContentDescription(R.string.accessibility_btn_close)
        binding.toolbar.setNavigationOnClickListener { dismiss() }
        setAccessibilityRoleForToolbarTitle(binding.toolbar)
        setAccessibilityRoles()
        binding.btnAcceptAll.setOnClickListener {
            adjustManager.updateTrackingPermissions(true)
            acceptedListener?.invoke()
            dismiss()
        }

        binding.btnChangeSettings.setOnClickListener {
            adjustManager.updateTrackingPermissions(binding.optionalToolsToggle.isChecked)
            acceptedListener?.invoke()
            dismiss()
        }
        binding.requiredToolsShowMore.setOnClickListener {
            binding.requiredToolsDescription.updateState()
            binding.requiredToolsShowMore.setText(
                if (binding.requiredToolsDescription.isCollapsed) R.string.dialog_dpn_settings_show_more_btn
                else R.string.dialog_dpn_settings_show_less_btn
            )
        }

        binding.optionalToolsShowMore.setOnClickListener {
            binding.optionalToolsDescription.updateState()
            binding.optionalToolsShowMore.setText(
                if (binding.optionalToolsDescription.isCollapsed) R.string.dialog_dpn_settings_show_more_btn
                else R.string.dialog_dpn_settings_show_less_btn
            )
        }
    }

    private fun setAccessibilityRoles() {
        binding.requiredToolsShowMore.setAccessibilityRole(AccessibilityRole.Button)
        binding.optionalToolsShowMore.setAccessibilityRole(AccessibilityRole.Button)
        binding.dataSecurityNoticeLink.setAccessibilityRole(AccessibilityRole.Button)
        binding.textHeader.setAccessibilityRole(
            AccessibilityRole.Heading,
            (getString(R.string.accessibility_heading_level_2))
        )
        binding.requiredToolsHeader.setAccessibilityRole(
            AccessibilityRole.Heading,
            (getString(R.string.accessibility_heading_level_2))
        )
        binding.optionalToolsHeader.setAccessibilityRole(
            AccessibilityRole.Heading,
            (getString(R.string.accessibility_heading_level_2))
        )
    }
}
