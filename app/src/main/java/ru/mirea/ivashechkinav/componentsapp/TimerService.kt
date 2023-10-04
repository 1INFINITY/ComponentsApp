package ru.mirea.ivashechkinav.componentsapp

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class TimerService : Service() {

    private val TAG = this::class.simpleName ?: ""

    private val serviceScope = CoroutineScope(Dispatchers.IO)
    private val timeState = MutableStateFlow<Int>(0)
    private var isPaused = AtomicBoolean(true)
    private val lock = ReentrantLock()
    private val condition = lock.newCondition()

    init {
        serviceScope.launch {
            repeat(100) {
                delay(500)
                lock.withLock {
                    if (isPaused.get()) {
                        Log.d(TAG, "Suspend Countdown")
                        condition.await()
                    }
                    timeState.value = 99 - it
                    Log.d(TAG, "Countdown - ${99 - it}")
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")
    }

    override fun onBind(intent: Intent): IBinder {
        return LocalBinder()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service destroyed")
    }

    inner class LocalBinder : Binder() {

        fun observeTimeState() = timeState.asStateFlow()

        fun startTimer() {
            isPaused.set(false)
            lock.withLock { condition.signal() }
        }

        fun pauseTimer() {
            isPaused.set(true)
        }

        fun cancelService() {}
    }
}