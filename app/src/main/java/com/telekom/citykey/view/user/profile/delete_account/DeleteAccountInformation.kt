package com.telekom.citykey.view.user.profile.delete_account

import android.os.Bundle
import android.view.View
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.telekom.citykey.R
import com.telekom.citykey.databinding.ProfileDeleteAccountInformationFragmentBinding
import com.telekom.citykey.utils.extensions.viewBinding
import com.telekom.citykey.view.user.profile.ProfileActivity

class DeleteAccountInformation : Fragment(R.layout.profile_delete_account_information_fragment) {
    private val binding by viewBinding(ProfileDeleteAccountInformationFragmentBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as ProfileActivity).run {
            setPageTitle(R.string.d_001_delete_account_info_title)
            adaptToolbarForBack()
        }
        val deleteInfoText =
            "${getString(R.string.d_001_delete_account_info)}<br/><br/>${getString(R.string.d_001_delete_account_info1)}"
        binding.deleteInfo.text = HtmlCompat.fromHtml(deleteInfoText, HtmlCompat.FROM_HTML_MODE_LEGACY)
        binding.deleteAccountButton.setOnClickListener {
            it.findNavController().navigate(R.id.action_deleteAccountInformation_to_deleteAccountValidation)
        }
    }
}
