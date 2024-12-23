package com.telekom.citykey.view.dialogs

import android.content.DialogInterface
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import com.telekom.citykey.R
import com.telekom.citykey.databinding.DataPrivacyFtuDialogBinding
import com.telekom.citykey.domain.track.AdjustManager
import com.telekom.citykey.utils.PreferencesHelper
import com.telekom.citykey.utils.extensions.getColor
import com.telekom.citykey.utils.extensions.setAccessibilityRoleForToolbarTitle
import com.telekom.citykey.utils.extensions.showDialog
import com.telekom.citykey.utils.extensions.viewBinding
import com.telekom.citykey.view.FullScreenBottomSheetDialogFragment
import org.koin.android.ext.android.inject

class FtuDataPrivacyDialog : FullScreenBottomSheetDialogFragment(R.layout.data_privacy_ftu_dialog) {

    private val adjustManager: AdjustManager by inject()
    private val preferencesHelper: PreferencesHelper by inject()
    private val binding: DataPrivacyFtuDialogBinding by viewBinding(DataPrivacyFtuDialogBinding::bind)

    init {
        isCancelable = false
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setAccessibilityRoleForToolbarTitle(binding.toolbar)
        binding.btnChangeSettings.setupOutlineStyle()

        binding.btnAcceptAll.setOnClickListener {
            adjustManager.updateTrackingPermissions(true)
            preferencesHelper.saveConfirmedTrackingTerms()
            dismiss()
        }

        binding.btnChangeSettings.setOnClickListener {
            DataPrivacySettingsDialog(
                acceptedListener = {
                    preferencesHelper.saveConfirmedTrackingTerms()
                    dismiss()
                }
            )
                .showDialog(parentFragmentManager, "DataPrivacySettingsDialog")
        }

        setupDescription()
    }

    private fun setupDescription() {
        val dpnLink = getString(R.string.dialog_dpn_ftu_dpn_link)
        val continueLink = getString(R.string.dialog_dpn_ftu_continue_link)
        val format = getString(
            R.string.dialog_dpn_ftu_text_format,
            dpnLink,
            continueLink
        )
        val dpnLinkIndex = format.indexOf(dpnLink)
        val continueLinkIndex = format.indexOf(continueLink)
        val textSpan = SpannableString(format)

        textSpan.setSpan(
            object : ClickableSpan() {
                override fun onClick(widget: View) {
                    DataPrivacyNoticeDialog()
                        .showDialog(parentFragmentManager, "DataPrivacyNoticeDialog")
                }
            },
            dpnLinkIndex, dpnLinkIndex + dpnLink.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        textSpan.setSpan(
            object : ClickableSpan() {
                override fun onClick(widget: View) {
                    adjustManager.updateTrackingPermissions(false)
                    dismiss()
                }
            },
            continueLinkIndex, continueLinkIndex + continueLink.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        binding.textBody.apply {
            text = textSpan
            movementMethod = LinkMovementMethod.getInstance()
            setLinkTextColor(getColor(R.color.oscaColor))
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        adjustManager.trackAppLaunchedEvent()
    }
}
