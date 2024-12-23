package com.telekom.citykey.view.home.eventsdetails

import com.google.android.gms.maps.model.LatLng
import com.telekom.citykey.InstantTaskExecutorExtension
import com.telekom.citykey.RxImmediateSchedulerExtension
import com.telekom.citykey.domain.city.events.EventsInteractor
import com.telekom.citykey.domain.global.GlobalData
import com.telekom.citykey.domain.location.OscaLocationManager
import com.telekom.citykey.domain.repository.exceptions.InvalidRefreshTokenException
import com.telekom.citykey.domain.repository.exceptions.NoConnectionException
import com.telekom.citykey.domain.track.AdjustManager
import com.telekom.citykey.domain.user.UserState
import com.telekom.citykey.models.content.Event
import com.telekom.citykey.models.content.UserProfile
import com.telekom.citykey.utils.PreferencesHelper
import com.telekom.citykey.view.home.events_details.EventDetailsViewModel
import com.telekom.citykey.view.user.login.LogoutReason
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(RxImmediateSchedulerExtension::class)
@ExtendWith(InstantTaskExecutorExtension::class)
class EventDetailsViewModelTest {

    private lateinit var eventDetailsViewModel: EventDetailsViewModel
    private val locationManager: OscaLocationManager = mockk(relaxed = true)
    private val eventsInteractor: EventsInteractor = mockk(relaxed = true)
    private val globalData: GlobalData = mockk(relaxed = true)
    private val preferencesHelper: PreferencesHelper = mockk(relaxed = true)
    private val userProfile: UserProfile = mockk(relaxed = true)
    private val event: Event = mockk(relaxed = true)
    private val adjustManager: AdjustManager = mockk(relaxed = true)

    @BeforeEach
    fun setUp() {
        val userState = UserState.Present(userProfile)
        every { globalData.user } returns Observable.just(userState)
        eventDetailsViewModel = EventDetailsViewModel(
            locationManager,
            eventsInteractor, globalData, preferencesHelper, adjustManager
        )
        assertEquals(true, eventDetailsViewModel.userLoggedIn.value)
    }

    @Test
    fun `Test onViewCreated if lat, long are not null`() {
        val listOfEvent = arrayListOf(event)
        every { eventsInteractor.favoredEvents } returns Observable.just(listOfEvent)
        every { eventsInteractor.favoritesErrors } returns Observable.just(NoConnectionException())

        eventDetailsViewModel.onViewCreated(
            mockk(relaxed = true) {
                every { latitude } returns 1.0
                every { longitude } returns 1.0
            }
        )
        assertEquals(LatLng(1.0, 1.0), eventDetailsViewModel.latLng.value)
        assertEquals(true, eventDetailsViewModel.favored.value)
        assertEquals(Unit, eventDetailsViewModel.showFavoritesLoadError.value)
    }

    @Test
    fun `Test onViewCreated if localAddress not null or blank`() {
        val listOfEvent = arrayListOf(event)
        every { locationManager.getLatLngFromAddress(any()) } returns Single.just(LatLng(0.0, 0.0))
        every { eventsInteractor.favoredEvents } returns Observable.just(listOfEvent)
        every { eventsInteractor.favoritesErrors } returns Observable.just(InvalidRefreshTokenException(LogoutReason.TECHNICAL_LOGOUT))

        // getLatLngFromAddress
        eventDetailsViewModel.onViewCreated(
            mockk(relaxed = true) {
                every { latitude } returns 0.0
                every { longitude } returns 0.0
                every { locationAddress } returns "Bonn"
            }
        )

        assertEquals(LatLng(0.0, 0.0), eventDetailsViewModel.latLng.value)
        assertEquals(true, eventDetailsViewModel.favored.value)

        // favoritesErrors
        verify { globalData.logOutUser(LogoutReason.TECHNICAL_LOGOUT) }
    }

    @Test
    fun `Test onViewCreated if lat zero`() {
        eventDetailsViewModel.onFavoriteClicked(true, event)
        assertEquals(null, eventDetailsViewModel.latLng.value)
    }

    @Test
    fun `Test onFavoriteClicked success`() {
        every { eventsInteractor.setEventFavored(true, event) } returns Completable.complete()
        eventDetailsViewModel.onFavoriteClicked(true, event)
        assertEquals(true, eventDetailsViewModel.favored.value)
    }

    @Test
    fun testOnError_InvalidRefreshTokenException() {
        val exception = InvalidRefreshTokenException(LogoutReason.ACTIVE_LOGOUT)
        coEvery { eventsInteractor.setEventFavored(true, event) } returns Completable.error(exception)

        eventDetailsViewModel.onFavoriteClicked(true, event)

        verify { eventsInteractor.setEventFavored(true, event) }
        verify { globalData.logOutUser(LogoutReason.ACTIVE_LOGOUT) }
        assertEquals(true, eventDetailsViewModel.promptLoginRequired.value)
    }

}
