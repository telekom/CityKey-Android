package com.telekom.citykey.view.home.news

import com.telekom.citykey.InstantTaskExecutorExtension
import com.telekom.citykey.RxImmediateSchedulerExtension
import com.telekom.citykey.domain.city.news.NewsInteractor
import com.telekom.citykey.domain.city.news.NewsState
import com.telekom.citykey.models.content.CityContent
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.reactivex.Observable
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.Date

@ExtendWith(RxImmediateSchedulerExtension::class)
@ExtendWith(InstantTaskExecutorExtension::class)
class NewsViewModelTest {

    private lateinit var newsViewModel: NewsViewModel
    private val newsInteractor: NewsInteractor = mockk(relaxed = true)

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `should post success state content to LiveData when interactor emits success state`() {
        val newsItem1 =
            CityContent(
                1,
                Date(1000L),
                1,
                "ContentDetailsText1",
                "ContentTeaser1",
                "ContentSubtitle1",
                "ContentSource1",
                "ContentImage1",
                "ContentType1",
                "ContentCategory1",
                "ImageCredit1",
                "Thumbnail1",
                "ThumbnailCredit1",
                true
            )
        val newsItem2 =
            CityContent(
                2,
                Date(2000L),
                2,
                "ContentDetailsText2",
                "ContentTeaser2",
                "ContentSubtitle2",
                "ContentSource2",
                "ContentImage2",
                "ContentType2",
                "ContentCategory2",
                "ImageCredit2",
                "Thumbnail2",
                "ThumbnailCredit2",
                true
            )
        val mockNewsContent = listOf(newsItem1, newsItem2)

        every { newsInteractor.newsObservable } returns Observable.just(NewsState.Success(mockNewsContent))

        newsViewModel = NewsViewModel(newsInteractor)

        assertEquals(true, newsViewModel.news.value.isNullOrEmpty().not())
        assertEquals(mockNewsContent, newsViewModel.news.value)

    }

    @Test
    fun `should not post error state content to LiveData when interactor emits non-success state`() {
        val errorState = NewsState.Error

        every { newsInteractor.newsObservable } returns Observable.just(errorState)

        newsViewModel = NewsViewModel(newsInteractor)

        assertEquals(true, newsViewModel.news.value.isNullOrEmpty())
    }

    @Test
    fun `should not post error state content to LiveData when interactor emits Loading state`() {
        val errorState = NewsState.Loading

        every { newsInteractor.newsObservable } returns Observable.just(errorState)

        newsViewModel = NewsViewModel(newsInteractor)
        assertEquals(true, newsViewModel.news.value.isNullOrEmpty())

    }

    @Test
    fun `should not post error state content to LiveData when interactor emits ActionError state`() {
        val errorState = NewsState.ActionError

        every { newsInteractor.newsObservable } returns Observable.just(errorState)

        newsViewModel = NewsViewModel(newsInteractor)
        assertEquals(true, newsViewModel.news.value.isNullOrEmpty())

    }
}
