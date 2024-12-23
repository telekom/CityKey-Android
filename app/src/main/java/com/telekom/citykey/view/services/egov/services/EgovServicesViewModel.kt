package com.telekom.citykey.view.services.egov.services

import com.telekom.citykey.domain.services.egov.EgovInterractor
import com.telekom.citykey.models.egov.EgovGroup
import com.telekom.citykey.view.NetworkingViewModel

class EgovServicesViewModel(private val egovInterractor: EgovInterractor) : NetworkingViewModel() {
    fun loadEgovGroupData(groupId: Int): EgovGroup? {
        return egovInterractor.loadEgovGroupData(groupId)
    }
}
