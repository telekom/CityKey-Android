package com.telekom.citykey.models.content

import android.os.Parcelable
import com.telekom.citykey.utils.extensions.isSameDayAs
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class Event(
    val eventId: String? = null,
    val uid: Long,
    val link: String?,
    val title: String?,
    val subtitle: String?,
    val description: String?,
    val thumbnail: String?,
    val image: String?,
    val imageCredit: String?,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val locationName: String?,
    val locationAddress: String?,
    val startDate: Date?,
    val endDate: Date?,
    val hasStartTime: Boolean = false,
    val hasEndTime: Boolean = false,
    val pdf: ArrayList<String>?,
    val cityEventCategories: List<EventCategory> = emptyList(),
    val status: String? = null
) : Parcelable {
    val isSingleDay get() = startDate.isSameDayAs(endDate)

    @IgnoredOnParcel
    var isFavored = false
    val isCancelled: Boolean get() = status.contentEquals("CANCELLED", ignoreCase = true)
    val isPostponed: Boolean get() = status.contentEquals("POSTPONED", ignoreCase = true)
    val isSoldOut: Boolean get() = status.contentEquals("SOLDOUT", ignoreCase = true)
}

@Parcelize
data class EventCategory(val id: Int?, val categoryName: String?) : Parcelable
