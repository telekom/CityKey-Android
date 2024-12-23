package com.telekom.citykey.view.dialogs

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import com.telekom.citykey.R
import com.telekom.citykey.databinding.ForceUpdateDialogBinding
import com.telekom.citykey.utils.extensions.viewBinding
import com.telekom.citykey.view.FullScreenBottomSheetDialogFragment

class ForceUpdateDialog : FullScreenBottomSheetDialogFragment(R.layout.force_update_dialog) {

    companion object {
        const val TAG = "ForceUpdate"
        const val PACKAGE_NAME = "com.telekom.citykey"
    }

    init {
        isCancelable = false
    }

    private val binding: ForceUpdateDialogBinding by viewBinding(ForceUpdateDialogBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.updateBtn.setOnClickListener {
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$PACKAGE_NAME")))
            } catch (e: ActivityNotFoundException) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$PACKAGE_NAME")))
            }
        }
    }
}
