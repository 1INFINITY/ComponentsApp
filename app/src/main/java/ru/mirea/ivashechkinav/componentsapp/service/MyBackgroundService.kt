package ru.mirea.ivashechkinav.componentsapp.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MyBackgroundService: Service() {

    val TAG = this::class.simpleName ?: ""
    private val serviceScope = CoroutineScope(Dispatchers.IO)
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service started")
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Get command")
        serviceScope.launch {
            repeat(10) {
                delay(500)
                Log.d(TAG, "$it")
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.d(TAG, "Service bind")
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service destroy")
        serviceScope.cancel()
    }
}