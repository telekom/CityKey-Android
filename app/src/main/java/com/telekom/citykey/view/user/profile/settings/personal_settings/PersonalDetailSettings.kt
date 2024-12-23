package com.telekom.citykey.view.user.profile.settings.personal_settings

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.telekom.citykey.R
import com.telekom.citykey.databinding.PersonalDetailSettingsFragmentBinding
import com.telekom.citykey.utils.extensions.AccessibilityRole
import com.telekom.citykey.utils.extensions.setAccessibilityRole
import com.telekom.citykey.utils.extensions.toDateString
import com.telekom.citykey.utils.extensions.viewBinding
import com.telekom.citykey.view.user.profile.ProfileActivity
import com.telekom.citykey.view.user.profile.ProfileBackActions
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*

class PersonalDetailSettings : Fragment(R.layout.personal_detail_settings_fragment) {

    private val binding by viewBinding(PersonalDetailSettingsFragmentBinding::bind)
    private val viewModel: PersonalDetailSettingsViewModel by viewModel()

    private var postalCode = ""
    private var birthDay: Date? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as ProfileActivity).run {
            setPageTitle(R.string.p_001_profile_label_personal_data)
            adaptToolbarForBack()
            backAction = ProfileBackActions.BACK
        }
        initViews()
        subscribeUi()
    }

    fun initViews() {
        binding.birthDateButton.setOnClickListener {
            findNavController().navigate(
                PersonalDetailSettingsDirections.toChangeBirthday(birthDay)
            )
        }
        binding.residenceButton.setOnClickListener {
            findNavController().navigate(PersonalDetailSettingsDirections.toChangeResidence(postalCode))
        }
        setAccessibilityRole()
    }

    @SuppressLint("SetTextI18n")
    fun subscribeUi() {
        viewModel.userPersonal.observe(viewLifecycleOwner) {
            if (it.dateOfBirth != null)
                binding.birthDateText.text = it.dateOfBirth.toDateString()
            else
                binding.birthDateText.setText(R.string.p_001_profile_no_date_of_birth_added)

            binding.txtResidence.text = "${it.postalCode} ${it.cityName}".trim()
            postalCode = it.postalCode
            birthDay = it.dateOfBirth
        }
    }

    fun setAccessibilityRole() {
        binding.birthDateButton.setAccessibilityRole(AccessibilityRole.Button)
        binding.residenceButton.setAccessibilityRole(AccessibilityRole.Button)
    }
}
