package com.telekom.citykey.view

import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.children
import androidx.fragment.app.Fragment
import com.telekom.citykey.R
import com.telekom.citykey.utils.extensions.AccessibilityRole
import com.telekom.citykey.utils.extensions.setAccessibilityRole
import com.telekom.citykey.utils.extensions.shouldPreventContentSharing
import com.telekom.citykey.view.main.MainActivity

abstract class MainFragment(layoutResId: Int, private val containsSensitiveInfo: Boolean = false) :
    Fragment(layoutResId) {

    protected fun setupToolbar(toolbar: Toolbar) {
        (activity as? MainActivity)?.setupActionBar(toolbar)
        val titleTextView = toolbar.children.firstOrNull { view -> view is TextView }
        titleTextView?.setAccessibilityRole(AccessibilityRole.Heading)
        setHasOptionsMenu(true)
    }

    override fun onResume() {
        super.onResume()
        if (containsSensitiveInfo)
            setSecureFlag()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        if (containsSensitiveInfo)
            removeSecureFlag()
    }

    protected fun setSecureFlag() {
        if (shouldPreventContentSharing)
            requireActivity().window.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )
    }

    protected fun removeSecureFlag() {
        if (shouldPreventContentSharing)
            requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
    }

    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? {
        val activity = requireActivity() as MainActivity

        if (activity.tabWasSelected) {
            return if (enter) {
                activity.tabWasSelected = false
                AnimationUtils.loadAnimation(requireContext(), R.anim.nav_default_pop_enter_anim)
            } else {
                AnimationUtils.loadAnimation(requireContext(), R.anim.nav_default_pop_exit_anim)
            }
        }

        return super.onCreateAnimation(transit, enter, nextAnim)
    }
}
