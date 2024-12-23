package com.telekom.citykey.domain.legaldata

import com.google.gson.Gson
import com.telekom.citykey.InstantTaskExecutorExtension
import com.telekom.citykey.RxImmediateSchedulerExtension
import com.telekom.citykey.domain.legal_data.LegalDataManager
import com.telekom.citykey.domain.repository.OscaRepository
import com.telekom.citykey.models.content.Terms
import com.telekom.citykey.utils.PreferencesHelper
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.Maybe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith


@ExtendWith(RxImmediateSchedulerExtension::class)
@ExtendWith(InstantTaskExecutorExtension::class)
class LegalDataManagerTest {

    private val oscaRepository: OscaRepository = mockk(relaxed = true)
    private val preferences: PreferencesHelper = mockk(relaxed = true)
    private lateinit var legalDataManager: LegalDataManager

    @BeforeEach
    fun setUp() {
        legalDataManager = LegalDataManager(oscaRepository, preferences)
    }

    @Test
    fun `Data is missing so we need to get it - success`() {
        every { preferences.legalData } returns ""
        every { oscaRepository.getLegalData() } returns Maybe.just(mockk())

        legalDataManager.loadLegalData()
            .test()
            .assertNoErrors()
            .dispose()
    }

    private class ErrorException : Exception()

    @Test
    fun `Data is missing so we need to get it - error`() {
        every { preferences.legalData } returns ""
        every { oscaRepository.getLegalData() } returns Maybe.error(ErrorException())

        legalDataManager.loadLegalData()
            .test()
            .assertError(ErrorException::class.java)
            .dispose()
    }

    @Test
    fun `Data is there so we try to update`() {
        every { preferences.legalData } returns Gson().toJson(mockk<Terms>())
        every { oscaRepository.getLegalData() } returns Maybe.error(ErrorException())

        legalDataManager.loadLegalData()
            .test()
            .assertNoErrors()
            .assertOf { verify { oscaRepository.getLegalData() } }
            .dispose()
    }
}
