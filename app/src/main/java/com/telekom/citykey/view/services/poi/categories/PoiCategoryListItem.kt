package com.telekom.citykey.view.services.poi.categories

import com.telekom.citykey.models.poi.PoiCategory
import com.telekom.citykey.models.poi.PoiCategoryGroup

sealed class PoiCategoryListItem {
    class Header(val categoryGroup: PoiCategoryGroup) : PoiCategoryListItem()
    class Item(val categoryListItem: PoiCategory, val categoryGroupId: Int, val categoryGroupIcon: String) : PoiCategoryListItem()
}
