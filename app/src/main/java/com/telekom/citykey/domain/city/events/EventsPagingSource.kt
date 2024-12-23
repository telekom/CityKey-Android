package com.telekom.citykey.domain.city.events

import androidx.paging.PagingState
import androidx.paging.rxjava2.RxPagingSource
import com.telekom.citykey.custom.views.calendar.DateSelection
import com.telekom.citykey.domain.global.GlobalData
import com.telekom.citykey.domain.repository.CityRepository
import com.telekom.citykey.models.content.Event
import com.telekom.citykey.utils.extensions.toApiFormat
import io.reactivex.Single

class EventsPagingSource(
    private val cityRepository: CityRepository,
    private val globalData: GlobalData,
    private var selectedDates: DateSelection? = null,
    private var categoryFilters: ArrayList<Int>? = null
) : RxPagingSource<Int, Event>() {

    override val keyReuseSupported: Boolean
        get() = true

    override fun getRefreshKey(state: PagingState<Int, Event>): Int = 1

    override fun loadSingle(params: LoadParams<Int>): Single<LoadResult<Int, Event>> {
        val key = params.key ?: 1

        return cityRepository.getEvents(
            cityId = globalData.currentCityId,
            start = selectedDates?.start?.toApiFormat(),
            end = selectedDates?.end?.toApiFormat(),
            pageNo = key,
            pageSize = params.loadSize,
            categories = categoryFilters
        )
            .map { toLoadResult(it, params) }
            .onErrorReturn { LoadResult.Error(it) }
    }

    private fun toLoadResult(data: List<Event>, params: LoadParams<Int>): LoadResult<Int, Event> {
        val currentKey = params.key ?: 1

        return if (data.isEmpty() && currentKey == 1)
            LoadResult.Error(NoEventsException())
        else
            LoadResult.Page(
                data = data,
                prevKey = null,
                nextKey = if (data.size < params.loadSize) null else currentKey + 1
            )
    }
}
