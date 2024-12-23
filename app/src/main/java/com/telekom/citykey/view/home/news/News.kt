package com.telekom.citykey.view.home.news

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.telekom.citykey.R
import com.telekom.citykey.databinding.HomeNewsFragmentBinding
import com.telekom.citykey.utils.extensions.viewBinding
import com.telekom.citykey.view.MainFragment
import org.koin.android.ext.android.inject

class News : MainFragment(R.layout.home_news_fragment) {
    private val viewModel: NewsViewModel by inject()
    private val binding by viewBinding(HomeNewsFragmentBinding::bind)

    private var newsAdapter: NewsAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        newsAdapter = NewsAdapter()

        with(binding.newsList) {
            layoutManager = LinearLayoutManager(context)
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
            adapter = newsAdapter
        }
        setupToolbar(binding.toolbarNews)
        subscribeUi()
    }

    private fun subscribeUi() {
        viewModel.news.observe(viewLifecycleOwner) {
            newsAdapter?.submitList(it)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        newsAdapter = null
    }
}
