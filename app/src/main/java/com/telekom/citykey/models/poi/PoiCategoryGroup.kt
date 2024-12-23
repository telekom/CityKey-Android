package com.telekom.citykey.models.poi

import android.os.Parcelable
import com.telekom.citykey.R
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
data class PoiCategoryGroup(
    val categoryGroupId: Int,
    val categoryGroupName: String,
    val categoryGroupIcon: String,
    val categoryList: List<PoiCategory>
) : Parcelable {
    private companion object {
        const val ACTIVITIES_ICON = "poi_activities_2x.png"
        const val FAMILY_ICON = "poi_family_2x.png"
        const val KIDS_ICON = "poi_children_2x.png"
        const val CULTURE_ICON = "poi_culture_2x.png"
        const val LIFE_ICON = "poi_life_2x.png"
        const val NATURE_ICON = "poi_nature_2x.png"
        const val INSIDERS_ICON = "poi_insiders_2x.png"
        const val MOBILITY_ICON = "icon-category-mobility-2@2x.png"
        const val SIGHTS_ICON = "icon-category-sights-2x.png"
        const val RECYCLING_ICON = "icon-category-recycling@2x.png"
    }

    val categoryGroupIconId: Int
        get() = when (categoryGroupIcon) {
            ACTIVITIES_ICON -> R.drawable.ic_poi_category_activities
            FAMILY_ICON -> R.drawable.ic_poi_category_family
            KIDS_ICON -> R.drawable.ic_poi_category_children
            CULTURE_ICON -> R.drawable.ic_poi_category_culture
            LIFE_ICON -> R.drawable.ic_poi_category_life
            NATURE_ICON -> R.drawable.ic_poi_category_nature
            INSIDERS_ICON -> R.drawable.ic_poi_category_insiders
            MOBILITY_ICON -> R.drawable.ic_poi_category_mobility2
            SIGHTS_ICON -> R.drawable.ic_poi_category_sights
            RECYCLING_ICON -> R.drawable.ic_poi_category_recycling
            else -> R.drawable.ic_poi_category_other
        }
}

@Parcelize
data class PoiCategory(
    val categoryId: Int,
    val categoryName: String,
    var categoryIcon: String?
) : Parcelable {
    @IgnoredOnParcel
    var categoryGroupId: Int = -1
}
