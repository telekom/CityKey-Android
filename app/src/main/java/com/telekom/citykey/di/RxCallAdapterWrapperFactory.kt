package com.telekom.citykey.di

import com.telekom.citykey.common.NetworkException
import com.telekom.citykey.domain.repository.ErrorType
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.Function
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Converter
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import java.lang.reflect.Type
import kotlin.reflect.KClass

class RxCallAdapterWrapperFactory constructor(private val rxJava2CallAdapterFactory: CallAdapter.Factory) :
    CallAdapter.Factory() {

    companion object {
        fun create(): RxCallAdapterWrapperFactory {
            return RxCallAdapterWrapperFactory(RxJava2CallAdapterFactory.create())
        }
    }

    private fun handleError(annotations: Array<Annotation>, retrofit: Retrofit, throwable: Throwable): Throwable {

        val errorType: ErrorType? = annotations.find { it is ErrorType } as? ErrorType

        return if (errorType != null && throwable is HttpException) {
            val error = parseError(retrofit, throwable, errorType.type)
            NetworkException(throwable.code(), error, throwable.message(), throwable)
        } else throwable
    }

    private fun parseError(retrofit: Retrofit, httpException: HttpException, kClass: KClass<*>): Any? {
        if (httpException.response()?.isSuccessful == true) {
            return null
        }
        val errorBody = httpException.response()?.errorBody() ?: return null
        val converter: Converter<ResponseBody, Any> = retrofit.responseBodyConverter(kClass.java, arrayOf())
        return converter.convert(errorBody)
    }

    override fun get(returnType: Type, annotations: Array<Annotation>, retrofit: Retrofit): CallAdapter<*, *>? {
        return rxJava2CallAdapterFactory.get(returnType, annotations, retrofit)?.let {
            RxCallAdapterWrapper(annotations, retrofit, it)
        }
    }

    private inner class RxCallAdapterWrapper<R, T> constructor(
        private val annotations: Array<Annotation>,
        private val retrofit: Retrofit,
        private val callAdapter: CallAdapter<R, T>
    ) : CallAdapter<R, T> {

        @Suppress("UNCHECKED_CAST", "IMPLICIT_CAST_TO_ANY")
        override fun adapt(call: Call<R>): T =
            when (val any = callAdapter.adapt(call)) {
                is Observable<*> ->
                    any.onErrorResumeNext(Function { Observable.error(handleError(annotations, retrofit, it)) })
                is Maybe<*> ->
                    any.onErrorResumeNext(Function { Maybe.error(handleError(annotations, retrofit, it)) })
                is Single<*> ->
                    any.onErrorResumeNext(Function { Single.error(handleError(annotations, retrofit, it)) })
                is Flowable<*> ->
                    any.onErrorResumeNext(Function { Flowable.error(handleError(annotations, retrofit, it)) })
                is Completable ->
                    any.onErrorResumeNext(Function { Completable.error(handleError(annotations, retrofit, it)) })
                else -> any
            } as T

        override fun responseType(): Type = callAdapter.responseType()
    }
}
