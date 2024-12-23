package com.telekom.citykey.view.infobox

import com.telekom.citykey.InstantTaskExecutorExtension
import com.telekom.citykey.RxImmediateSchedulerExtension
import com.telekom.citykey.domain.global.GlobalData
import com.telekom.citykey.domain.global_messager.GlobalMessages
import com.telekom.citykey.domain.mailbox.MailboxManager
import com.telekom.citykey.domain.notifications.notification_badges.InAppNotificationsInteractor
import com.telekom.citykey.domain.repository.OscaRepository
import com.telekom.citykey.models.user.InfoBoxCategory
import com.telekom.citykey.models.user.InfoBoxContent
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import io.reactivex.Completable
import io.reactivex.Maybe
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.Date

@ExtendWith(RxImmediateSchedulerExtension::class)
@ExtendWith(InstantTaskExecutorExtension::class)
class InfoBoxViewModelTest {


    private lateinit var infoBoxViewModel: InfoBoxViewModel

    private val repository = mockk<OscaRepository>(relaxed = true)
    private val globalData = mockk<GlobalData>(relaxed = true)
    private val inAppUpdates = mockk<InAppNotificationsInteractor>(relaxed = true)
    private val globalMessages = mockk<GlobalMessages>()
    private val mailboxManager = spyk(MailboxManager(globalData, repository, globalMessages, inAppUpdates))

    @BeforeEach
    fun setUp() {
        infoBoxViewModel = InfoBoxViewModel(globalData, mailboxManager)
    }

    @Test
    fun `Test on refresh`() {

        val infoBoxContent = InfoBoxContent(
            attachments = emptyList(),
            buttonAction = "action",
            buttonText = "actionName",
            category = InfoBoxCategory("bbb", "eee"),
            creationDate = Date(),
            description = "ddd",
            details = "aaa",
            headline = "ccc",
            isRead = false,
            userInfoId = 1,
            messageId = 1
        )
        val expectedResult = mutableListOf(infoBoxContent)

        assertThat(infoBoxViewModel.content.value?.first).isNullOrEmpty()
        assertThat(infoBoxViewModel.content.value?.second).isNullOrEmpty()

        every { repository.getMailBox() } returns Maybe.just(expectedResult)

        infoBoxViewModel.onRefresh()

        verify(exactly = 1) { mailboxManager.refreshInfoBox() }

        assertThat(infoBoxViewModel.content.value?.first).isNotEmpty
        assertThat(infoBoxViewModel.content.value?.second).isNotEmpty
        assertThat((infoBoxViewModel.content.value?.second?.map { (it as InfoboxItem.Mail).item })).isEqualTo(
            expectedResult
        )
    }

    @Test
    fun `Test on delete`() {

        val infoBoxContent = InfoBoxContent(
            attachments = emptyList(),
            buttonAction = "action",
            buttonText = "actionName",
            category = InfoBoxCategory("bbb", "eee"),
            creationDate = Date(),
            description = "ddd",
            details = "aaa",
            headline = "ccc",
            isRead = false,
            userInfoId = 1,
            messageId = 1
        )
        val infoBoxContentToDelete = InfoBoxContent(
            attachments = emptyList(),
            buttonAction = "action",
            buttonText = "actionName",
            category = InfoBoxCategory("www", "zzz"),
            creationDate = Date(),
            description = "yyy",
            details = "vvv",
            headline = "xxx",
            isRead = false,
            userInfoId = 2,
            messageId = 2
        )

        val initialList = mutableListOf(infoBoxContent, infoBoxContentToDelete)
        every { repository.getMailBox() } returns Maybe.just(initialList)

        // Initialize the InfoBoxViewModel with content
        mailboxManager.refreshInfoBox()

        every { repository.deleteMail(any(), any()) } returns Completable.complete()
        infoBoxViewModel.onDelete(infoBoxContentToDelete)

        verify(exactly = 1) { mailboxManager.deleteMessage(infoBoxContentToDelete) }

        assertThat(infoBoxViewModel.content.value?.first?.size).isEqualTo(1)
    }

