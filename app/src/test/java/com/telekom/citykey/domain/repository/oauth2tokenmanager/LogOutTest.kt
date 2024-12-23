package com.telekom.citykey.domain.repository.oauth2tokenmanager

import com.telekom.citykey.domain.repository.OAuth2TokenManager
import com.telekom.citykey.domain.repository.SmartCredentialsApi
import com.telekom.citykey.domain.repository.exceptions.InvalidRefreshTokenException
import com.telekom.citykey.domain.security.crypto.Crypto
import com.telekom.citykey.models.user.Credentials
import com.telekom.citykey.utils.PreferencesHelper
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.Single
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import retrofit2.HttpException

class LogOutTest {

    private val tokenApi: SmartCredentialsApi = mockk(relaxed = true)
    private val crypto: Crypto = mockk(relaxed = true)
    private lateinit var oAuth2TokenManager: OAuth2TokenManager
    private val prefs: PreferencesHelper = mockk(relaxed = true)

    @BeforeEach
    fun setUp() {
        // since null would or might lead to empty tokens even without logout
        oAuth2TokenManager = OAuth2TokenManager(tokenApi, crypto, prefs)
        oAuth2TokenManager.updateCredentials(createCredentials(), true)
    }

    @Test
    fun `When user should be logged out`() {
        every { tokenApi.getNewToken(any()) } returns Single.error(
            mockk<HttpException>(relaxed = true) {
                every { code() } returns 400
            }
        )
        assertThrows(InvalidRefreshTokenException::class.java) {
            oAuth2TokenManager.fetchAccessToken()
        }

        verify { crypto.remove(any()) }
    }

    private fun createCredentials(): Credentials {
        return Credentials(
            "", "def",
            10, 20, 0, false
        )
    }
}
