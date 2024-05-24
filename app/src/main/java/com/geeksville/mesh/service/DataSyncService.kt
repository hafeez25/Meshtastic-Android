package com.geeksville.mesh.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.geeksville.mesh.NodeInfo
import com.geeksville.mesh.database.PacketRepository
import com.geeksville.mesh.model.MyNodeDB
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import javax.inject.Inject

@AndroidEntryPoint
class DataSyncService : Service() {

    @Inject
    lateinit var packetRepository: PacketRepository
    @Inject
    lateinit var myNodeDB: MyNodeDB
    private val serviceScope = CoroutineScope(Dispatchers.IO)
    private val intervalMillis = 4000L

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        serviceScope.launch {
//            while (isActive) {
//                try {
//                    val waypointsJson = packetRepository.getWaypoints()
//                   if(waypointsJson.length>0) sendHttpPostRequest("https://loramesh.linear-amptech.com/data/waypoint", waypointsJson)
//                    Log.i("DataSync", waypointsJson)
//
//                    val markersJson = myNodeDB.getAllNodes();
//                    Log.i("DataSync",markersJson)
//                    if(markersJson.length>0)
//                    sendHttpPostRequest("https://loramesh.linear-amptech.com/data/marker",markersJson)
//
//
//
//
//                } catch (e: Exception) {
//                    Log.e("DataSync", "Error in sending data: ${e.localizedMessage}")
//                }
////                Log.i("DataSync", "Service is running")
//                delay(intervalMillis)
//            }
        }
        return START_NOT_STICKY
    }

    fun sendHttpPostRequest(url: String, body: String) {
        val client = OkHttpClient()
        val requestBody = body.toRequestBody("application/json".toMediaTypeOrNull())
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .addHeader("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyIjp7Imdyb3VwcyI6W10sIl9pZCI6IjY2MTJhNTE5Yzk4YWUxMDg1NTQ5ZmYwMSIsIm5hbWUiOiJoZWxsbyIsInBhc3N3b3JkIjoiJDJiJDEwJGFSYi5jVlBKcmtFdlFYZm9tdEc1a3VvTW54LkozUnI5emVYTVRXUGRaLk5WSDE3S09SMXQuIiwiZW1haWwiOiJhYmNAZ21haWwuY29tIiwiX192IjowfSwiaWF0IjoxNzEzMzgwMjMyfQ.WlnxsS4IZKFQMsd8faKQkr-XVJW2PicU9cs_lseI-yM")
            .addHeader("Source","Phone")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("DataSync", "HTTP request failed: ${e.localizedMessage}")
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        Log.e("DataSync", "HTTP request unsuccessful: ${response}")

                    } else {
//                        Log.i("DataSync", "HTTP request successful")
                        Log.i("DataSync",url);

                    }
                }
            }
        })
    }


    fun convertNodeInfoListToJson(nodeInfoList: List<NodeInfo>): String {
        val gson = Gson()
        return gson.toJson(nodeInfoList)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}