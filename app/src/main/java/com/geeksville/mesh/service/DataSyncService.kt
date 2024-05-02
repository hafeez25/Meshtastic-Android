package com.geeksville.mesh.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.*

class DataSyncService : Service() {
    private val serviceScope = CoroutineScope(Dispatchers.IO)
    private val intervalMillis = 6000L // Adjust the interval as needed

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        serviceScope.launch {
            try {
                while (isActive) {
                    sendDataToServer()
                    delay(intervalMillis)
                }
            } catch (e: Exception) {
                Log.e("DataSync", "Error in background service: ${e.message}")
            }
        }
        return START_NOT_STICKY // Consider using START_NOT_STICKY irf you do not want the service to estart automatically
    }

    private suspend fun sendDataToServer() {
        // Your code to send data to the server
        Log.i("DataSync", "Hello from DataSyncService")
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
