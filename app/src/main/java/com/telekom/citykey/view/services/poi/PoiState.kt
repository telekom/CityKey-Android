package com.telekom.citykey.view.services.poi

sealed class PoiState {
    object LOADING : PoiState()
    object ERROR : PoiState()
    object EMPTY : PoiState()
    object SUCCESS : PoiState()
}
