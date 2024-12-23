package com.telekom.citykey.domain.repository

import com.telekom.citykey.common.ErrorCodes
import com.telekom.citykey.domain.repository.exceptions.UnsupportedVersionException
import com.telekom.citykey.models.api.requests.FeedbackRequest
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class OscaRepository(
    private val api: SmartCityApi,
    private val authApi: SmartCityAuthApi
) {

    fun setMailRead(msgId: Int, markRead: Boolean) = authApi.setInformationRead(msgId, markRead)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())

    fun getLegalData() = api.getLegalData()
        .subscribeOn(Schedulers.io())
        .map { it.content[0] }
        .observeOn(AndroidSchedulers.mainThread())

    fun getMailBox() = authApi.getInfoBox()
        .subscribeOn(Schedulers.io())
        .map { it.content }

    fun deleteMail(msgId: Int, delete: Boolean) = authApi.deleteInfoBoxMessage(msgId, delete)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())

    fun acceptDataSecurityChanges(dpnAccepted: Boolean) = authApi.acceptDataSecurityChanges(dpnAccepted)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())

    fun sendFeedback(feedbackRequest: FeedbackRequest) = api.sendFeedback(feedbackRequest)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())

    fun checkAppVersion() = api.checkAppVersion()
        .subscribeOn(Schedulers.io())
        .flatMapCompletable { response ->
            if (response.content.any { it.errorCode == ErrorCodes.VERSION_NOT_SUPPORTED })
                Completable.error(UnsupportedVersionException())
            else
                Completable.complete()
        }
        .onErrorResumeNext { error ->
            if (error is UnsupportedVersionException) Completable.error(error)
            else Completable.complete()
        }
        .observeOn(AndroidSchedulers.mainThread())
}
