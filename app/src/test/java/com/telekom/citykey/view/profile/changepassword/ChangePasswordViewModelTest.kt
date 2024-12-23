package com.telekom.citykey.view.profile.changepassword

import com.telekom.citykey.InstantTaskExecutorExtension
import com.telekom.citykey.RxImmediateSchedulerExtension
import com.telekom.citykey.common.NetworkException
import com.telekom.citykey.custom.views.inputfields.FieldValidation
import com.telekom.citykey.domain.global.GlobalData
import com.telekom.citykey.domain.repository.UserRepository
import com.telekom.citykey.domain.repository.exceptions.InvalidRefreshTokenException
import com.telekom.citykey.domain.repository.exceptions.NoConnectionException
import com.telekom.citykey.domain.user.UserState
import com.telekom.citykey.models.OscaError
import com.telekom.citykey.models.OscaErrorResponse
import com.telekom.citykey.view.user.login.LogoutReason
import com.telekom.citykey.view.user.profile.change_password.ChangePasswordViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.Completable
import io.reactivex.subjects.BehaviorSubject
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(RxImmediateSchedulerExtension::class)
@ExtendWith(InstantTaskExecutorExtension::class)
class ChangePasswordViewModelTest {

    private val repository: UserRepository = mockk(relaxed = true)
    private val globalData: GlobalData = mockk(relaxed = true)
    private lateinit var viewModel: ChangePasswordViewModel
    private val userEmailDispatcher: BehaviorSubject<UserState> =
        BehaviorSubject.create()

    @BeforeEach
    fun setUp() {
        every { globalData.user } returns userEmailDispatcher
        viewModel = ChangePasswordViewModel(globalData, repository)

        viewModel.onPasswordHintsResourcesReceived(arrayOf("1", "2", "3"), "Format: 1, 2, 3")
    }

    @Test
    fun onSaveClicked_password_should_chnage() {
        val actualPwd = "bb12345678@"
        val newPwd = "aa123445678@"

        every { repository.changePassword(any()) } returns Completable.complete()
        viewModel.onSaveButtonClicked(actualPwd, newPwd)

        assert(viewModel.saveSuccessful.value!!)
    }

    @Test
    fun error_on_same_email_as_old_password() {
        val password = "hello@smart.city"
        userEmailDispatcher.onNext(dispatchEmail(password))
        viewModel.onPasswordTextChanged(password)
        assert(viewModel.inputValidation.value!!.second.state == FieldValidation.ERROR)
    }

    @Test
    fun error_on_new_password_format_not_correct() {
        val password = "12345@"
        viewModel.onPasswordTextChanged(password)
        assert(viewModel.inputValidation.value!!.second.state == FieldValidation.ERROR)
    }

    @Test
    fun success_on_new_password_format_correct() {
        val password = "bb12345678@"
        viewModel.onPasswordTextChanged(password)
        assert(viewModel.inputValidation.value!!.second.state == FieldValidation.SUCCESS)
    }

    @Test
    fun onSaveClicked_should_not_change_email_thrown_InvalidRefreshTokenException() {
        val actualPwd = "bb12345678@"
        val newPwd = "aa123445678@"
        every { repository.changePassword(any()) } returns Completable.error(InvalidRefreshTokenException(LogoutReason.TECHNICAL_LOGOUT))

        viewModel.onSaveButtonClicked(actualPwd, newPwd)
        verify { globalData.logOutUser(LogoutReason.TECHNICAL_LOGOUT) }
    }

    @Test
    fun onSaveClicked_should_not_change_email_thrown_NetworkException() {
        val actualPwd = "bb12345678@"
        val newPwd = "aa123445678@"
        every { repository.changePassword(any()) } returns Completable.error(
            NetworkException(
                0,
                OscaErrorResponse(listOf(OscaError("message", "ec"))),
                "",
                Exception()
            )
        )

        viewModel.onSaveButtonClicked(actualPwd, newPwd)

        verify(exactly = 0) { globalData.logOutUser() }
        assert(viewModel.technicalError.value == Unit)
    }

    @Test
    fun onConfirmClicked_no_connection() {
        val actualPwd = "bb12345678@"
        val newPwd = "aa123445678@"
        every { repository.changePassword(any()) } returns Completable.error(mockk<NoConnectionException>())

        viewModel.onSaveButtonClicked(actualPwd, newPwd)

        verify(exactly = 0) { globalData.logOutUser() }
        assert(viewModel.showRetryDialog.value != null)
    }

    @Test
    fun onSaveClicked_should_not_change_email_thrown_exception() {
        val actualPwd = "bb12345678@"
        val newPwd = "aa123445678@"
        every { repository.changePassword(any()) } returns Completable.error(Throwable("Just an error"))

        viewModel.onSaveButtonClicked(actualPwd, newPwd)

        verify(exactly = 0) { globalData.logOutUser() }
        assert(viewModel.technicalError.value == Unit)
    }

    private fun dispatchEmail(wantedEmail: String) = UserState.Present(
        mockk {
            every { email } returns wantedEmail
        }
    )
}
