package com.telekom.citykey.view.home.eventslist.categoryfilter

import com.telekom.citykey.InstantTaskExecutorExtension
import com.telekom.citykey.RxImmediateSchedulerExtension
import com.telekom.citykey.domain.city.events.EventsInteractor
import com.telekom.citykey.domain.global.GlobalData
import com.telekom.citykey.domain.repository.CityRepository
import com.telekom.citykey.models.content.City
import com.telekom.citykey.models.content.EventCategory
import com.telekom.citykey.view.home.events_list.category_filter.CategoryFilterViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import io.reactivex.Maybe
import io.reactivex.subjects.BehaviorSubject
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(RxImmediateSchedulerExtension::class)
@ExtendWith(InstantTaskExecutorExtension::class)
class CategoryFilterViewModelTest {

    private val city: City = mockk(relaxed = true)
    private val cityRepository: CityRepository = mockk(relaxed = true)
    private val globalData: GlobalData = mockk(relaxed = true)
    private val eventsInteractor: EventsInteractor = mockk(relaxed = true)
    private lateinit var spyCategoryFilterViewModel: CategoryFilterViewModel
    private val eventCategories = listOf(
        EventCategory(1, "Lesen"),
        EventCategory(2, "Sport"),
        EventCategory(3, "Natur")
    )

    @BeforeEach
    fun setUp() {
        val cityColor = "#2FADED"
        every { city.cityColor } returns cityColor
        every { globalData.city } returns BehaviorSubject.createDefault(city)

        every { cityRepository.getAllEventCategories(any()) } returns
                Maybe.just(eventCategories)

        spyCategoryFilterViewModel = spyk(
            CategoryFilterViewModel(globalData, cityRepository, eventsInteractor),
            recordPrivateCalls = true
        )
    }

    @Test
    fun init_should_succeed() {
        assert(spyCategoryFilterViewModel.allCategories.value == eventCategories)
        assert(spyCategoryFilterViewModel.filters.value != null)
    }

    @Test
    fun revokeFiltering() {
        spyCategoryFilterViewModel.revokeFiltering()
        verify { eventsInteractor.revokeEventsCount() }
    }

    @Test
    fun confirmFiltering() {
        spyCategoryFilterViewModel.confirmFiltering()

        verify { eventsInteractor.updateCategories(arrayListOf(), arrayListOf()) }
        verify { eventsInteractor.applyFilters() }
    }
}
