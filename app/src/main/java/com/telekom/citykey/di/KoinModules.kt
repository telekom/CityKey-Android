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

package com.telekom.citykey.di

import android.content.Context
import android.location.Geocoder
import com.google.android.gms.auth.api.credentials.Credentials
import com.google.android.gms.auth.api.credentials.CredentialsClient
import com.google.android.gms.auth.api.credentials.CredentialsOptions
import com.google.android.gms.location.LocationServices
import com.scottyab.rootbeer.RootBeer
import com.telekom.citykey.domain.ausweiss_app.IdentInteractor
import com.telekom.citykey.domain.auth.OAuth2TokenManager
import com.telekom.citykey.domain.city.CityInteractor
import com.telekom.citykey.domain.city.available_cities.AvailableCitiesInteractor
import com.telekom.citykey.domain.city.events.EventsInteractor
import com.telekom.citykey.domain.city.news.NewsInteractor
import com.telekom.citykey.domain.city.weather.WeatherInteractor
import com.telekom.citykey.domain.global.GlobalData
import com.telekom.citykey.domain.global_messager.GlobalMessages
import com.telekom.citykey.domain.legal_data.LegalDataManager
import com.telekom.citykey.domain.location.LocationBasedCitiesInteractor
import com.telekom.citykey.domain.location.LocationInteractor
import com.telekom.citykey.domain.location.OscaLocationManager
import com.telekom.citykey.domain.mailbox.MailboxManager
import com.telekom.citykey.domain.notifications.NotificationsStateController
import com.telekom.citykey.domain.notifications.TpnsManager
import com.telekom.citykey.domain.notifications.notification_badges.InAppNotificationsInteractor
import com.telekom.citykey.domain.repository.CityRepository
import com.telekom.citykey.domain.repository.OscaRepository
import com.telekom.citykey.domain.repository.ServicesRepository
import com.telekom.citykey.domain.repository.TpnsRepository
import com.telekom.citykey.domain.repository.UserRepository
import com.telekom.citykey.domain.repository.WidgetRepository
import com.telekom.citykey.domain.security.crypto.Crypto
import com.telekom.citykey.domain.security.rootbeer.RootDetector
import com.telekom.citykey.domain.services.appointments.AppointmentsInteractor
import com.telekom.citykey.domain.services.defect_reporter.DefectReporterInteractor
import com.telekom.citykey.domain.services.egov.EgovInterractor
import com.telekom.citykey.domain.services.fahrradparken.FahrradparkenServiceInteractor
import com.telekom.citykey.domain.services.main.ServicesInteractor
import com.telekom.citykey.domain.services.poi.POIInteractor
import com.telekom.citykey.domain.services.surveys.SurveysInteractor
import com.telekom.citykey.domain.services.surveys.SurveysQuestionsCache
import com.telekom.citykey.domain.services.waste_calendar.WasteCalendarInteractor
import com.telekom.citykey.domain.services.waste_calendar.WasteExportInteractor
import com.telekom.citykey.domain.track.AdjustManager
import com.telekom.citykey.domain.user.UserInteractor
import com.telekom.citykey.domain.user.smartlock.CredentialsClientHandler
import com.telekom.citykey.domain.whats_new.WhatsNewInteractor
import com.telekom.citykey.domain.widget.WidgetInteractor
import com.telekom.citykey.network.interceptors.AppConfigInterceptor
import com.telekom.citykey.network.interceptors.ConnectivityInterceptor
import com.telekom.citykey.network.interceptors.OAuth2Interceptor
import com.telekom.citykey.network.interceptors.UserInterceptor
import com.telekom.citykey.networkinterface.interceptors.IAppConfigInterceptor
import com.telekom.citykey.networkinterface.interceptors.IConnectivityInterceptor
import com.telekom.citykey.networkinterface.interceptors.IOAuth2Interceptor
import com.telekom.citykey.networkinterface.interceptors.IUserInterceptor
import com.telekom.citykey.utils.PreferencesHelper
import com.telekom.citykey.view.auth_webview.AuthBottomSheetDialogViewModel
import com.telekom.citykey.view.city_imprint.CityImprintViewModel
import com.telekom.citykey.view.city_selection.CitySelectionViewModel
import com.telekom.citykey.view.dialogs.dpn_updates.DpnUpdatesViewModel
import com.telekom.citykey.view.home.HomeViewModel
import com.telekom.citykey.view.home.events_details.EventDetailsViewModel
import com.telekom.citykey.view.home.events_list.EventsListViewModel
import com.telekom.citykey.view.home.events_list.category_filter.CategoryFilterViewModel
import com.telekom.citykey.view.home.events_list.date_filter.DateFilterViewModel
import com.telekom.citykey.view.home.news.NewsViewModel
import com.telekom.citykey.view.infobox.InfoBoxViewModel
import com.telekom.citykey.view.main.MainViewModel
import com.telekom.citykey.view.services.ServicesViewModel
import com.telekom.citykey.view.services.appointments.AppointmentServiceViewModel
import com.telekom.citykey.view.services.appointments.appointments_overview.AppointmentsOverviewViewModel
import com.telekom.citykey.view.services.appointments.details.AppointmentDetailsViewModel
import com.telekom.citykey.view.services.appointments.qr.AppointmentQRViewModel
import com.telekom.citykey.view.services.appointments.web.AppointmentWebViewModel
import com.telekom.citykey.view.services.citizen_surveys.SurveysOverviewViewModel
import com.telekom.citykey.view.services.citizen_surveys.survey.SurveyQuestionsViewModel
import com.telekom.citykey.view.services.citizen_surveys.survey_details.SurveyDetailsViewModel
import com.telekom.citykey.view.services.defect_reporter.category_selection.DefectCategorySelectionViewModel
import com.telekom.citykey.view.services.defect_reporter.details.DefectServiceDetailViewModel
import com.telekom.citykey.view.services.defect_reporter.location_selection.DefectLocationSelectionViewModel
import com.telekom.citykey.view.services.defect_reporter.report_form.DefectReportFormViewModel
import com.telekom.citykey.view.services.egov.details.EgovServiceDetailsViewModel
import com.telekom.citykey.view.services.egov.search.EgovSearchViewModel
import com.telekom.citykey.view.services.egov.services.EgovServicesViewModel
import com.telekom.citykey.view.services.fahrradparken.category_selection.FahrradparkenCategorySelectionViewModel
import com.telekom.citykey.view.services.fahrradparken.details.FahrradparkenServiceDetailViewModel
import com.telekom.citykey.view.services.fahrradparken.existing_reports.FahrradparkenExistingReportsViewModel
import com.telekom.citykey.view.services.fahrradparken.report_form.FahrradparkenCreateReportFormViewModel
import com.telekom.citykey.view.services.poi.PoiGuideViewModel
import com.telekom.citykey.view.services.poi.categories.PoiCategorySelectionViewModel
import com.telekom.citykey.view.services.service_detail_help.ServiceDetailHelpViewModel
import com.telekom.citykey.view.services.waste_calendar.WasteCalendarViewModel
import com.telekom.citykey.view.services.waste_calendar.address_change.WasteCalendarAddressViewModel
import com.telekom.citykey.view.services.waste_calendar.export.WasteEventsExportViewModel
import com.telekom.citykey.view.services.waste_calendar.filters.WasteFiltersViewModel
import com.telekom.citykey.view.services.waste_calendar.reminders.WasteReminderViewModel
import com.telekom.citykey.view.services.waste_calendar.service_details.WasteCalendarDetailsViewModel
import com.telekom.citykey.view.user.forgot_password.ForgotPasswordViewModel
import com.telekom.citykey.view.user.login.login.LoginViewModel
import com.telekom.citykey.view.user.pin_verification.PINVerificationViewModel
import com.telekom.citykey.view.user.profile.change_birthday.ChangeBirthdayViewModel
import com.telekom.citykey.view.user.profile.change_email.ChangeEmailViewModel
import com.telekom.citykey.view.user.profile.change_password.ChangePasswordViewModel
import com.telekom.citykey.view.user.profile.change_residence.ChangeResidenceViewModel
import com.telekom.citykey.view.user.profile.delete_account.DeleteAccountValidationViewModel
import com.telekom.citykey.view.user.profile.feedback.FeedbackViewModel
import com.telekom.citykey.view.user.profile.profile.ProfileViewModel
import com.telekom.citykey.view.user.profile.settings.personal_settings.PersonalDetailSettingsViewModel
import com.telekom.citykey.view.user.registration.RegistrationViewModel
import com.telekom.citykey.view.welcome.WelcomeViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module
import java.util.Locale

