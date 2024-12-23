package com.telekom.citykey.models.api.requests

class SubmitSurveyRequest(
    val totalQuestions: Int,
    val attemptedQuestions: Int,
    val responses: List<TopicAnswers>
)

class TopicAnswers(
    val questionId: Int,
    val topicId: Int,
    val optionNo: Int?,
    val freeText: String
)
