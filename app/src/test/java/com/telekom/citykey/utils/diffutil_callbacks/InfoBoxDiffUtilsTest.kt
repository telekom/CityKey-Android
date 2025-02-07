/**
 * Copyright (C) 2025 Deutsche Telekom AG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * In accordance with Sections 4 and 6 of the License, the following exclusions apply:
 *
 *  1. Trademarks & Logos – The names, logos, and trademarks of the Licensor are not covered by this License and may not be used without separate permission.
 *  2. Design Rights – Visual identities, UI/UX designs, and other graphical elements remain the property of their respective owners and are not licensed under the Apache License 2.0.
 *  3: Non-Coded Copyrights – Documentation, images, videos, and other non-software materials require separate authorization for use, modification, or distribution.
 *
 * These elements are not considered part of the licensed Work or Derivative Works unless explicitly agreed otherwise. All elements must be altered, removed, or replaced before use or distribution. All rights to these materials are reserved, and Contributor accepts no liability for any infringing use. By using this repository, you agree to indemnify and hold harmless Contributor against any claims, costs, or damages arising from your use of the excluded elements.
 *
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG
 * SPDX-License-Identifier: Apache-2.0
 * License-Filename: LICENSES/Apache-2.0.txt
 */

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