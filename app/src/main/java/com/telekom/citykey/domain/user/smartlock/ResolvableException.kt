package com.telekom.citykey.domain.user.smartlock

class ResolvableException(val exception: Throwable, val requestCode: Int) : RuntimeException()
