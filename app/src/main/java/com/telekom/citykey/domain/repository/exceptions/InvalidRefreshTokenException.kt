package com.telekom.citykey.domain.repository.exceptions

import com.telekom.citykey.view.user.login.LogoutReason
import java.io.IOException

class InvalidRefreshTokenException(val reason: LogoutReason) : IOException()
