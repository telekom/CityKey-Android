package com.telekom.citykey.view.home

import android.graphics.Color
import androidx.lifecycle.Observer
import com.telekom.citykey.InstantTaskExecutorExtension
import com.telekom.citykey.RxImmediateSchedulerExtension
import com.telekom.citykey.domain.city.events.EventsInteractor
import com.telekom.citykey.domain.city.news.NewsInteractor
import com.telekom.citykey.domain.city.weather.WeatherInteractor
import com.telekom.citykey.domain.global.GlobalData
import com.telekom.citykey.domain.user.UserState
import com.telekom.citykey.models.content.CityConfig
import com.telekom.citykey.models.content.UserProfile
import com.telekom.citykey.models.live_data.HomeData
import com.telekom.citykey.utils.PreferencesHelper
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.Completable
import io.reactivex.Observable
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(RxImmediateSchedulerExtension::class)
@ExtendWith(InstantTaskExecutorExtension::class)
class HomeViewModelTest {

    private lateinit var homeViewModel: HomeViewModel
    private val globalData: GlobalData = mockk(relaxed = true)
    private val eventsInteractor: EventsInteractor = mockk(relaxed = true)
    private val newsInteractor: NewsInteractor = mockk(relaxed = true)
    private val weatherInteractor: WeatherInteractor = mockk(relaxed = true)
    private val preferencesHelper: PreferencesHelper = mockk(relaxed = true)
    private val cityConfigMock: CityConfig = mockk(relaxed = true)
    private val userProfile: UserProfile = mockk(relaxed = true)

    @Test
    fun `Test init city details, city events empty list, city selection tooltip`() {
        val viewTypes = mutableListOf<Int>()
        viewTypes.add(HomeViewTypes.VIEW_TYPE_NEWS)
        viewTypes.add(HomeViewTypes.VIEW_TYPE_EVENTS)
        val homeData = HomeData(
            "city", "coat",
            Color.parseColor("color"),
            viewTypes
        )
        val userState = UserState.Present(userProfile)

        every { globalData.city } returns Observable.just(
            mockk(relaxed = true) {
                every { cityName } returns "city"
                every { cityColor } returns "color"
                every { municipalCoat } returns "coat"
                every { cityPicture } returns "path"
                every { cityNightPicture } returns "cityNightPicturePath"
                every { cityConfig } returns cityConfigMock
            }
        )
        every { globalData.user } returns Observable.just(userState)
        every { eventsInteractor.favoredEvents } returns Observable.just(emptyList())
        every { preferencesHelper.getShowedCitySelectionToolTip() } returns true

        var response: Boolean? = null
        val observer = Observer<Boolean> { response = it }

        // observeCityDetails
        homeViewModel = HomeViewModel(
            globalData, eventsInteractor, newsInteractor,
            weatherInteractor, preferencesHelper
        )

        homeViewModel.citySelectionTooltipState.observeForever(observer)
        assertEquals(homeData, homeViewModel.homeData.value)

        // Test User State
        assertEquals(true, homeViewModel.userState.value)

        // Show tooltip
        assertEquals(false, response)

        homeViewModel.citySelectionTooltipState.removeObserver(observer)
    }

    @Test
    fun `Test onCitySelectionClicked`() {
        val userState = UserState.Present(userProfile)

        every { globalData.city } returns Observable.just(
            mockk(relaxed = true) {
                every { cityName } returns "city"
                every { cityColor } returns "color"
                every { municipalCoat } returns "coat"
                every { cityPicture } returns "path"
                every { cityConfig } returns cityConfigMock
            }
        )
        every { globalData.user } returns Observable.just(userState)
        every { eventsInteractor.favoredEvents } returns Observable.just(emptyList())
        every { preferencesHelper.getShowedCitySelectionToolTip() } returns true

        homeViewModel = HomeViewModel(
            globalData, eventsInteractor, newsInteractor,
            weatherInteractor, preferencesHelper
        )

        var response: Boolean? = null
        val observer = Observer<Boolean> { response = it }

        homeViewModel.citySelectionTooltipState.observeForever(observer)
        homeViewModel.onTooltipDismissed()
        verify { preferencesHelper.saveShowedCitySelectionToolTip() }
        assertEquals(false, response)
        homeViewModel.citySelectionTooltipState.removeObserver(observer)
    }

    @Test
    fun `Test onRefresh`() {
        every { globalData.refreshContent() } returns Completable.complete()
        homeViewModel = HomeViewModel(
            globalData, eventsInteractor, newsInteractor,
            weatherInteractor, preferencesHelper
        )
        homeViewModel.onRefresh()
        assertEquals(Unit, homeViewModel.refreshFinished.value)
    }
}
