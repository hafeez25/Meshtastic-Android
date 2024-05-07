package com.geeksville.mesh.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log

import com.geeksville.mesh.MeshProtos
import com.geeksville.mesh.NodeInfo
import com.geeksville.mesh.database.PacketRepository
import com.geeksville.mesh.model.MyNodeDB
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
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

            try {

                while (isActive) {

                    launch {
                        val waypointsJson = packetRepository.getWaypoints()
                        com.geeksville.mesh.ui.map.sendHttpPostRequest(
                            "https://loramesh.linear-amptech.com/data/waypoint",
                            waypointsJson
                        )

//                        Log.i("DataSync",waypointsJson)

                        myNodeDB.nodeDBbyID.collect { nodeMap ->
                            val nodeInfoList = nodeMap.values.toList()
                            if (nodeInfoList.isNotEmpty()) {
                                val json = convertNodeInfoListToJson(nodeInfoList)
                                    sendHttpPostRequest("https://loramesh.linear-amptech.com/data/marker",json)
                            }
                        }






                    }
                   Log.i("DataSync","Send Data to Server")
                    delay(intervalMillis)
                }
            } catch (e: Exception) {
                Log.e("DataSync", "Error in background service: ${e.message}")
            }
        }
        return START_NOT_STICKY // Consider using START_NOT_STICKY irf you do not want the service to estart automatically
    }





    fun sendHttpPostRequest(url: String, body: String, ) {
        val client = OkHttpClient()
        val requestBody = body.toRequestBody("application/json".toMediaTypeOrNull())
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .addHeader("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyIjp7Imdyb3VwcyI6W10sIl9pZCI6IjY2MTJhNTE5Yzk4YWUxMDg1NTQ5ZmYwMSIsIm5hbWUiOiJoZWxsbyIsInBhc3N3b3JkIjoiJDJiJDEwJGFSYi5jVlBKcmtFdlFYZm9tdEc1a3VvTW54LkozUnI5emVYTVRXUGRaLk5WSDE3S09SMXQuIiwiZW1haWwiOiJhYmNAZ21haWwuY29tIiwiX192IjowfSwiaWF0IjoxNzEzMzgwMjMyfQ.WlnxsS4IZKFQMsd8faKQkr-XVJW2PicU9cs_lseI-yM")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Handle failure
                Log.i("hafizur", e.toString())
            }

            override fun onResponse(call: Call, response: Response) {
                // Handle response
                val responseBody = response.body?.string()
                responseBody?.let {
                    val jsonObject = JSONObject(it)
                    val success = jsonObject.getBoolean("success")
                    Log.i("hafizur", "Success: $success")

                    // Log other keys and values
                    val keys = jsonObject.keys()
                    while (keys.hasNext()) {
                        val key = keys.next()
                        val value = jsonObject.get(key)
                        Log.i("hafizur", "$key: $value")
                    }
                }
            }
        })
    }
    fun convertWaypointListToJson(waypoint: MeshProtos.Waypoint): String {
        val gson = Gson()
        return gson.toJson(waypoint)
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
