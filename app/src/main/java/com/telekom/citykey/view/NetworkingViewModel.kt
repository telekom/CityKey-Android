package com.telekom.citykey.view

import androidx.lifecycle.LiveData
import com.telekom.citykey.utils.SingleLiveEvent
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.subjects.PublishSubject
import org.reactivestreams.Subscription

abstract class NetworkingViewModel : BaseViewModel() {

    private val _retryDispatcher: PublishSubject<String> = PublishSubject.create()
    protected val retryDispatcher: Flowable<String> get() = _retryDispatcher.toFlowable(BackpressureStrategy.DROP)
    protected val pendingRetries = mutableMapOf<String, Subscription?>()

    val showRetryDialog: LiveData<String?> get() = _showRetryDialog
    val technicalError: LiveData<Unit> get() = _technicalError
    @Suppress("PropertyName", "VariableNaming")
    protected val _showRetryDialog: SingleLiveEvent<String?> = SingleLiveEvent()
    @Suppress("PropertyName", "VariableNaming")
    protected val _technicalError: SingleLiveEvent<Unit> = SingleLiveEvent()

    fun onRetryRequired() {
        pendingRetries.forEach { entry -> _retryDispatcher.onNext(entry.key) }
    }

    protected fun showRetry() {
        _showRetryDialog.call()
    }

    fun onRetryCanceled() {
        pendingRetries.forEach { entry -> entry.value?.cancel() }
        pendingRetries.clear()
    }

    override fun onCleared() {
        super.onCleared()
        pendingRetries.forEach { entry -> entry.value?.cancel() }
        pendingRetries.clear()
    }
}