    @Test
    fun `Test on read`() {
        val infoBoxContent = InfoBoxContent(
            attachments = emptyList(),
            buttonAction = "action",
            buttonText = "actionName",
            category = InfoBoxCategory("bbb", "eee"),
            creationDate = Date(),
            description = "ddd",
            details = "aaa",
            headline = "ccc",
            isRead = false,
            userInfoId = 1,
            messageId = 1
        )
        val infoBoxContentToRead = InfoBoxContent(
            attachments = emptyList(),
            buttonAction = "action",
            buttonText = "actionName",
            category = InfoBoxCategory("www", "zzz"),
            creationDate = Date(),
            description = "yyy",
            details = "vvv",
            headline = "xxx",
            isRead = false,
            userInfoId = 2,
            messageId = 2
        )

        val initialList = mutableListOf(infoBoxContent, infoBoxContentToRead)
        every { repository.getMailBox() } returns Maybe.just(initialList)

        // Initialize the InfoBoxViewModel with content
        mailboxManager.refreshInfoBox()

        // Assert that unread content contains 2 messages
        assertThat(infoBoxViewModel.content.value!!.first.size).isEqualTo(2)

        every { repository.setMailRead(any(), any()) } returns Completable.complete()

        // Mark the  message "infoBoxContentToRead" as read
        infoBoxViewModel.onToggleRead(false, 2)

        verify(exactly = 1) { mailboxManager.toggleInfoRead(2, false) }
        verify(exactly = 1) { repository.setMailRead(2, true) }

        // Test that unread content is now containing only one message
        assertThat(infoBoxViewModel.content.value!!.second.size).isEqualTo(1)
        // Test that the remaining unread message has the correct ID
        assertThat((infoBoxViewModel.content.value!!.second[0] as InfoboxItem.Mail).item.userInfoId).isEqualTo(1)
    }

    @Test
    fun `Test on undo deletion`() {

        val infoBoxContent = InfoBoxContent(
            attachments = emptyList(),
            buttonAction = "action",
            buttonText = "actionName",
            category = InfoBoxCategory("bbb", "eee"),
            creationDate = Date(),
            description = "ddd",
            details = "aaa",
            headline = "ccc",
            isRead = false,
            userInfoId = 1,
            messageId = 1
        )
        val infoBoxContentToDelete = InfoBoxContent(
            attachments = emptyList(),
            buttonAction = "action",
            buttonText = "actionName",
            category = InfoBoxCategory("www", "zzz"),
            creationDate = Date(),
            description = "yyy",
            details = "vvv",
            headline = "xxx",
            isRead = false,
            userInfoId = 2,
            messageId = 2
        )

        val initialList = mutableListOf(infoBoxContent, infoBoxContentToDelete)
        every { repository.getMailBox() } returns Maybe.just(initialList)

        // Initialize the InfoBoxViewModel with content
        mailboxManager.refreshInfoBox()

        assertThat(infoBoxViewModel.content.value!!.first.size).isEqualTo(2)

        // Delete the "infoBoxContentToDelete" message
        every { repository.deleteMail(any(), any()) } returns Completable.complete()
        infoBoxViewModel.onDelete(infoBoxContentToDelete)

        // Test that message was deleted and the content is now containing only 1 message
        assertThat(infoBoxViewModel.content.value!!.first.size).isEqualTo(1)
        assertThat((infoBoxViewModel.content.value!!.second[0] as InfoboxItem.Mail).item.userInfoId).isEqualTo(1)

        // Undo the deletion
        infoBoxViewModel.onUndoDeletion()

        // Test that the content is now containing 2 messages and the initial content is restored
        assertThat(infoBoxViewModel.content.value!!.first.size).isEqualTo(2)
        assertThat(infoBoxViewModel.content.value!!.first.map { (it as InfoboxItem.Mail).item }).isEqualTo(initialList)
    }
}
