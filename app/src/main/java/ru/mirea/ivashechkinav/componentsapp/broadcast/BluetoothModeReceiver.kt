package ru.mirea.ivashechkinav.componentsapp.broadcast

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast

class BluetoothModeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (arrayOf(
                BluetoothAdapter.ACTION_DISCOVERY_STARTED,
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED
            ).contains(intent?.action)
        ) {
            val action = intent?.action
            when (action) {
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    Toast.makeText(context, "Bluetooth discovery started", Toast.LENGTH_SHORT).show()
                    Log.d(this::class.java.simpleName, "Bluetooth discovery started")
                }

                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    Toast.makeText(context, "Bluetooth discovery finished", Toast.LENGTH_SHORT).show()
                    Log.d(this::class.java.simpleName, "Bluetooth discovery finished")
                }

                else -> {
                    Toast.makeText(context, "Bluetooth Smth", Toast.LENGTH_SHORT).show()
                    Log.d(this::class.java.simpleName, "Bluetooth Smth")
                }
            }
        }
    }
}