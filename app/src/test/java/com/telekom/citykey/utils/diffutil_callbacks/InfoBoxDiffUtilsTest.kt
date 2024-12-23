package com.telekom.citykey.utils.diffutil_callbacks

import com.telekom.citykey.models.user.InfoBoxContent
import com.telekom.citykey.view.infobox.InfoboxItem
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test


class InfoBoxDiffUtilsTest {
    @Test
    fun `test areItemsTheSame with same class`() {
        val oldMailItem = mockk<InfoboxItem.Mail>()
        val newMailItem = mockk<InfoboxItem.Mail>()

        val oldInfoBoxContent = listOf(oldMailItem)
        val newInfoBoxContent = listOf(newMailItem)

        val diffUtil = InfoBoxDiffUtils(oldInfoBoxContent, newInfoBoxContent)

        // Check if items are the same based on class
        assertTrue(diffUtil.areItemsTheSame(0, 0))  // Same class
    }

    @Test
    fun `test areItemsTheSame with different class`() {
        val oldMailItem = mockk<InfoboxItem.Mail>()
        val newEmptyItem = InfoboxItem.Empty

        val oldInfoBoxContent = listOf(oldMailItem)
        val newInfoBoxContent = listOf(newEmptyItem)

        val diffUtil = InfoBoxDiffUtils(oldInfoBoxContent, newInfoBoxContent)

        // Check if items are considered different based on class
        assertFalse(diffUtil.areItemsTheSame(0, 0))  // Different class
    }


    @Test
    fun `test areContentsTheSame with different content`() {
        val content1 = mockk<InfoBoxContent> {
            every { headline } returns "Old Headline"
        }
        val content2 = mockk<InfoBoxContent> {
            every { headline } returns "New Headline"
        }

        val oldMailItem = InfoboxItem.Mail(content1)
        val newMailItem = InfoboxItem.Mail(content2)

        val oldInfoBoxContent = listOf(oldMailItem)
        val newInfoBoxContent = listOf(newMailItem)

        val diffUtil = InfoBoxDiffUtils(oldInfoBoxContent, newInfoBoxContent)

        // Check if contents are considered different
        assertFalse(diffUtil.areContentsTheSame(0, 0))  // Different content
    }

    @Test
    fun `test getOldListSize and getNewListSize`() {
        val oldMailItem = mockk<InfoboxItem>()
        val newMailItem = mockk<InfoboxItem>()
        val newEmptyItem = InfoboxItem.Empty

        val oldInfoBoxContent = listOf(oldMailItem, oldMailItem)
        val newInfoBoxContent = listOf(newMailItem, newMailItem, newEmptyItem)

        val diffUtil = InfoBoxDiffUtils(oldInfoBoxContent, newInfoBoxContent)

        // Check the sizes of the lists
        assertEquals(2, diffUtil.oldListSize)
        assertEquals(3, diffUtil.newListSize)
    }
}