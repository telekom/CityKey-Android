package com.telekom.citykey.view.home.events_list

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.paging.PagingData
import androidx.recyclerview.widget.ConcatAdapter
import com.telekom.citykey.R
import com.telekom.citykey.databinding.EventsListFragmentBinding
import com.telekom.citykey.domain.city.CityInteractor
import com.telekom.citykey.domain.track.AdjustManager
import com.telekom.citykey.utils.extensions.*
import com.telekom.citykey.view.MainFragment
import com.telekom.citykey.view.home.events_list.category_filter.CategoryFilter
import com.telekom.citykey.view.home.events_list.date_filter.DateFilter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*

@ExperimentalCoroutinesApi
class EventsList : MainFragment(R.layout.events_list_fragment) {
    private val viewModel: EventsListViewModel by viewModel()
    private val adjustManager: AdjustManager by inject()
    private val binding by viewBinding(EventsListFragmentBinding::bind)

    private var listAdapter: EventsListAdapter? = null
    private var favoritesAdapter: FavoredEventsAdapter? = null
    private var loadStateAdapter: EventsLoadStateAdapter? = null
    private var refreshStateAdapter: EventsLoadStateAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadStateAdapter = EventsLoadStateAdapter { listAdapter?.retry() }
        refreshStateAdapter = EventsLoadStateAdapter { listAdapter?.retry() }

        favoritesAdapter = FavoredEventsAdapter()

        listAdapter = EventsListAdapter().also {
            it.addLoadStateListener { loadStates ->
                viewModel.onLoadStateChanged(loadStates)
                refreshStateAdapter?.loadState = loadStates.refresh
                loadStateAdapter?.loadState = loadStates.append
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        subscribeUi()
    }

    private fun initViews() {
        setupToolbar(binding.toolbarEvents)

        binding.eventsList.adapter = ConcatAdapter(favoritesAdapter, refreshStateAdapter, listAdapter, loadStateAdapter)

        binding.dateFilterBtn.setOnClickListener {
            DateFilter()
                .showDialog(childFragmentManager)
        }
        binding.categoryFilterBtn.setOnClickListener {
            CategoryFilter()
                .showDialog(childFragmentManager)
        }
        binding.categoryFilters.setAccessibilityRole(AccessibilityRole.Button)
        binding.dateFilters.setAccessibilityRole(AccessibilityRole.Button)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun subscribeUi() {

        viewModel.pagingData.observe(viewLifecycleOwner) {
            listAdapter?.submitData(lifecycle, it)
        }

        viewModel.clearLoadedEvents.observe(viewLifecycleOwner) {
            listAdapter?.submitData(lifecycle, PagingData.empty())
        }

        viewModel.activeDateFilter.observe(viewLifecycleOwner) { filter ->
            binding.dateFilters.setTextColor(if (filter?.start == null) getColor(R.color.onSurfaceSecondary) else CityInteractor.cityColorInt)
            binding.dateFilters.setText(R.string.e_002_filter_empty_label)
            filter?.let {
                val calendarStart = it.start?.toCalendar()
                val calendarEnd = it.end?.toCalendar()
                val dateFiltersText = String.format(
                    "%d %s - %d %s",
                    calendarStart?.get(Calendar.DAY_OF_MONTH),
                    calendarStart?.getShortMonthName(),
                    calendarEnd?.get(Calendar.DAY_OF_MONTH),
                    calendarEnd?.getShortMonthName()
                )

                binding.dateFilters.text = dateFiltersText
            }
        }

        viewModel.activeCategoryFilter.observe(viewLifecycleOwner) { filters ->
            binding.categoryFilters.setTextColor(if (filters.isNullOrEmpty()) getColor(R.color.onSurfaceSecondary) else CityInteractor.cityColorInt)

            if (filters.isNullOrEmpty()) {
                binding.categoryFilters.setText(R.string.e_002_filter_empty_label)
            } else {
                binding.categoryFilters.text = filters
                    .filter { it.categoryName.isNullOrBlank().not() }
                    .joinToString(separator = ", ") { it.categoryName!! }
            }
        }

        viewModel.favoredEvents.observe(viewLifecycleOwner) {
            favoritesAdapter?.submitList(it)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        listAdapter = null
        favoritesAdapter = null
        loadStateAdapter = null
        refreshStateAdapter = null
    }
}
