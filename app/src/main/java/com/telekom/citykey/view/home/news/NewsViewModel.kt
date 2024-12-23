package com.telekom.citykey.view.home.news

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.telekom.citykey.domain.city.news.NewsInteractor
import com.telekom.citykey.domain.city.news.NewsState
import com.telekom.citykey.models.content.CityContent
import com.telekom.citykey.view.BaseViewModel

class NewsViewModel(private val newsInteractor: NewsInteractor) : BaseViewModel() {

    val news: LiveData<List<CityContent>> get() = _news

    private val _news: MutableLiveData<List<CityContent>> = MutableLiveData()

    init {
        launch {
            newsInteractor.newsObservable.filter { it is NewsState.Success }
                .map { it as NewsState.Success }
                .map { it.content }
                .subscribe(_news::postValue)
        }
    }
}
