package com.telekom.citykey.view.user.profile

import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.WindowManager
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.telekom.citykey.R
import com.telekom.citykey.databinding.ProfileActivityBinding
import com.telekom.citykey.utils.extensions.setAccessibilityRoleForToolbarTitle
import com.telekom.citykey.utils.extensions.shouldPreventContentSharing
import com.telekom.citykey.utils.extensions.viewBinding
import com.telekom.citykey.view.user.login.LoginActivity

class ProfileActivity : AppCompatActivity() {

    private val binding by viewBinding(ProfileActivityBinding::inflate)

    private val toX: AnimatedVectorDrawableCompat by lazy {
        AnimatedVectorDrawableCompat.create(applicationContext, R.drawable.back_to_x)!!
    }
    private val toBack: AnimatedVectorDrawableCompat by lazy {
        AnimatedVectorDrawableCompat.create(applicationContext, R.drawable.x_to_back)!!
    }
    private var isIndicatorCross = true
    var backAction = ProfileBackActions.LOGOUT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            setRecentsScreenshotEnabled(false)
        } else if (shouldPreventContentSharing) {
            window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        }
        setContentView(binding.root)

        window.statusBarColor = ContextCompat.getColor(this, R.color.black5a)
        setupToolbar()
    }

    private fun setupToolbar() {
        binding.toolbarProfile.setNavigationIcon(R.drawable.ic_profile_close)
        binding.toolbarProfile.setNavigationIconTint(getColor(R.color.onSurface))
        binding.toolbarProfile.setNavigationContentDescription(R.string.accessibility_btn_close)
        binding.toolbarProfile.setNavigationOnClickListener { onBackPressed() }
    }

    fun adaptToolbarForClose() {
        if (!isIndicatorCross) {
            binding.toolbarProfile.navigationIcon = toX.apply { start() }
            isIndicatorCross = true
        }
    }

    fun adaptToolbarForBack() {
        if (isIndicatorCross) {
            binding.toolbarProfile.navigationIcon = toBack.apply { start() }
            isIndicatorCross = false
        }
    }

    fun logOut() {
        startActivity(
            Intent(applicationContext, LoginActivity::class.java).apply {
                putExtra(LoginActivity.LAUNCH_PROFILE, true)
            }
        )
        finish()
    }

    fun setPageTitle(@StringRes resId: Int) {
        binding.toolbarProfile.setTitle(resId)
        setAccessibilityRoleForToolbarTitle(binding.toolbarProfile)
    }

    override fun onBackPressed() {
        when (backAction) {
            ProfileBackActions.LOGOUT ->
                logOut()

            ProfileBackActions.BACK ->
                super.onBackPressed()

            ProfileBackActions.POP_TO_PROFILE ->
                findNavController(R.id.profile_nav_host).popBackStack(R.id.profile, false)

            ProfileBackActions.FINISH ->
                finish()
        }
    }

    override fun onDestroy() {
        backAction = ProfileBackActions.BACK
        super.onDestroy()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        finish()
    }
}