private const val APP_PREFERENCES_FILE = "APP_PREFERENCES_FILE"

private fun getGeoCoder(context: Context) = Geocoder(context, Locale.GERMANY)

private fun getCredentialsClient(context: Context): CredentialsClient =
    Credentials.getClient(
        context,
        CredentialsOptions.Builder()
            .forceEnableSaveDialog()
            .build()
    )

private fun getContentResolver(context: Context) = context.contentResolver

private fun getSharedPreferences(context: Context) =
    context.getSharedPreferences(APP_PREFERENCES_FILE, Context.MODE_PRIVATE)

private val viewModelModule = module {

    viewModel { MainViewModel(get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }

    viewModel { HomeViewModel(get(), get(), get(), get(), get()) }

    viewModel { NewsViewModel(get()) }

    viewModel { ProfileViewModel(get(), get(), get()) }

    viewModel { ServicesViewModel(get(), get(), get(), get()) }

    viewModel { InfoBoxViewModel(get(), get()) }

    viewModel { LoginViewModel(get(), get(), get(), get(), get()) }

    viewModel { RegistrationViewModel(get()) }

    viewModel { PINVerificationViewModel(get(), get()) }

    viewModel { ChangePasswordViewModel(get(), get()) }

    viewModel { ForgotPasswordViewModel(get(), get()) }

    viewModel { ChangeEmailViewModel(get(), get()) }

    viewModel { DeleteAccountValidationViewModel(get(), get()) }

    viewModel { EventsListViewModel(get(), get()) }

    viewModel { CategoryFilterViewModel(get(), get(), get()) }

    viewModel { DateFilterViewModel(get(), get()) }

    viewModel { EventDetailsViewModel(get(), get(), get(), get(), get()) }

    viewModel { WelcomeViewModel(get()) }

    viewModel { WasteCalendarAddressViewModel(get(), get()) }

    viewModel { CitySelectionViewModel(get(), get(), get(), get(), get()) }

    viewModel { AppointmentWebViewModel(get(), get()) }

    viewModel { CityImprintViewModel(get()) }

    viewModel { AppointmentServiceViewModel(get(), get()) }

    viewModel { AppointmentsOverviewViewModel(get(), get(), get()) }

    viewModel { AppointmentDetailsViewModel(get(), get(), get(), get()) }

    viewModel { AppointmentQRViewModel(get()) }

    viewModel { WasteCalendarViewModel(get(), get()) }

    viewModel { WasteCalendarDetailsViewModel(get(), get(), get()) }

    viewModel { WasteFiltersViewModel(get()) }

    viewModel { WasteReminderViewModel(get(), get(), get(), get()) }

    viewModel { (url: String) -> AuthBottomSheetDialogViewModel(get(), url) }

    viewModel { SurveysOverviewViewModel(get(), get()) }

    viewModel { SurveyDetailsViewModel(get(), get()) }

    viewModel { (surveyId: Int) -> SurveyQuestionsViewModel(surveyId, get(), get(), get(), get()) }

    viewModel { ChangeResidenceViewModel(get(), get(), get()) }

    viewModel { PersonalDetailSettingsViewModel(get()) }

    viewModel { PoiGuideViewModel(get(), get(), get(), get()) }

    viewModel { PoiCategorySelectionViewModel(get(), get()) }

    viewModel { EgovServiceDetailsViewModel(get(), get()) }

    viewModel { EgovServicesViewModel(get()) }

    viewModel { DefectServiceDetailViewModel(get()) }

    viewModel { DefectCategorySelectionViewModel(get()) }

    viewModel { DefectReportFormViewModel(get(), get(), get(), get(), get()) }

    viewModel { DefectLocationSelectionViewModel(get(), get()) }

    viewModel { ChangeBirthdayViewModel(get(), get()) }

    viewModel { parameter -> ServiceDetailHelpViewModel(get(), serviceId = parameter.get()) }

    viewModel { DpnUpdatesViewModel(get(), get()) }

    viewModel { FeedbackViewModel(get(), get(), get(), get()) }

    viewModel { EgovSearchViewModel(get()) }

    viewModel { WasteEventsExportViewModel(get()) }

    viewModel { FahrradparkenServiceDetailViewModel(get()) }

    viewModel { FahrradparkenCategorySelectionViewModel(get()) }

    viewModel { FahrradparkenCreateReportFormViewModel(get(), get(), get(), get()) }

    viewModel { FahrradparkenExistingReportsViewModel(get(), get(), get()) }
}

private val useCaseModule = module {

    single { GlobalData(get(), get(), get()) }

    single { getGeoCoder(get()) }

    single { OscaLocationManager(get()) }

    single { PreferencesHelper(getSharedPreferences(get())) }

    single { EventsInteractor(get(), get(), get()) }

    factory { Crypto(get()) }

    single { GlobalMessages(get()) }

    single(createdAtStart = true) { MailboxManager(get(), get(), get(), get()) }

    single { CredentialsClientHandler(getCredentialsClient(get())) }

    single { TpnsManager(get(), get(), getSharedPreferences(get()), get()) }

    single { AdjustManager(get(), get()) }

    single { LegalDataManager(get(), get()) }

    single { NewsInteractor(get(), get()) }

    single { WeatherInteractor(get(), get()) }

    single { AvailableCitiesInteractor(get()) }

    single { LocationBasedCitiesInteractor(get(), get()) }

    single { ServicesInteractor(get(), get(), get()) }

    single { UserInteractor(get(), get(), get()) }

    single { CityInteractor(get(), get()) }

    single(createdAtStart = true) { AppointmentsInteractor(get(), get(), get(), get()) }

    single { WasteCalendarInteractor(get(), get(), get(), get()) }

    factory { NotificationsStateController(get()) }

    single { InAppNotificationsInteractor() }

    single(createdAtStart = true) { SurveysInteractor(get(), get(), get(), get()) }

    single { IdentInteractor(get()) }

    single { SurveysQuestionsCache(get(), get()) }

    single { POIInteractor(get(), get(), get(), get()) }

    single { DefectReporterInteractor(get(), get()) }

    factory { LocationInteractor(LocationServices.getFusedLocationProviderClient(get<Context>())) }

    single { EgovInterractor(get(), get(), getSharedPreferences(get())) }

    factory { RootDetector(RootBeer(get())) }

    single { OAuth2TokenManager(get(), get(), get()) }

    single { WasteExportInteractor(getContentResolver(get())) }

    factory { WidgetInteractor(get()) }

    factory { WhatsNewInteractor(get()) }

    single { FahrradparkenServiceInteractor(get(), get()) }
}

private val repositoryModule = module {
    single { OscaRepository(get(), get()) }
    single { ServicesRepository(get(), get()) }
    single { UserRepository(get(), get()) }
    single { CityRepository(get(), get(), get()) }
    single { WidgetRepository(get(), get(), get()) }
    single { TpnsRepository(get()) }
}

private val apiModule = module {

    single<IAppConfigInterceptor> {
        IAppConfigInterceptor(::AppConfigInterceptor)
    }.bind<IAppConfigInterceptor>()

    single<IConnectivityInterceptor> {
        IConnectivityInterceptor(::ConnectivityInterceptor)
    }.bind<IConnectivityInterceptor>()

    single<IOAuth2Interceptor> {
        IOAuth2Interceptor {
            OAuth2Interceptor(get(), get())
        }
    }.bind<IOAuth2Interceptor>()

    single<IUserInterceptor> {
        IUserInterceptor {
            UserInterceptor(get(), get())
        }
    }.bind<IUserInterceptor>()
}

val citykeyKoinModules = listOf(viewModelModule, useCaseModule, repositoryModule, apiModule)
