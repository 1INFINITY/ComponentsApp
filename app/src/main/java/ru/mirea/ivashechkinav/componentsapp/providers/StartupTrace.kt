package ru.mirea.ivashechkinav.componentsapp.providers

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import ru.mirea.ivashechkinav.componentsapp.SplashActivity
import java.util.concurrent.TimeUnit

object StartupTrace : Application.ActivityLifecycleCallbacks, LifecycleEventObserver {

    private val TAG = StartupTimeProvider::class.simpleName

    private val MAX_LATENCY_BEFORE_UI_INIT = TimeUnit.MINUTES.toMillis(1)

    var appStartTime: Long? = null
    private var onCreateTime: Long? = null
    var isStartedFromBackground = false
    var atLeastOnTimeOnBackground = false

    private var isRegisteredForLifecycleCallbacks = false
    private var appContext: Context? = null


    /**
     * If the time difference between app starts and creation of any Activity is larger than
     * MAX_LATENCY_BEFORE_UI_INIT, set isTooLateToInitUI to true and we don't send AppStart Trace.
     */
    var isTooLateToInitUI = false

    fun onColdStartInitiated(context: Context) {
        appStartTime = System.currentTimeMillis()

        val appContext = context.applicationContext
        if (appContext is Application) {
            Log.i(TAG, "Add observer of application")
            appContext.registerActivityLifecycleCallbacks(this)
            ProcessLifecycleOwner.get().lifecycle.addObserver(this)
            isRegisteredForLifecycleCallbacks = true
            this.appContext = appContext
        }
    }

    /** Unregister this callback after AppStart trace is logged.  */
    private fun unregisterActivityLifecycleCallbacks() {
        if (!isRegisteredForLifecycleCallbacks) {
            return
        }
        (appContext as Application).unregisterActivityLifecycleCallbacks(this)
        isRegisteredForLifecycleCallbacks = false
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        if (isStartedFromBackground || onCreateTime != null) {
            return
        }
        onCreateTime = System.currentTimeMillis()
        Log.i(TAG, "TTID first activity creation completed ${onCreateTime!! - appStartTime!!}ms")
        if ((onCreateTime!! - appStartTime!!) > MAX_LATENCY_BEFORE_UI_INIT) {
            isTooLateToInitUI = true
        }
    }

    override fun onActivityStarted(activity: Activity) {}

    override fun onActivityResumed(activity: Activity) {
        if (isStartedFromBackground || isTooLateToInitUI || atLeastOnTimeOnBackground) {
            unregisterActivityLifecycleCallbacks()
            return
        }

        if (activity !is SplashActivity) {
            Log.i(TAG, "TTFD Cold start finished after ${System.currentTimeMillis() - appStartTime!!}ms")

            if (isRegisteredForLifecycleCallbacks) {
                unregisterActivityLifecycleCallbacks()
            }
        }
    }

    override fun onActivityPaused(activity: Activity) {}

    override fun onActivityStopped(activity: Activity) {}

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityDestroyed(activity: Activity) {}

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (event == Lifecycle.Event.ON_STOP) {
            Log.i(TAG, "Remove observer of application")
            atLeastOnTimeOnBackground = true
            ProcessLifecycleOwner.get().lifecycle.removeObserver(this)
        }
    }

    /**
     * We use StartFromBackgroundRunnable to detect if app is started from background or foreground.
     * If app is started from background, we do not generate AppStart trace. This runnable is posted
     * to main UI thread from StartupTimeProvider. If app is started from background, this runnable
     * will be executed before any activity's onCreate() method. If app is started from foreground,
     * activity's onCreate() method is executed before this runnable.
     */
    object StartFromBackgroundRunnable : Runnable {
        override fun run() {
            // if no activity has ever been created.
            if (onCreateTime == null) {
                isStartedFromBackground = true
            }
        }
    }


}