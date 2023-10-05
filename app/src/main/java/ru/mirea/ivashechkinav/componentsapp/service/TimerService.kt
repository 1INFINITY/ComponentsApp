package ru.mirea.ivashechkinav.componentsapp.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class CountDownTimer(scope: CoroutineScope) {

    private val _timeState = MutableStateFlow(MAX_COUNT)
    val timeState = _timeState.asStateFlow()

    private var isPaused = AtomicBoolean(true)
    private val lock = ReentrantLock()
    private val condition = lock.newCondition()

    init {
        scope.launch {
            repeat(MAX_COUNT) {
                delay(DELAY_MILLIS)
                lock.withLock {
                    if (isPaused.get()) {
                        Log.d("TimerService", "Suspend Countdown")
                        condition.await()
                    }
                    _timeState.value = MAX_COUNT - it - 1
                    Log.d("TimerService", "Countdown - ${MAX_COUNT - it - 1}")
                }
            }
        }
    }

    fun resume() {
        isPaused.set(false)
        lock.withLock { condition.signal() }
    }

    fun pause() {
        isPaused.set(true)
    }

    companion object {
        const val MAX_COUNT = 100
        const val DELAY_MILLIS = 500L
    }
}

class TimerService : Service() {
    private val serviceScope = CoroutineScope(Dispatchers.IO)
    private val timer = CountDownTimer(serviceScope)

    override fun onCreate() {
        super.onCreate()
        Log.d("TimerService", "Service created")
    }

    override fun onBind(intent: Intent): IBinder {
        return LocalBinder()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        Log.d("TimerService", "Service destroyed")
    }

    inner class LocalBinder : Binder() {

        fun observeTimeState() = timer.timeState

        fun startTimer() = timer.resume()

        fun pauseTimer() = timer.pause()

        fun cancelService() {}
    }
}