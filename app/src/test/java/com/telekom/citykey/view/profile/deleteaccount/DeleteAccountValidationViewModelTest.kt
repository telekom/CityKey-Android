package com.telekom.citykey.view.profile.deleteaccount

import com.telekom.citykey.InstantTaskExecutorExtension
import com.telekom.citykey.RxImmediateSchedulerExtension
import com.telekom.citykey.common.ErrorCodes
import com.telekom.citykey.common.NetworkException
import com.telekom.citykey.domain.global.GlobalData
import com.telekom.citykey.domain.repository.OAuth2TokenManager
import com.telekom.citykey.domain.repository.UserRepository
import com.telekom.citykey.domain.repository.exceptions.NoConnectionException
import com.telekom.citykey.models.OscaError
import com.telekom.citykey.models.OscaErrorResponse
import com.telekom.citykey.view.user.profile.delete_account.DeleteAccountValidationViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.Completable
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(RxImmediateSchedulerExtension::class)
@ExtendWith(InstantTaskExecutorExtension::class)
class DeleteAccountValidationViewModelTest {

  


    private val repository: UserRepository = mockk(relaxed = true)
    private val globalData: GlobalData = mockk(relaxed = true)
    private lateinit var viewModel: DeleteAccountValidationViewModel

    @BeforeEach
    fun setUp() {
        viewModel = DeleteAccountValidationViewModel(repository, globalData)
        OAuth2TokenManager.keepMeLoggedIn = true
    }

    @Test
    fun onConfirmClicked_invalidPassword() {
        viewModel.onConfirmClicked("kfkkq.")

        verify(exactly = 0) { repository.login(any(), any()) }
    }

    @Test
    fun onConfirmClicked_invalid_password_online() {
        every { repository.deleteUser(any()) } returns generateError(ErrorCodes.ACCOUNT_WRONG_PASSWORD)

        viewModel.onConfirmClicked("kakdkaksd.")

        assert(viewModel.error.value != null)
        verify(exactly = 1) { repository.deleteUser(any()) }
    }

    @Test
    fun onConfirmClicked_no_connection() {
        every { repository.deleteUser(any()) } returns Completable.error(mockk<NoConnectionException>())

        viewModel.onConfirmClicked("kakdkaksd.")

        assert(viewModel.error.value == null)
        assert(viewModel.showRetryDialog.value != null)
        verify(exactly = 1) { repository.deleteUser(any()) }
    }

    private fun generateError(errorCode: String): Completable {
        return Completable.error(
            NetworkException(
                0,
                OscaErrorResponse(listOf(OscaError("test", errorCode))),
                "",
                Exception()
            )
        )
    }
}
