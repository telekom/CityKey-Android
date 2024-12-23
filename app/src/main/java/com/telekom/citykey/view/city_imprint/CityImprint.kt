package com.telekom.citykey.view.city_imprint

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import com.telekom.citykey.R
import com.telekom.citykey.custom.views.OscaAppBarLayout
import com.telekom.citykey.databinding.CityImprintFragmentBinding
import com.telekom.citykey.domain.track.AdjustManager
import com.telekom.citykey.utils.extensions.AccessibilityRole
import com.telekom.citykey.utils.extensions.getColor
import com.telekom.citykey.utils.extensions.loadFromDrawable
import com.telekom.citykey.utils.extensions.loadFromOSCA
import com.telekom.citykey.utils.extensions.openLink
import com.telekom.citykey.utils.extensions.setAccessibilityRole
import com.telekom.citykey.utils.extensions.setAndPerformAccessibilityFocusAction
import com.telekom.citykey.utils.extensions.setVisible
import com.telekom.citykey.utils.extensions.showDialog
import com.telekom.citykey.utils.extensions.startActivity
import com.telekom.citykey.utils.extensions.viewBinding
import com.telekom.citykey.view.city_selection.CitySelectionFragment
import com.telekom.citykey.view.user.login.LoginActivity
import com.telekom.citykey.view.user.profile.ProfileActivity
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class CityImprint : Fragment(R.layout.city_imprint_fragment) {

    private val viewModel: CityImprintViewModel by viewModel()
    private val binding by viewBinding(CityImprintFragmentBinding::bind)
    private val adjustManager: AdjustManager by inject()

    private var isUserLoggedIn = false
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.cityImprintToolbar.setAndPerformAccessibilityFocusAction()
        binding.cityImprintToolbar.inflateMenu(R.menu.home_menu)
        setupToolbar(binding.appBarLayout)
        binding.labelCityServices.setAccessibilityRole(AccessibilityRole.Heading)
        subscribeUi()
    }

    private fun subscribeUi() {
        viewModel.city.observe(viewLifecycleOwner) {
            binding.toolbarCoat.loadFromOSCA(it.municipalCoat)
            binding.toolbarTitle.text = it.cityName
            binding.cityIcon.loadFromOSCA(it.municipalCoat)
            binding.cityLabel.text = it.cityName
            binding.openImprintButton.button.setBackgroundColor(it.cityColorInt)
            binding.openImprintButton.button.background.setTint(it.cityColorInt)
            binding.content.text = it.imprintDesc
            it.imprintLink?.let { link -> binding.openImprintButton.setOnClickListener { openLink(link.trim()) } }
            with(binding.zoomableImage) {
                if (it.imprintImage.isNullOrBlank().not()) {
                    loadFromOSCA(it.imprintImage!!, R.drawable.imprint_top_image)
                } else {
                    loadFromDrawable(R.drawable.imprint_top_image)
                }
            }
        }

        viewModel.user.observe(viewLifecycleOwner) { isUserLoggedIn = it }
    }

    private fun setupToolbar(appBarLayout: OscaAppBarLayout) {

        appBarLayout.onCollapse { collapsed ->
            val menuItemColor = getColor(if (collapsed) R.color.onSurface else R.color.white)

            binding.toolbarCityBox.setVisible(collapsed)

            binding.cityImprintToolbar.menu.forEach {
                it.icon?.setTint(menuItemColor)
            }
        }

        appBarLayout.findViewById<View>(R.id.actionProfile).setOnClickListener {
            if (isUserLoggedIn) {
                adjustManager.trackEvent(R.string.open_profile)
                startActivity<ProfileActivity>()
            } else {
                startActivity(
                    Intent(it.context, LoginActivity::class.java).apply {
                        putExtra(LoginActivity.LAUNCH_PROFILE, true)
                    }
                )
            }
        }
        appBarLayout.findViewById<View>(R.id.actionSelectCity).setOnClickListener {
            CitySelectionFragment()
                .showDialog(requireActivity().supportFragmentManager)
        }
    }
}
