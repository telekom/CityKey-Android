package com.telekom.citykey.domain.services.surveys

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.telekom.citykey.domain.global.GlobalData
import com.telekom.citykey.domain.repository.ServicesRepository
import com.telekom.citykey.domain.repository.exceptions.InvalidRefreshTokenException
import com.telekom.citykey.domain.services.main.ServicesInteractor
import com.telekom.citykey.domain.services.main.ServicesStates
import com.telekom.citykey.models.api.requests.TopicAnswers
import com.telekom.citykey.models.citizen_survey.Question
import com.telekom.citykey.models.citizen_survey.SubmitResponse
import com.telekom.citykey.models.citizen_survey.Survey
import com.telekom.citykey.models.content.DataPrivacyNoticeResponse
import com.telekom.citykey.view.services.ServicesFunctions
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber

class SurveysInteractor(
    private val servicesRepository: ServicesRepository,
    private val globalData: GlobalData,
    private val servicesInteractor: ServicesInteractor,
    private val surveysQuestionsCache: SurveysQuestionsCache
) {
    val surveys: Observable<List<Survey>> get() = _surveysSubject.hide()
    val state: LiveData<SurveysState> get() = _surveysState

    private val _surveysSubject: BehaviorSubject<List<Survey>> = BehaviorSubject.create()
    private val _surveysState: MutableLiveData<SurveysState> = MutableLiveData()
    var dataPrivacyMap = mutableMapOf<Int, Boolean>()

    init {
        observeServices()
    }

    @SuppressLint("CheckResult")
    private fun observeServices() {
        servicesInteractor.state
            .filter { it !is ServicesStates.Loading }
            .map {
                it is ServicesStates.Success &&
                        it.data.services.find { service -> service.function == ServicesFunctions.SURVEYS } != null
            }
            .switchMap { isServiceAvailable ->
                return@switchMap if (isServiceAvailable) {
                    _surveysState.postValue(SurveysState.Loading)
                    servicesRepository.getSurveys(globalData.currentCityId)
                        .doOnSuccess {
                            _surveysState.postValue(
                                if (it.isEmpty()) SurveysState.Empty else SurveysState.Success
                            )
                        }
                        .onErrorReturn {
                            _surveysState.postValue(SurveysState.Error)
                            emptyList()
                        }
                        .toFlowable()
                } else {
                    _surveysState.postValue(SurveysState.ServiceNotAvailable)
                    Flowable.just(emptyList())
                }
            }
            .subscribe(_surveysSubject::onNext, Timber::e)
    }

    fun isSurveyPreviewAvailable(): Boolean {
        return _surveysState.value in listOf(SurveysState.Success, SurveysState.Empty, SurveysState.Loading)
    }

    fun getQuestionsForSurvey(surveyId: Int): Maybe<List<Question>> = surveysQuestionsCache.getSurveyQuestions(surveyId)

    fun getDataPrivacyNoticeForSurvey(): Maybe<DataPrivacyNoticeResponse> =
        servicesRepository.getDataPrivacyNoticeForSurvey(globalData.currentCityId)

    fun setSurveyPrivacyNoticeAccepted(id: Int) {
        dataPrivacyMap[id] = true
    }

    private fun setSurveyCompleted(surveyId: Int) {
        _surveysSubject.value?.apply { find { it.id == surveyId }?.status = Survey.STATUS_COMPLETED }
            ?.also(_surveysSubject::onNext)
    }

    fun setSurveyAnswer(
        surveyId: Int,
        totalQuestions: Int,
        attemptedQuestions: Int,
        topicResponse: Map<String, MutableList<TopicAnswers>>,
        submitSurvey: Boolean
    ): Maybe<SubmitResponse> =
        surveysQuestionsCache.setSurveyAnswer(surveyId, totalQuestions, attemptedQuestions, topicResponse, submitSurvey)
            .doOnSuccess { if (it.isSuccessful) setSurveyCompleted(surveyId) }

    fun refreshSurveys(): Completable {
        return if (globalData.isUserLoggedIn) {
            _surveysState.postValue(SurveysState.Loading)
            servicesRepository.getSurveys(globalData.currentCityId)
                .doOnError {
                    when (it) {
                        is InvalidRefreshTokenException -> {
                            globalData.logOutUser(it.reason)
                            _surveysSubject.onNext(emptyList())
                            _surveysState.postValue(SurveysState.ServiceNotAvailable)
                        }

                        else -> {
                            if (_surveysState.value != SurveysState.Success)
                                _surveysState.postValue(SurveysState.Error)
                        }
                    }
                }
                .doOnSuccess {
                    _surveysSubject.onNext(it)
                    _surveysState.postValue(if (it.isEmpty()) SurveysState.Empty else SurveysState.Success)
                }
                .ignoreElement()
                .onErrorComplete()
        } else {
            _surveysState.postValue(SurveysState.ServiceNotAvailable)
            Completable.complete()
        }
    }
}
