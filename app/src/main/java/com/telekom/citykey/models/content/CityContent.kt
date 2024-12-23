package com.telekom.citykey.models.content

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class CityContent(
    val contentId: Int,
    val contentCreationDate: Date,
    val uid: Int? = null,
    val contentDetails: String?,
    val contentTeaser: String?,
    val contentSubtitle: String?,
    val contentSource: String?,
    val contentImage: String?,
    val contentTyp: String?,
    val contentCategory: String?,
    val imageCredit: String?,
    val thumbnail: String?,
    val thumbnailCredit: String?,
    val sticky: Boolean,
) : Parcelable
