package com.telekom.citykey.domain.user

import com.telekom.citykey.models.content.UserProfile

sealed class UserState {
    object Absent : UserState()
    class Present(val profile: UserProfile) : UserState()
}
