package com.rakuten.tech.mobile.sdkutils

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import java.lang.ref.WeakReference

/**
 * Listener that should be registered to the [AppLifecycleObserver.registerListener] to be notified
 * of desired application lifecycle callbacks.
 */
@SuppressWarnings("kotlin:S6517")
interface LifecycleListener {
    /**
     * Called when app transitioned from background to foreground.
     */
    fun becameForeground() { /* default: do nothing */ }
}

/**
 * Observes to application lifecycle callbacks.
 */
class AppLifecycleObserver(private val context: WeakReference<Context>) {

    private lateinit var lifecycleCallback: LifecycleCallback
    private val listeners = mutableListOf<LifecycleListener>()

    /**
     * Registers [listener] to be notified of app lifecycle callbacks.
     */
    fun registerListener(listener: LifecycleListener) {
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

        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit

        override fun onActivityStarted(activity: Activity) = Unit

        override fun onActivityResumed(activity: Activity) {
            if (isInBackground) {
                isInBackground = false
                notifyOnBecameForeground()
            }
        }

        override fun onActivityPaused(activity: Activity) = Unit

        override fun onActivityStopped(activity: Activity) {
            isInBackground = true
        }

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit

        override fun onActivityDestroyed(activity: Activity) {
            isInBackground = false
        }
    }
}
