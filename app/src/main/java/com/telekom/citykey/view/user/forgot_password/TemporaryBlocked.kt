package com.telekom.citykey.view.user.forgot_password

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.telekom.citykey.R
import com.telekom.citykey.utils.extensions.AccessibilityRole
import com.telekom.citykey.utils.extensions.setAccessibilityRole
import com.telekom.citykey.view.user.login.LoginActivity

class TemporaryBlocked : Fragment(R.layout.temporary_blocked_fragment) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (activity as LoginActivity).run {
            adaptToolbarForBack()
            setTopIcon(R.drawable.ic_icon_account_locked)
        }

        view.findViewById<View>(R.id.resetPasswordBtn).setOnClickListener {
            findNavController().navigate(
                TemporaryBlockedDirections.actionTemporaryBlockedToForgotPassword2(
                    arguments?.getString("email")
                )
            )
        }
        view.findViewById<View>(R.id.resetPasswordBtn).setAccessibilityRole(AccessibilityRole.Button)
    }
}
