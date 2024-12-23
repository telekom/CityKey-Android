package com.telekom.citykey.utils.diffutil_callbacks

import com.telekom.citykey.models.appointments.Appointment
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class AppointmentsDiffUtilsTest {

    @Test
    fun `test areItemsTheSame`() {
        val oldAppointment1 = mockk<Appointment> { every { uuid } returns "1" }
        val oldAppointment2 = mockk<Appointment> { every { uuid } returns "2" }
        val newAppointment1 = mockk<Appointment> { every { uuid } returns "1" }
        val newAppointment2 = mockk<Appointment> { every { uuid } returns "3" }

        val oldAppointments = listOf(oldAppointment1, oldAppointment2)
        val newAppointments = listOf(newAppointment1, newAppointment2)

        val diffUtil = AppointmentsDiffUtils(oldAppointments, newAppointments)

        // Check if items are the same based on UUID
        assertTrue(diffUtil.areItemsTheSame(0, 0))  // Same UUID
        assertFalse(diffUtil.areItemsTheSame(1, 1)) // Different UUID
    }

    @Test
    fun `test areContentsTheSame`() {
        val oldAppointment1 = mockk<Appointment> { every { uuid } returns "1" }
        val newAppointment1 = mockk<Appointment> { every { uuid } returns "1" }
        val oldAppointment2 = mockk<Appointment> { every { uuid } returns "2" }
        val newAppointment2 = mockk<Appointment> { every { uuid } returns "2" }

        every { oldAppointment1.apptId } returns "Appointment 1"
        every { newAppointment1.apptId } returns "Appointment 1 Updated"
        every { oldAppointment2.apptId } returns "Appointment 2"
        every { newAppointment2.apptId } returns "Appointment 2"

        val oldAppointments = listOf(oldAppointment1, oldAppointment2)
        val newAppointments = listOf(newAppointment1, newAppointment2)

        val diffUtil = AppointmentsDiffUtils(oldAppointments, newAppointments)

        // Check if contents are the same based on data
        assertFalse(diffUtil.areContentsTheSame(0, 0))  // Different content
    }

    @Test
    fun `test getOldListSize and getNewListSize`() {
        val oldAppointment1 = mockk<Appointment>()
        val oldAppointment2 = mockk<Appointment>()
        val newAppointment1 = mockk<Appointment>()
        val newAppointment2 = mockk<Appointment>()
        val newAppointment3 = mockk<Appointment>()

        val oldAppointments = listOf(oldAppointment1, oldAppointment2)
        val newAppointments = listOf(newAppointment1, newAppointment2, newAppointment3)

        val diffUtil = AppointmentsDiffUtils(oldAppointments, newAppointments)

        // Check the sizes of the lists
        assertEquals(2, diffUtil.oldListSize)
        assertEquals(3, diffUtil.newListSize)
    }
}