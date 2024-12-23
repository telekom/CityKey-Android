package com.telekom.citykey.view.welcome

import android.Manifest
import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.telekom.citykey.R
import com.telekom.citykey.databinding.WelcomeActivityBinding
import com.telekom.citykey.utils.extensions.AccessibilityRole
import com.telekom.citykey.utils.extensions.hasPermission
import com.telekom.citykey.utils.extensions.setAccessibilityRole
import com.telekom.citykey.utils.extensions.showDialog
import com.telekom.citykey.utils.extensions.startActivity
import com.telekom.citykey.utils.extensions.viewBinding
import com.telekom.citykey.view.dialogs.FtuDataPrivacyDialog
import com.telekom.citykey.view.main.MainActivity
import org.koin.android.ext.android.inject

class WelcomeActivity : AppCompatActivity() {
    private val viewModel: WelcomeViewModel by inject()
    private val binding by viewBinding(WelcomeActivityBinding::inflate)

    companion object {
        const val RESULT_CODE_LOGIN_TO_REGISTRATION = 103
        const val RESULT_CODE_REGISTRATION_TO_LOGIN = 104
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

    private fun subscribeUI() {
        viewModel.confirmTrackingTerms.observe(this) { confirmed ->
            if (!confirmed) {
                FtuDataPrivacyDialog()
                    .showDialog(supportFragmentManager)
                checkPermission()
            }

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            setRecentsScreenshotEnabled(false)
        }
        setContentView(binding.root)
        window.statusBarColor = ContextCompat.getColor(this, R.color.black5a)
        binding.skipBtn.setAccessibilityRole(AccessibilityRole.Button)
        binding.skipBtn.setOnClickListener {
            viewModel.onSkipBtnClicked()
            startActivity<MainActivity>()
        }

        subscribeUI()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.welcome_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.actionSkipLogin -> {
                viewModel.onSkipBtnClicked()
                startActivity<MainActivity>()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        finishAffinity()
    }

    @SuppressLint("MissingPermission")
    private fun arePermissionsGranted() =
        this.hasPermission(Manifest.permission.POST_NOTIFICATIONS)

    private fun requestPermission() {
        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    private fun checkPermission() {
        if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.S_V2) {
            if (!arePermissionsGranted()) {
                requestPermission()
            }
        }
    }
}
