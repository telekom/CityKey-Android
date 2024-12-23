package com.telekom.citykey.domain.services.egov

import com.telekom.citykey.models.egov.EgovGroup

sealed class EgovState {
    class Success(val egovItems: List<EgovGroup>) : EgovState()
    object LOADING : EgovState()
    object ERROR : EgovState()
}
