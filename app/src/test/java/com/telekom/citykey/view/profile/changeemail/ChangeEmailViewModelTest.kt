package com.telekom.citykey.view.profile.changeemail

import com.telekom.citykey.InstantTaskExecutorExtension
import com.telekom.citykey.RxImmediateSchedulerExtension
import com.telekom.citykey.common.NetworkException
import com.telekom.citykey.domain.global.GlobalData
import com.telekom.citykey.domain.repository.UserRepository
import com.telekom.citykey.domain.repository.exceptions.InvalidRefreshTokenException
import com.telekom.citykey.domain.repository.exceptions.NoConnectionException
import com.telekom.citykey.models.OscaError
import com.telekom.citykey.models.OscaErrorResponse
import com.telekom.citykey.models.api.requests.EmailChangeRequest
import com.telekom.citykey.view.user.login.LogoutReason
import com.telekom.citykey.view.user.profile.change_email.ChangeEmailViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import io.reactivex.Completable
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(RxImmediateSchedulerExtension::class)
@ExtendWith(InstantTaskExecutorExtension::class)
class ChangeEmailViewModelTest {

    private val repository: UserRepository = mockk(relaxed = true)
    private val globalData: GlobalData = mockk(relaxed = true)
    private lateinit var spyChangeEmailViewModel: ChangeEmailViewModel

    @BeforeEach
    fun setUp() {
        spyChangeEmailViewModel =
            spyk(ChangeEmailViewModel(globalData, repository), recordPrivateCalls = true)
    }

    @Test
    fun onSaveClicked_should_not_change_email() {
        val email = "abcd"
        spyChangeEmailViewModel.onSaveClicked(email)

        verify(exactly = 0) {
            repository.changeEmail(EmailChangeRequest("abc"))
        }
    }

    @Test
    fun onSaveClicked_should_change_email() {
        val email = "abcd@yahoo.com"

        every { repository.changeEmail(any()) } returns Completable.complete()
        spyChangeEmailViewModel.onSaveClicked(email)

        assert(spyChangeEmailViewModel.requestSent.value!!)
    }

    @Test
    fun onSaveClicked_should_not_change_email_thrown_InvalidRefreshTokenException() {
        val email = "abcd@yahoo.com"
        every { repository.changeEmail(any()) } returns Completable.error(InvalidRefreshTokenException(LogoutReason.TECHNICAL_LOGOUT))

        spyChangeEmailViewModel.onSaveClicked(email)
        verify { globalData.logOutUser(LogoutReason.TECHNICAL_LOGOUT) }
    }

    @Test
    fun onSaveClicked_should_not_change_email_thrown_NetworkException() {
        val email = "abcd@yahoo.com"
        every { repository.changeEmail(any()) } returns Completable.error(
            NetworkException(
                0,
                OscaErrorResponse(listOf(OscaError("message", "ec"))),
                "",
                Exception()
            )
        )

        spyChangeEmailViewModel.onSaveClicked(email)

        verify(exactly = 0) { globalData.logOutUser() }
        assert(spyChangeEmailViewModel.technicalError.value == Unit)
    }

    @Test
    fun onConfirmClicked_no_connection() {
        val email = "abcd@yahoo.com"
        every { repository.changeEmail(any()) } returns Completable.error(mockk<NoConnectionException>())

        spyChangeEmailViewModel.onSaveClicked(email)

        verify(exactly = 0) { globalData.logOutUser() }
        assert(spyChangeEmailViewModel.showRetryDialog.value != null)
    }

    @Test
    fun onSaveClicked_should_not_change_email_thrown_exception() {
        val email = "abcd@yahoo.com"
        every { repository.changeEmail(any()) } returns Completable.error(Throwable("Just an error"))

        spyChangeEmailViewModel.onSaveClicked(email)

        verify(exactly = 0) { globalData.logOutUser() }
        assert(spyChangeEmailViewModel.technicalError.value == Unit)
    }
}
