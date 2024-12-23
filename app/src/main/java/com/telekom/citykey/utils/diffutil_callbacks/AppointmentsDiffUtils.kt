package com.telekom.citykey.utils.diffutil_callbacks

import androidx.recyclerview.widget.DiffUtil
import com.telekom.citykey.models.appointments.Appointment

class AppointmentsDiffUtils(
    private val oldAppointment: List<Appointment>,
    private val newAppointment: List<Appointment>
) : DiffUtil.Callback() {

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
        oldAppointment[oldItemPosition].uuid == newAppointment[newItemPosition].uuid

    override fun getOldListSize() = oldAppointment.size

    override fun getNewListSize() = newAppointment.size

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
        oldAppointment[oldItemPosition] == newAppointment[newItemPosition]
}
