/**
 * Copyright (C) 2025 Deutsche Telekom AG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * In accordance with Sections 4 and 6 of the License, the following exclusions apply:
 *
 *  1. Trademarks & Logos – The names, logos, and trademarks of the Licensor are not covered by this License and may not be used without separate permission.
 *  2. Design Rights – Visual identities, UI/UX designs, and other graphical elements remain the property of their respective owners and are not licensed under the Apache License 2.0.
 *  3: Non-Coded Copyrights – Documentation, images, videos, and other non-software materials require separate authorization for use, modification, or distribution.
 *
 * These elements are not considered part of the licensed Work or Derivative Works unless explicitly agreed otherwise. All elements must be altered, removed, or replaced before use or distribution. All rights to these materials are reserved, and Contributor accepts no liability for any infringing use. By using this repository, you agree to indemnify and hold harmless Contributor against any claims, costs, or damages arising from your use of the excluded elements.
 *
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG
 * SPDX-License-Identifier: Apache-2.0 AND LicenseRef-Deutsche-Telekom-Brand
 * License-Filename: LICENSES/Apache-2.0.txt LICENSES/LicenseRef-Deutsche-Telekom-Brand.txt
 */

@file:Suppress("MatchingDeclarationName")

package com.telekom.citykey.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.telekom.citykey.BuildConfig
import com.telekom.citykey.domain.mock.CitykeyWidgetApiMockImpl
import com.telekom.citykey.domain.mock.CitykeyWidgetAuthApiMockImpl
import com.telekom.citykey.domain.mock.AssetResponseMocker
import com.telekom.citykey.domain.mock.SmartCityApiMockImpl
import com.telekom.citykey.domain.mock.SmartCityAuthApiMockImpl
import com.telekom.citykey.domain.mock.SmartCityTpnsApiMockImpl
import com.telekom.citykey.domain.mock.SmartCredentialsApiMockImpl
import com.telekom.citykey.domain.repository.*
import com.telekom.citykey.domain.repository.interceptors.AppConfigInterceptor
import com.telekom.citykey.domain.repository.interceptors.ConnectivityInterceptor
import com.telekom.citykey.domain.repository.interceptors.OAuth2Interceptor
import com.telekom.citykey.domain.repository.interceptors.UserInterceptor
import com.telekom.citykey.models.api.contracts.CitykeyWidgetApi
import com.telekom.citykey.models.api.contracts.CitykeyWidgetAuthApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.net.ssl.HttpsURLConnection

val remote_datasource_module = module {

    single(named("auth")) { createOkHttpClient(get()) }
    single(named("clean")) { createCleanOkHttpClient(get()) }
    single(named("tpns")) { createOkHttpClientTpns() }

    single { AssetResponseMocker(get(), createGsonDateFormater()) }

    single<SmartCityApi> { SmartCityApiMockImpl(get()) }.bind<SmartCityApi>()
    single<SmartCityAuthApi> { SmartCityAuthApiMockImpl(get()) }.bind<SmartCityAuthApi>()
    single<SmartCityTpnsApi> { SmartCityTpnsApiMockImpl() }.bind<SmartCityTpnsApi>()
    single<SmartCredentialsApi> { SmartCredentialsApiMockImpl(get()) }.bind<SmartCredentialsApi>()
    single<CitykeyWidgetApi> { CitykeyWidgetApiMockImpl(get()) }.bind<CitykeyWidgetApi>()
    single<CitykeyWidgetAuthApi> { CitykeyWidgetAuthApiMockImpl(get()) }.bind<CitykeyWidgetAuthApi>()

    single { OscaRepository(get(), get()) }
    single { ServicesRepository(get(), get()) }
    single { UserRepository(get(), get()) }
    single { CityRepository(get(), get()) }
    single { WidgetRepository(get(), get(), get()) }
    single { OAuth2Interceptor(get(), get()) }
    factory { TpnsRepository(get()) }
    factory { UserInterceptor(get(), get()) }
}

private const val TIMEOUT = 30L

fun createOkHttpClientTpns(): OkHttpClient {
    val httpLoggingInterceptor = HttpLoggingInterceptor { Timber.tag("TPNS").i(it) }
    httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

    return OkHttpClient.Builder()
        .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
        .readTimeout(TIMEOUT, TimeUnit.SECONDS)
        .addInterceptor(ConnectivityInterceptor())
        .addInterceptor(httpLoggingInterceptor)
        .hostnameVerifier { _, session ->
            HttpsURLConnection.getDefaultHostnameVerifier()
                .verify(BuildConfig.TPNS_HOST_NAME, session)
        }
        .build()
}

fun createOkHttpClient(oAuth2Interceptor: OAuth2Interceptor): OkHttpClient {
    val httpLoggingInterceptor = HttpLoggingInterceptor { Timber.tag("HTTP").i(it) }
    httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

    return OkHttpClient.Builder()
        .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
        .readTimeout(TIMEOUT, TimeUnit.SECONDS)
        .addInterceptor(AppConfigInterceptor())
        .addInterceptor(ConnectivityInterceptor())
        .addInterceptor(oAuth2Interceptor)
        .addInterceptor(httpLoggingInterceptor)
        .hostnameVerifier { _, session ->
            HttpsURLConnection.getDefaultHostnameVerifier()
                .verify(BuildConfig.HOST_NAME, session)
        }
        .build()
}

fun createCleanOkHttpClient(userInterceptor: UserInterceptor): OkHttpClient {
    val httpLoggingInterceptor = HttpLoggingInterceptor { Timber.tag("HTTP").i(it) }
    httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

    return OkHttpClient.Builder()
        .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
        .readTimeout(TIMEOUT, TimeUnit.SECONDS)
        .addInterceptor(AppConfigInterceptor())
        .addInterceptor(ConnectivityInterceptor())
        .addInterceptor(userInterceptor)
        .addInterceptor(httpLoggingInterceptor)
        .hostnameVerifier { _, session ->
            HttpsURLConnection.getDefaultHostnameVerifier()
                .verify(BuildConfig.HOST_NAME, session)
        }
        .build()
}

fun createGsonDateFormater(): Gson {
    return GsonBuilder()
        .setDateFormat("yyyy-MM-dd HH:mm:ss")
        .create()
}

inline fun <reified T> createWebService(okHttpClient: OkHttpClient, url: String): T =
    Retrofit.Builder()
        .baseUrl(url)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(createGsonDateFormater()))
        .addCallAdapterFactory(RxCallAdapterWrapperFactory.create())
        .build()
        .create(T::class.java)
