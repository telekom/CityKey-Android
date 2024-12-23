package com.telekom.citykey.view.user.profile.delete_account

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.telekom.citykey.R
import com.telekom.citykey.databinding.ProfileDeleteAccountConfirmationBinding
import com.telekom.citykey.utils.extensions.viewBinding
import com.telekom.citykey.view.user.profile.ProfileActivity
import com.telekom.citykey.view.user.profile.ProfileBackActions

class DeleteAccountConfirmation : Fragment(R.layout.profile_delete_account_confirmation) {
    private val binding by viewBinding(ProfileDeleteAccountConfirmationBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as ProfileActivity).run {
            setPageTitle(R.string.d_003_delete_account_confirmation_title)
            adaptToolbarForClose()
            backAction = ProfileBackActions.FINISH
        }

        binding.okButton.setOnClickListener {
            activity?.finish()
        }
    }
}
