package com.telekom.citykey.domain.ausweiss_app

import com.telekom.citykey.domain.ausweiss_app.models.Card
import com.telekom.citykey.domain.ausweiss_app.models.CertificateInfo
import com.telekom.citykey.domain.ausweiss_app.models.CertificateValidity
import com.telekom.citykey.domain.ausweiss_app.models.Result

@Suppress("ClassName")
sealed class IdentMsg {
    object REQUEST_PUK : IdentMsg()
    object REQUEST_CAN : IdentMsg()
    object INSERT_CARD : IdentMsg()
    object CARD_BLOCKED : IdentMsg()

    class RequestPin(val retries: Int) : IdentMsg()
    class Error(val msg: String) : IdentMsg()
    class ErrorResult(val result: Result) : IdentMsg()
    class AccessRights(val rights: List<String>) : IdentMsg()
    class Completed(val url: String) : IdentMsg()
    class Certificate(val certificateInfo: CertificateInfo, val certificateValidity: CertificateValidity) : IdentMsg()
    class CardRecognized(val card: Card?) : IdentMsg()
}
