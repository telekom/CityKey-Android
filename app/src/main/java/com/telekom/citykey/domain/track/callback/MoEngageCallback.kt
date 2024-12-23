package com.telekom.citykey.domain.track.callback

import com.moengage.inapp.listeners.InAppLifeCycleListener
import com.moengage.inapp.listeners.OnClickActionListener
import com.moengage.inapp.model.ClickData
import com.moengage.inapp.model.InAppData

class InAppLifecycleCallbacks : InAppLifeCycleListener {

    override fun onDismiss(inAppData: InAppData) {}

    override fun onShown(inAppData: InAppData) {}
}

class ClickActionCallback : OnClickActionListener {

    override fun onClick(clickData: ClickData): Boolean {
        return true
    }
}
