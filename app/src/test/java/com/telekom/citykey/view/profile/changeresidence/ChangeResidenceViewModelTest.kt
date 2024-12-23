package com.telekom.citykey.view.profile.changeresidence

import com.telekom.citykey.InstantTaskExecutorExtension
import com.telekom.citykey.R
import com.telekom.citykey.RxImmediateSchedulerExtension
import com.telekom.citykey.common.NetworkException
import com.telekom.citykey.domain.global.GlobalData
import com.telekom.citykey.domain.repository.UserRepository
import com.telekom.citykey.domain.repository.exceptions.InvalidRefreshTokenException
import com.telekom.citykey.domain.repository.exceptions.NoConnectionException
import com.telekom.citykey.domain.user.UserInteractor
import com.telekom.citykey.models.OscaErrorResponse
import com.telekom.citykey.models.user.ResidenceValidationResponse
import com.telekom.citykey.view.user.login.LogoutReason
import com.telekom.citykey.view.user.profile.change_residence.ChangeResidenceViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.Completable
import io.reactivex.Maybe
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(RxImmediateSchedulerExtension::class)
@ExtendWith(InstantTaskExecutorExtension::class)
class ChangeResidenceViewModelTest {


    private val repository: UserRepository = mockk(relaxed = true)
    private val globalData: GlobalData = mockk(relaxed = true)
    private val userInteractor: UserInteractor = mockk(relaxed = true)
    private lateinit var changeResidenceViewModel: ChangeResidenceViewModel

    @BeforeEach
    fun setUp() {
        changeResidenceViewModel = ChangeResidenceViewModel(
            globalData,
            repository, userInteractor
        )
    }

    @Test
    fun `Test onSaveClicked`() {
        val oscaResponse = mockk<ResidenceValidationResponse>(relaxed = true) {
            every { cityName } returns "Bonn"
            every { homeCityId } returns 0
        }
        every { repository.validatePostalCode("11111") } returns
                Maybe.just(oscaResponse)
        every {
            repository.changePersonalData(
                mockk(relaxed = true),
                "postalCode"
            )
        } returns Completable.complete()

        changeResidenceViewModel.onSaveClicked("11111")
        assertEquals(oscaResponse, changeResidenceViewModel.requestSent.value)
    }

    @Test
    fun `Test onSaveClicked on error throw invalidRefreshTokenException`() {
        every { repository.validatePostalCode("11111") } returns
                Maybe.error(InvalidRefreshTokenException(LogoutReason.TECHNICAL_LOGOUT))
        changeResidenceViewModel.onSaveClicked("11111")
        verify { globalData.logOutUser(LogoutReason.TECHNICAL_LOGOUT) }
        assertEquals(Unit, changeResidenceViewModel.logUserOut.value)
    }

    @Test
    fun `Test onSaveClicked on error throw NoConnectionException`() {
        every { repository.validatePostalCode("11111") } returns
                Maybe.error(mockk<NoConnectionException>(relaxed = true))
        changeResidenceViewModel.onSaveClicked("11111")
        assertEquals("Change_Postcode", changeResidenceViewModel.showRetryDialog.value)
    }

    @Test
    fun `Test onSaveClicked on error throw NetworkException multipe error`() {
        every { repository.validatePostalCode("11111") } returns
                Maybe.error(
                    mockk<NetworkException>(relaxed = true) {
                        every { error } returns OscaErrorResponse(
                            arrayListOf(
                                mockk(relaxed = true) {
                                    every { errorCode } returns "form.validation.error"
                                }
                            )
                        )
                    }
                )
        changeResidenceViewModel.onSaveClicked("11111")
        assertEquals(
            R.string.p_003_profile_email_change_technical_error,
            changeResidenceViewModel.generalErrors.value
        )
    }

    @Test
    fun `Test onSaveClicked on error throw NetworkException postal code error`() {
        every { repository.validatePostalCode("11111") } returns
                Maybe.error(
                    mockk<NetworkException>(relaxed = true) {
                        every { error } returns OscaErrorResponse(
                            arrayListOf(
                                mockk(relaxed = true) {
                                    every { errorCode } returns "postalCode.validation.error"
                                    every { userMsg } returns "userMsg"
                                }
                            )
                        )
                    }
                )
        changeResidenceViewModel.onSaveClicked("11111")
        assertEquals(Pair("userMsg", null), changeResidenceViewModel.onlineErrors.value)
    }

    @Test
    fun `Test onSaveClicked on error throw any other exception`() {
        every { repository.validatePostalCode("11111") } returns
                Maybe.error(mockk<NullPointerException>(relaxed = true))
        changeResidenceViewModel.onSaveClicked("11111")
        assertEquals(
            Unit,
            changeResidenceViewModel.technicalError.value
        )
    }
}
