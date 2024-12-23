package com.telekom.citykey.view.services.poi

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.telekom.citykey.view.services.poi.list.PoiList
import com.telekom.citykey.view.services.poi.map.PoiMap

class PoiPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    private val mapFragment: PoiMap by lazy { PoiMap() }

    private val listFragment: PoiList by lazy { PoiList() }

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment = if (position == 0) mapFragment else listFragment
}
