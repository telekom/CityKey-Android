package com.telekom.citykey.view.welcome

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.telekom.citykey.R
import com.telekom.citykey.databinding.WelcomePageFragmentBinding
import com.telekom.citykey.utils.extensions.AccessibilityRole
import com.telekom.citykey.utils.extensions.setAccessibilityRole
import com.telekom.citykey.utils.extensions.startActivity
import com.telekom.citykey.utils.extensions.viewBinding
import com.telekom.citykey.view.main.MainActivity
import com.telekom.citykey.view.user.login.LoginActivity
import com.telekom.citykey.view.user.registration.RegistrationActivity

class Welcome : Fragment(R.layout.welcome_page_fragment) {
    private val binding by viewBinding(WelcomePageFragmentBinding::bind)

    companion object {
        const val RESULT_CODE_LOGIN_TO_REGISTRATION = 103
        const val RESULT_CODE_REGISTRATION_TO_LOGIN = 104
    }

    private val loginARL: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            when (it.resultCode) {
                RESULT_CODE_LOGIN_TO_REGISTRATION -> {
                    registerARL.launch(
                        Intent(activity, RegistrationActivity::class.java).apply {
                            putExtra("isFirstTime", true)
                            putExtra("isLaunchedByLogin", true)
                        }
                    )
                }

                Activity.RESULT_OK -> {
                    requireActivity().finish()
                    startActivity<MainActivity>()
                }
            }
        }

    private val registerARL: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_CODE_REGISTRATION_TO_LOGIN)
                loginARL.launch(
                    Intent(activity, LoginActivity::class.java).apply {
                        putExtra("isFirstTime", true)
                    }
                )
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    fun initViews() {
        binding.loginLink.setAccessibilityRole(AccessibilityRole.Button)
        binding.descHeading.setAccessibilityRole(AccessibilityRole.Heading)
        binding.loginLink.setOnClickListener {
            loginARL.launch(
                Intent(activity, LoginActivity::class.java).apply {
                    putExtra("isFirstTime", true)
                }
            )
        }

        binding.registerBtn.setOnClickListener {
            registerARL.launch(
                Intent(activity, RegistrationActivity::class.java).apply {
                    putExtra("isFirstTime", true)
                }
            )
        }
    }
}
