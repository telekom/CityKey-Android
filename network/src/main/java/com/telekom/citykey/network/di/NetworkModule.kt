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

package com.telekom.citykey.network.di

import com.telekom.citykey.network.api.createGsonDateFormater
import com.telekom.citykey.network.impl.CitykeyAPIClientImpl
import com.telekom.citykey.network.impl.CitykeyAuthAPIClientImpl
import com.telekom.citykey.network.impl.CitykeyCredentialsAPIClientImpl
import com.telekom.citykey.network.impl.CitykeyTpnsAPIClientImpl
import com.telekom.citykey.network.impl.CitykeyWidgetAPIClientImpl
import com.telekom.citykey.network.mock.AssetResponseMocker
import com.telekom.citykey.networkinterface.client.CitykeyAPIClient
import com.telekom.citykey.networkinterface.client.CitykeyAuthAPIClient
import com.telekom.citykey.networkinterface.client.CitykeyCredentialsAPIClient
import com.telekom.citykey.networkinterface.client.CitykeyTpnsAPIClient
import com.telekom.citykey.networkinterface.client.CitykeyWidgetAPIClient
import com.telekom.citykey.networkinterface.client.CitykeyWidgetAuthAPIClient
import org.koin.dsl.bind
import org.koin.dsl.module

private val assetMockerModule = module {
    single { AssetResponseMocker(get(), createGsonDateFormater()) }
}

/**
 * Koin module providing TPNS API interface and implementation.
 *
 * Provides:
 * - A `CitykeyTPNSApi` instance using the "tpns" OkHttpClient and TPNS server URL.
 * - A `CitykeyTpnsAPIInterface` implementation bound to `CitykeyTpnsAPIInterface`.
 */
private val tpnsModule = module {

    single<CitykeyTpnsAPIClient> {
        CitykeyTpnsAPIClientImpl(get())
    }.bind<CitykeyTpnsAPIClient>()
}

/**
 * Koin module providing Widget API interface and implementation.
 *
 * Provides:
 * - A `CitykeyWidgetApi` instance using the "clean" OkHttpClient and server URL.
 * - A `CitykeyWidgetAPIInterface` implementation bound to `CitykeyWidgetAPIInterface`.
 */
private val widgetModule = module {

    single<CitykeyWidgetAPIClient> {
        CitykeyWidgetAPIClientImpl(get())
    }.bind<CitykeyWidgetAPIClient>()

    single<CitykeyWidgetAuthAPIClient> {
        com.telekom.citykey.network.impl.CitykeyWidgetAuthAPIClientImpl(get())
    }.bind<CitykeyWidgetAuthAPIClient>()
}

/**
 * Koin module providing Credentials API interface and implementation.
 *
 * Provides:
 * - A `CitykeyCredentialsApi` instance using the "clean" OkHttpClient and server URL.
 * - A `CitykeyCredentialsAPIInterface` implementation bound to `CitykeyCredentialsAPIInterface`.
 */
private val credentialsModule = module {

    single<CitykeyCredentialsAPIClient> {
        CitykeyCredentialsAPIClientImpl(get())
    }.bind<CitykeyCredentialsAPIClient>()
}

/**
 * Koin module providing Citykey API interfaces and implementations.
 *
 * Provides:
 * - A `CitykeyApi` instance using the "clean" OkHttpClient and server URL.
 * - A `CitykeyAuthApi` instance using the "auth" OkHttpClient and server URL.
 */
private val citykeyAPIModule = module {

    single<CitykeyAPIClient> {
        CitykeyAPIClientImpl(get())
    }.bind<CitykeyAPIClient>()

    single<CitykeyAuthAPIClient> {
        CitykeyAuthAPIClientImpl(get())
    }.bind<CitykeyAuthAPIClient>()
}

val networkModule = listOf(
    assetMockerModule,
    tpnsModule,
    widgetModule,
    credentialsModule,
    citykeyAPIModule
)
