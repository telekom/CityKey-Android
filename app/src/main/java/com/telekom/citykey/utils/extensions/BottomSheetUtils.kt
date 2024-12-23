package com.telekom.citykey.utils.extensions

import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.telekom.citykey.view.FullScreenBottomSheetDialogFragment

fun DialogFragment.showDialog(
    childFragmentManager: FragmentManager,
    tag: String? = FullScreenBottomSheetDialogFragment.TAG
) {
    if (childFragmentManager.findFragmentByTag(tag) == null)
        show(childFragmentManager, tag)
}
