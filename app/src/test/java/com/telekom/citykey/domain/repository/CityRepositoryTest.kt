package com.telekom.citykey.domain.repository

import com.telekom.citykey.InstantTaskExecutorExtension
import com.telekom.citykey.RxImmediateSchedulerExtension
import com.telekom.citykey.common.NetworkException
import com.telekom.citykey.domain.city.news.NewsState
import com.telekom.citykey.models.OscaResponse
import com.telekom.citykey.models.content.CityContent
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.Maybe
import io.reactivex.observers.TestObserver
import io.reactivex.schedulers.TestScheduler
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.util.Date

@ExtendWith(RxImmediateSchedulerExtension::class)
@ExtendWith(InstantTaskExecutorExtension::class)
class CityRepositoryTest {
    val api: SmartCityApi = mockk(relaxed = true)
    private val authApi: SmartCityAuthApi = mockk(relaxed = true)
    private lateinit var cityRepository: CityRepository
    private lateinit var testScheduler: TestScheduler
    private val cityId = 123

    @BeforeEach
    fun setup() {
        cityRepository = CityRepository(api, authApi)
        testScheduler = TestScheduler()

    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `getNews should return Success state with sorted content on success`() {
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
        val newsItem3 = CityContent(
            3,
            Date(3000L),
            3,
            "ContentDetailsText3",
            "ContentTeaser3",
            "ContentSubtitle3",
            "ContentSource3",
            "ContentImage3",
            "ContentType3",
            "ContentCategory3",
            "ImageCredit3",
            "Thumbnail3",
            "ThumbnailCredit3",
            true
        )
        val cityContentList = listOf(newsItem1, newsItem2, newsItem3)
        every { api.getCityContent(cityId = cityId) } returns Maybe.just(
            OscaResponse(
                listOf(
                    newsItem1,
                    newsItem2,
                    newsItem3
                )
            )
        )

        val testObserver = TestObserver<NewsState>()
        cityRepository.getNews(cityId = cityId).subscribe(testObserver)

        testScheduler.triggerActions()

        // Then
        testObserver.assertValue {
            it is NewsState.Success && it.content == cityContentList.sortedByDescending { it1 -> it1.contentCreationDate }
        }
        testObserver.assertComplete()
        testObserver.assertNoErrors()

        verify { api.getCityContent(cityId = cityId) }
    }

    @Test
    fun `getNews should return Error state on API error`() {
        // Given
        val error = Throwable("Network Error")
        every { api.getCityContent(cityId) } returns Maybe.error(error)

        // When
        val testObserver = TestObserver<NewsState>()
        cityRepository.getNews(cityId).subscribe(testObserver)

        // Advance the scheduler to process the Rx chain
        testScheduler.triggerActions()

        // Then
        testObserver.assertError(error)
        testObserver.assertNotComplete()

        verify { api.getCityContent(cityId) }
    }

    @Test
    fun `getNews should handle error response when NetworkException`() {
        val networkException = NetworkException(1, null, "", Throwable())
        every { api.getCityContent(cityId) } throws networkException
        assertThrows<NetworkException> {
            cityRepository.getNews(cityId)
        }
    }
}