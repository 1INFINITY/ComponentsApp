package ru.mirea.ivashechkinav.componentsapp

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.mirea.ivashechkinav.componentsapp.App.Companion.CHANNEL_ID

class MyForegroundService : Service() {

    private val TAG = this::class.simpleName ?: ""
    private val serviceScope = CoroutineScope(Dispatchers.IO)
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            Action.START.toString() -> {
                Log.d(TAG, "Get command START")
                start()
            }
            Action.STOP.toString() ->  {
                Log.d(TAG, "Get command STOP")
                stopSelf()
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.d(TAG, "Service bind")
        return null
    }

    private fun start() {

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Running")
            .setContentText("Foreground service is active")
            .build()
        startForeground(1, notification)
        serviceScope.launch {
            repeat(20) {
                delay(1000)
                Log.d(TAG, "$it - foreground counter")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service destroy")
        serviceScope.cancel()
    }

    enum class Action {
        START, STOP
    }
}