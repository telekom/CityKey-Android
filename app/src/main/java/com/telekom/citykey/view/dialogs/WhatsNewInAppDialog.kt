package com.telekom.citykey.view.dialogs

import android.os.Bundle
import android.view.View
import com.telekom.citykey.R
import com.telekom.citykey.databinding.WhatsNewInAppDialogBinding
import com.telekom.citykey.utils.extensions.viewBinding
import com.telekom.citykey.view.FullScreenBottomSheetDialogFragment

class WhatsNewInAppDialog : FullScreenBottomSheetDialogFragment(R.layout.whats_new_in_app_dialog) {
    companion object {
        const val TAG = "WhatsNewInApp"
    }

    init {
        isCancelable = false
    }

    private val binding: WhatsNewInAppDialogBinding by viewBinding(WhatsNewInAppDialogBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnContinue.setOnClickListener {
            dialog?.dismiss()
        }
    }
}
