package com.telekom.citykey.utils.extensions

import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.NavGraph

fun NavController.safeNavigate(navDirections: NavDirections) {
    currentDestination?.let {
        it.getAction(navDirections.actionId)?.let { navAction ->
            val navGraph = when (it) {
                is NavGraph -> it
                else -> it.parent
            }
            if (navAction.destinationId != 0) {
                navGraph?.findNode(navAction.destinationId)?.let {
                    navigate(navDirections)
                }
            }
        } ?: tryNavigate(navDirections)
    } ?: tryNavigate(navDirections)
}

private fun NavController.tryNavigate(navDirections: NavDirections) {
    try {
        navigate(navDirections)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
