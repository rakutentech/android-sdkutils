package com.rakuten.tech.mobile.sdkutils

import android.app.Activity
import android.os.Bundle
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import java.lang.ref.WeakReference

@RunWith(RobolectricTestRunner::class)
@SuppressWarnings(
    "EmptyFunctionBlock",
    "TooGenericExceptionThrown"
)
class AppLifecycleObserverSpec {

    private val activity = Robolectric.buildActivity(Activity::class.java)
    private val observer = AppLifecycleObserver(WeakReference(activity.get()))

    @Test
    fun `should notify listener when transitioned from background to foreground`() {
        val listenerSpy = spy(object : LifecycleListener {
            override fun becameForeground() {}
        })
        observer.registerListener(listenerSpy)

        // simulate put to background and go back to screen
        activity.create().start().resume().pause().stop().resume()
        verify(listenerSpy).becameForeground()
    }

    @Test
    fun `should not notify listener if in foreground but not from background`() {
        val listenerSpy = spy(object : LifecycleListener {
            override fun becameForeground() {}
        })
        observer.registerListener(listenerSpy)

        // simulate screen visited
        activity.create().start().resume()
        verify(listenerSpy, never()).becameForeground()

        // simulate screen closed and new screen displayed
        activity.create().start().resume().saveInstanceState(Bundle()).destroy()
        val newActivity = Robolectric.buildActivity(Activity::class.java)
        newActivity.create().start().resume()
        verify(listenerSpy, never()).becameForeground()
    }

    @Test
    fun `should not crash when listener throws exception`() {
        val listenerSpy = spy(object : LifecycleListener {
            override fun becameForeground() {
                throw Exception("")
            }
        })
        observer.registerListener(listenerSpy)

        activity.create().start().resume().pause().stop().resume()
        verify(listenerSpy).becameForeground()
    }
}
