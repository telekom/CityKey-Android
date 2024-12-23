package com.telekom.citykey.domain.services.surveys

import android.annotation.SuppressLint
import com.telekom.citykey.domain.global.GlobalData
import com.telekom.citykey.domain.repository.ServicesRepository
import com.telekom.citykey.domain.user.UserState
import com.telekom.citykey.models.api.requests.SubmitSurveyRequest
import com.telekom.citykey.models.api.requests.TopicAnswers
import com.telekom.citykey.models.citizen_survey.Question
import com.telekom.citykey.models.citizen_survey.SubmitResponse
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers

class SurveysQuestionsCache(
    private val globalData: GlobalData,
    private val servicesRepository: ServicesRepository
) {

    private val submitList = mutableListOf<TopicAnswers>()
    private val mapAnswers = mutableMapOf<String, MutableList<TopicAnswers>>()
    private val _questions = mutableMapOf<Int, List<Question>>()
    private var attemptedQuestions = 0

    init {
        observeCityAndUser()
    }

    @SuppressLint("CheckResult")
    private fun observeCityAndUser() {
        Observable.combineLatest(
            globalData.user.filter { it is UserState.Absent },
            globalData.city.distinctUntilChanged { c1, c2 -> c1.cityId == c2.cityId }
        ) { _, _ -> }
            .subscribe { _questions.clear() }
    }

    fun setSurveyAnswer(
        surveyId: Int,
        totalQuestions: Int,
        attemptedQuestions: Int,
        topicResponse: Map<String, MutableList<TopicAnswers>>,
        submitSurvey: Boolean
    ): Maybe<SubmitResponse> {
        topicResponse.forEach { mapAnswers[it.key] = it.value }

        this.attemptedQuestions = attemptedQuestions
        if (submitSurvey) {
            submitList.clear()
            val submitSurveyRequest = SubmitSurveyRequest(totalQuestions, attemptedQuestions, submitList)
            mapAnswers.forEach { submitList.addAll(it.value) }
            return servicesRepository.submitSurvey(globalData.currentCityId, surveyId, submitSurveyRequest)
        }
        return Maybe.just(SubmitResponse(false))
    }

    fun getSurveyQuestions(surveyId: Int): Maybe<List<Question>> {
        _questions[surveyId]?.let { return Maybe.just(it) }

        return servicesRepository.getSurveyQuestions(surveyId, globalData.currentCityId)
            .doOnSuccess { _questions[surveyId] = it }
            .observeOn(AndroidSchedulers.mainThread())
    }
}
