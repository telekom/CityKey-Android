package com.telekom.citykey.models.appointments

import android.os.Parcelable
import com.telekom.citykey.utils.extensions.isInPast
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class Appointment(
    val apptId: String,
    var apptStatus: String,
    val attendee: List<Attendee>?,
    val contacts: Contacts,
    val documents: List<String>,
    val createdTime: Date,
    val endTime: Date,
    val location: Location,
    val notes: String,
    val reasons: List<Reason>?,
    val startTime: Date,
    val title: String,
    val uuid: String,
    val waitingNo: String,
    var isRead: Boolean
) : Parcelable {
    companion object {
        const val STATE_PENDING = "Reservierung"
        const val STATE_CONFIRMED = "Bestätigung"
        const val STATE_CANCELED = "Stornierung"
        const val STATE_REJECTED = "Ablehnung"
        const val STATE_UPDATED = "Änderung"
    }

    val isCancellable: Boolean get() = (apptStatus == STATE_CONFIRMED || apptStatus == STATE_UPDATED) && !endTime.isInPast
    val canBeDeleted: Boolean get() = apptStatus == STATE_REJECTED || apptStatus == STATE_CANCELED || endTime.isInPast
}
