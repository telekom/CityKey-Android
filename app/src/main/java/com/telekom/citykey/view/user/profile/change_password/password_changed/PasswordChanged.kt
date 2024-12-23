package com.telekom.citykey.view.user.profile.change_password.password_changed

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.telekom.citykey.R
import com.telekom.citykey.databinding.ProfilePasswordChangedFragmentBinding
import com.telekom.citykey.utils.extensions.viewBinding
import com.telekom.citykey.view.user.profile.ProfileActivity
import com.telekom.citykey.view.user.profile.ProfileBackActions

class PasswordChanged : Fragment(R.layout.profile_password_changed_fragment) {
    private val binding by viewBinding(ProfilePasswordChangedFragmentBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as ProfileActivity).run {
            adaptToolbarForClose()
            setPageTitle(R.string.p_006_profile_password_changed_title)
            backAction = ProfileBackActions.LOGOUT
        }

        binding.loginButton.setOnClickListener {
            activity?.onBackPressed()
        }
    }
}
