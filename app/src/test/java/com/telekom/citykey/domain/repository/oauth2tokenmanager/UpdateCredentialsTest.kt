package com.telekom.citykey.domain.repository.oauth2tokenmanager

import com.telekom.citykey.domain.repository.OAuth2TokenManager
import com.telekom.citykey.domain.repository.SmartCredentialsApi
import com.telekom.citykey.domain.security.crypto.Crypto
import com.telekom.citykey.domain.security.crypto.CryptoKeys
import com.telekom.citykey.models.user.Credentials
import com.telekom.citykey.utils.PreferencesHelper
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test


class UpdateCredentialsTest {

    private val tokenApi: SmartCredentialsApi = mockk(relaxed = true)
    private val crypto: Crypto = mockk(relaxed = true)
    private lateinit var oAuth2TokenManager: OAuth2TokenManager
    private val prefs: PreferencesHelper = mockk(relaxed = true)

    companion object {
        const val TOKEN_START = "Bearer "
    }

    @BeforeEach
    fun setUp() {
        every { crypto.get(CryptoKeys.REFRESH_TOKEN_KEY) } returns "abc"

        oAuth2TokenManager = spyk(OAuth2TokenManager(tokenApi, crypto, prefs))
        every { oAuth2TokenManager.isAccessTokenValid } returns true
    }

    @Test
    fun `When credentials sould not be stored`() {
        oAuth2TokenManager.updateCredentials(createEndCredentials(), false)

        verify(exactly = 1) { crypto.store(any()) }
        verify(exactly = 0) { crypto.remove(any()) }

        val endToken = "$TOKEN_START${createEndCredentials().accessToken}"
        assertEquals(endToken, oAuth2TokenManager.fetchAccessToken())
    }

    @Test
    fun `When credentials should be stored`() {
        oAuth2TokenManager.updateCredentials(createEndCredentials(), true)

        verify(exactly = 2) { crypto.store(any()) }
        verify(exactly = 0) { crypto.remove(any()) }

        val endToken = "$TOKEN_START${createEndCredentials().accessToken}"
        assertEquals(endToken, oAuth2TokenManager.fetchAccessToken())
    }

    private fun createCredentials(prefix: String, save: Boolean): Credentials {
        return Credentials(
            "${prefix}_accessToken", "${prefix}_refreshToken",
            10, 20, 0, save
        )
    }

    private fun createEndCredentials(save: Boolean = false): Credentials {
        return createCredentials("end", save)
    }
}
