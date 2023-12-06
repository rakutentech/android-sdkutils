package com.rakuten.tech.mobile.sdkutils.eventlogger

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import java.lang.ref.WeakReference

internal interface LifecycleListener {
    /**
     * Called when app transitioned from background to foreground.
     */
    fun becameForeground()
}

internal class AppLifecycleListener(private val context: WeakReference<Context>) {

    private lateinit var lifecycleCallback: LifecycleCallback
    private val listeners = mutableListOf<LifecycleListener>()

    /**
     * Registers [listener] to be notified of app lifecycle callbacks.
     */
    fun registerListener(listener: LifecycleListener) {
        EventLogger.log.debug("registerListener")
        if (!this::lifecycleCallback.isInitialized) {
            lifecycleCallback = LifecycleCallback()

            val app = context.get()?.applicationContext as? Application
            app?.registerActivityLifecycleCallbacks(lifecycleCallback)
        }

        if (listener !in listeners) {
            listeners.add(listener)
        }
    }

    private fun notifyOnBecameForeground() {
        listeners.forEach {
            try {
                it.becameForeground()
            } catch (_: Exception) {
                // do nothing
            }
        }
    }

    private inner class LifecycleCallback : Application.ActivityLifecycleCallbacks {
        private var isInBackground = false

        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            // do nothing
        }

        override fun onActivityStarted(activity: Activity) {
            // do nothing
        }

        override fun onActivityResumed(activity: Activity) {
            if (isInBackground) {
                isInBackground = false
                notifyOnBecameForeground()
            }
        }

        override fun onActivityPaused(activity: Activity) {
            // do nothing
        }

        override fun onActivityStopped(activity: Activity) {
            if (!activity.isChangingConfigurations) {
                isInBackground = true
            }
        }

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
            // do nothing
        }

        override fun onActivityDestroyed(activity: Activity) {
            // do nothing
        }
    }
}
