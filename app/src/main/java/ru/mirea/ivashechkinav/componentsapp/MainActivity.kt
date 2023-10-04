package ru.mirea.ivashechkinav.componentsapp

import android.Manifest
import android.content.ComponentName
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    val TAG = this::class.simpleName ?: ""

    lateinit var timerServiceBinder: TimerService.LocalBinder
    var isConnected = false

    val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            timerServiceBinder = binder as TimerService.LocalBinder
            isConnected = true
            initObserver()
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            isConnected = false
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                0
            )
        }
        initTimerService()
        initButtons()
    }

    private fun initTimerService() {
        Intent(this, TimerService::class.java).also {
            bindService(
                it,
                serviceConnection,
                BIND_AUTO_CREATE
            )
        }

    }
    private fun initObserver() {
        if(isConnected) {
            lifecycleScope.launch {
                timerServiceBinder.observeTimeState().collect {
                    findViewById<TextView>(R.id.tvTimer).text = it.toString()
                }
            }
        }
    }
    private fun unbindTimerService() = unbindService(serviceConnection)

    private fun initButtons() {
        findViewById<Button>(R.id.btnStartBackgroundService).setOnClickListener { startBackground() }

        findViewById<Button>(R.id.btnStartForegoundService).setOnClickListener { startForeground() }

        findViewById<Button>(R.id.btnStopForeground).setOnClickListener { stopBackground() }

        findViewById<Button>(R.id.btnStartTimerService).setOnClickListener {
            if(isConnected) timerServiceBinder.startTimer()
        }
        findViewById<Button>(R.id.btnPauseTimerService).setOnClickListener {
            if(isConnected) timerServiceBinder.pauseTimer()
        }
        findViewById<Button>(R.id.btnStopTimerService).setOnClickListener {  }

    }

    private fun startForeground() {
        Log.d(TAG, "Send START foreground service intent")
        Intent(this, MyForegroundService::class.java).also {
            it.action = MyForegroundService.Action.START.toString()
            startService(it)
        }
    }

    private fun startBackground() {
        Log.d(TAG, "Send background service intent")
        Intent(this, MyBackgroundService::class.java).also {
            startService(it)
        }
    }

    private fun stopBackground() {
        Log.d(TAG, "Send STOP foreground service intent")
        Intent(this, MyForegroundService::class.java).also {
            it.action = MyForegroundService.Action.STOP.toString()
            startService(it)
        }
    }

    override fun onDestroy() {
        unbindTimerService()
        super.onDestroy()
    }
}