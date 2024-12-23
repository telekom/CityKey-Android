package com.telekom.citykey.domain.repository.oauth2tokenmanager

import com.telekom.citykey.domain.repository.OAuth2TokenManager
import com.telekom.citykey.domain.repository.SmartCredentialsApi
import com.telekom.citykey.domain.repository.exceptions.InvalidRefreshTokenException
import com.telekom.citykey.domain.security.crypto.Crypto
import com.telekom.citykey.utils.PreferencesHelper
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test


class GetAccessTokenBranchTest {
    private val tokenApi: SmartCredentialsApi = mockk(relaxed = true)
    private val crypto: Crypto = mockk(relaxed = true)
    private lateinit var oAuth2TokenManager: OAuth2TokenManager
    private val prefs: PreferencesHelper = mockk(relaxed = true)

    companion object {
        const val TOKEN_START = "Bearer "
    }

    @BeforeEach
    fun setup() {
        OAuth2TokenManager.keepMeLoggedIn = true
    }

    @Test
    fun `when there is no refresh token, should throw InvalidRefreshTokenException`() {
        oAuth2TokenManager = spyk(OAuth2TokenManager(tokenApi, crypto, prefs))
        every { oAuth2TokenManager.isAccessTokenValid } returns false

        // Assert that InvalidRefreshTokenException is thrown
        assertThrows(InvalidRefreshTokenException::class.java) {
            val receivedToken = oAuth2TokenManager.fetchAccessToken()
            // Verify that updateCredentials is not called
            verify(exactly = 0) { oAuth2TokenManager.updateCredentials(any(), any()) }
            assertEquals("${TOKEN_START}null", receivedToken)
        }
    }
}
