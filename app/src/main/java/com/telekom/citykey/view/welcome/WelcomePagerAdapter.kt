package com.telekom.citykey.view.welcome

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.telekom.citykey.R
import com.telekom.citykey.databinding.WelcomeGuideFragmentBinding
import com.telekom.citykey.models.welcome.WelcomePageItem
import com.telekom.citykey.utils.extensions.inflateChild

class WelcomePagerAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val pagesData: List<WelcomePageItem> = arrayListOf(
        WelcomePageItem(
            R.drawable.bg_signin_01,
            R.string.x_001_welcome_info_01,
            R.string.x_001_welcome_info_title_01
        ),
        WelcomePageItem(
            R.drawable.bg_signin_02,
            R.string.x_001_welcome_info_02,
            R.string.x_001_welcome_info_title_02
        ),
        WelcomePageItem(
            R.drawable.bg_signin_03,
            R.string.x_001_welcome_info_03,
            R.string.x_001_welcome_info_title_03
        ),
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        WelcomePageViewHolder(WelcomeGuideFragmentBinding.bind(parent.inflateChild(R.layout.welcome_guide_fragment)))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as WelcomePageViewHolder).setPageData(pagesData[position])
    }

    override fun getItemCount(): Int = pagesData.size

    private inner class WelcomePageViewHolder(val binding: WelcomeGuideFragmentBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun setPageData(pageData: WelcomePageItem) {
            binding.welcomeImage.setImageResource(pageData.imgRes)
            binding.descHeading.setText(pageData.title)
            binding.description.setText(pageData.description)
        }
    }
}
