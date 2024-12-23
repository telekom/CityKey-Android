package com.telekom.citykey.view.profile.profile

import com.telekom.citykey.InstantTaskExecutorExtension
import com.telekom.citykey.RxImmediateSchedulerExtension
import com.telekom.citykey.domain.city.available_cities.AvailableCitiesInteractor
import com.telekom.citykey.domain.global.GlobalData
import com.telekom.citykey.domain.user.UserInteractor
import com.telekom.citykey.domain.user.UserState
import com.telekom.citykey.models.content.UserProfile
import com.telekom.citykey.view.user.login.LogoutReason
import com.telekom.citykey.view.user.profile.profile.ProfileViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.Observable
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(RxImmediateSchedulerExtension::class)
@ExtendWith(InstantTaskExecutorExtension::class)
class ProfileViewModelTest {


    private lateinit var profileViewModel: ProfileViewModel
    private val globalData: GlobalData = mockk(relaxed = true)
    private val userInteractor: UserInteractor = mockk(relaxed = true)
    private val availableCitiesInteractor: AvailableCitiesInteractor = mockk(relaxed = true)
    private val userProfile: UserProfile = mockk(relaxed = true)

    @Test
    fun `Test observe Profile if userState Present`() {
        val userState = UserState.Present(userProfile)
        every { userInteractor.user } returns Observable.just(userState)
        profileViewModel = ProfileViewModel(globalData, userInteractor, availableCitiesInteractor)
        assertEquals(UserState.Present(userProfile).profile, profileViewModel.profileContent.value)
    }

    @Test
    fun `Test observe Profile if userState Absent`() {
        val userState = UserState.Absent
        every { userInteractor.user } returns Observable.just(userState)
        profileViewModel = ProfileViewModel(globalData, userInteractor, availableCitiesInteractor)
        assertEquals(Unit, profileViewModel.logOutUser.value)
    }

    @Test
    fun `Test logout button clicked`() {
        profileViewModel = ProfileViewModel(globalData, userInteractor, availableCitiesInteractor)
        profileViewModel.onLogoutBtnClicked()
        verify { userInteractor.logOutUser(LogoutReason.ACTIVE_LOGOUT) }
    }
}
