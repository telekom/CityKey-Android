<!--
SPDX-FileCopyrightText: 2025 Deutsche Telekom AG
SPDX-License-Identifier: CC-BY-4.0
License-Filename: LICENSES/CC-BY-4.0.txt
-->

# Citykey API integration

## Service / API
1. Citykey uses Retrofit & Koin to support the networking
2. Each API is a part of some or other service (e.g. Waste Calendar, Digital Administration etc.)
3. To add a new service, we need to add service's APIs and link them properly using Repository + ViewModel pattern

## Adding a new Service / API

To add a new Service (API) into Citykey, follow these steps:

1. **Put the API method**: Either put your API method into existing [`SmartCityApi`](https://github.com/telekom/CityKey-Android/blob/main/app/src/main/java/com/telekom/citykey/domain/repository/SmartCityApi.kt) interface, or create your own API interface under [`com.telekom.citykey.domain.repository`](https://github.com/telekom/CityKey-Android/tree/main/app/src/main/java/com/telekom/citykey/domain/repository) package.
2. **Actualise the API**: Actualise the API method in the [`SmartCityApiMockImpl`](https://github.com/telekom/CityKey-Android/blob/main/app/src/main/java/com/telekom/citykey/domain/mock/SmartCityApiMockImpl.kt) class, or if you have created a new interface, you can write your own implementation under the service's domain package.
3. **Link the API via Koin**: Provide the API (make ready to inject) by specifying the dependency in [`remote_datasource_module`](https://github.com/telekom/CityKey-Android/blob/main/app/src/main/java/com/telekom/citykey/di/RemoteDataSourceModule.kt). You don't have to do this if you have put your method into the existing [`SmartCityApi`](https://github.com/telekom/CityKey-Android/blob/main/app/src/main/java/com/telekom/citykey/domain/repository/SmartCityApi.kt).
4. **Usage of the API**: Use the API by injecting it into the relevant Repository which serves for the service you're trying to add.
5. **Call the API from the ViewModel**: Inject the repository into your ViewModel, then you can call the relevant API from there.
