<!--
SPDX-FileCopyrightText: 2025 Deutsche Telekom AG
SPDX-License-Identifier: CC-BY-4.0
License-Filename: LICENSES/CC-BY-4.0.txt
-->

# Citykey API Integration Guide

## Core Architecture

1. Citykey uses Retrofit & Koin to support the networking
2. Each API is a part of some or other service (e.g. Waste Calendar, Digital Administration etc.)
3. To add a new service, we need to add service's APIs and link them properly using Repository +
   ViewModel pattern

## Understanding API Structure

The Citykey app provides various city services through a modular architecture where each service is
defined in a JSON response. This documentation will help you understand the API structure and how to
integrate new services into the app.

### API Response

**Base Response Format**:

[
`OscaResponse`](https://github.com/telekom/CityKey-Android/blob/main/app/src/main/java/com/telekom/citykey/models/OscaResponse.kt)
is created to serve the purpose of "base" response.

```sh
{ 
  "content": [{ 
    "cityServiceCategoryList": [ 
      { 
        "categoryId": string, 
        "category": string, 
        "icon": string, 
        "image": string, 
        "description": string, 
        "cityServiceList": [Service] 
      } 
    ], 
    "cityId": number 
  }] 
} 
```

**Service Object Structure**:

```sh
{ 
  "serviceId": number, 
  "service": string, 
  "description": string, 
  "icon": string, 
  "image": string, 
  "function": string, 
  "serviceType": string, 
  "isNew": boolean, 
  "new": boolean, 
  "residence": boolean, 
  "restricted": boolean, 
  "serviceParams": { 
    // Optional parameters specific to the service 
  }, 
  "serviceAction": [Action], 
  "templateId": number 
} 
```

**Action Object Structure**:

```sh
{ 
  "actionId": number, 
  "action": string, 
  "actionOrder": number, 
  "androidUri": string, 
  "buttonDesign": string, 
  "iosAppStoreUri": string, 
  "iosUri": string, 
  "visibleText": string, 
  "actionType": string 
} 
```

### Required Fields

**Category Fields**:
| Field | Description |
| -------- | ------- |
|**categoryId** | Unique identifier for the service category |
|**category** | Display name of the category |
|**description** | Brief description of the category |
|**cityServiceList** | Array of services within this category |

**Service Fields**:
| Field | Description |
| -------- | ------- |
| **serviceId** | Unique identifier for the service |
| **service** | Display name of the service |
| **description** | HTML-formatted description of the service |
| **function** | Service type identifier used for routing |
| **serviceType** | Classification of the service |
| **restricted** | Boolean indicating if the service requires authentication |

**Action Fields**:
| Field | Description |
| -------- | ------- |
| **actionId** | Unique identifier for the action |
| **actionOrder** | Order in which actions should be displayed |
| **androidUri** | URI scheme or URL for handling the action |
| **visibleText** | Button text to display |
| **actionType** | Type of action to perform |

## Adding a new Service tile / API in city key App

To add a new Service (API) into Citykey, follow these steps:

1. **Put the API method**: Either put your API method into existing [
   `SmartCityApi`](https://github.com/telekom/CityKey-Android/blob/main/app/src/main/java/com/telekom/citykey/domain/repository/SmartCityApi.kt)
   interface, or create your own API interface under [
   `com.telekom.citykey.domain.repository`](https://github.com/telekom/CityKey-Android/tree/main/app/src/main/java/com/telekom/citykey/domain/repository)
   package.
2. **Actualise the API**: Actualise the API method in the [
   `SmartCityApiMockImpl`](https://github.com/telekom/CityKey-Android/blob/main/app/src/main/java/com/telekom/citykey/domain/mock/SmartCityApiMockImpl.kt)
   class, or if you have created a new interface, you can write your own implementation under the
   service's domain package.
3. **Link the API via Koin**: Provide the API (make ready to inject) by specifying the dependency
   in [
   `remote_datasource_module`](https://github.com/telekom/CityKey-Android/blob/main/app/src/main/java/com/telekom/citykey/di/RemoteDataSourceModule.kt).
   You don't have to do this if you have put your method into the existing [
   `SmartCityApi`](https://github.com/telekom/CityKey-Android/blob/main/app/src/main/java/com/telekom/citykey/domain/repository/SmartCityApi.kt).
4. **Usage of the API**: Use the API by injecting it into the relevant Repository which serves for
   the service you're trying to add.
5. **Call the API from the ViewModel**: Inject the repository into your ViewModel, then you can call
   the relevant API from there.

## Testing Your Integration

1. Add your service JSON to the mock response file
2. Build and run the app
3. Verify your service appears in the correct category
4. Test all actions and links
5. Verify the service description renders correctly
6. Test both authenticated and unauthenticated scenarios if applicable
