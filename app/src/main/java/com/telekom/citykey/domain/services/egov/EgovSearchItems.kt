package com.telekom.citykey.domain.services.egov

import androidx.annotation.StringRes
import com.telekom.citykey.models.egov.EgovService

sealed class EgovSearchItems {
    class Result(val item: EgovService) : EgovSearchItems()
    class History(val item: String) : EgovSearchItems()
    class Header(@StringRes val resId: Int) : EgovSearchItems()
    class FullScreenMessage(@StringRes val resId: Int, val query: String? = null) : EgovSearchItems()
}
