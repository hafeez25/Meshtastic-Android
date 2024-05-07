package com.geeksville.mesh.model

import android.graphics.Bitmap
import android.net.Uri
import android.util.Base64
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import com.geeksville.mesh.AppOnlyProtos.ChannelSet
import com.geeksville.mesh.MyNodeInfo
import com.geeksville.mesh.NodeInfo
import com.geeksville.mesh.android.BuildUtils.errormsg
import com.geeksville.mesh.database.dao.NodeInfoDao
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.journeyapps.barcodescanner.BarcodeEncoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import java.net.MalformedURLException
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.jvm.Throws

internal const val URL_PREFIX = "https://meshtastic.org/e/#"
private const val BASE64FLAGS = Base64.URL_SAFE + Base64.NO_WRAP + Base64.NO_PADDING

/**
 * Return a [ChannelSet] that represents the URL
 * @throws MalformedURLException when not recognized as a valid Meshtastic URL
 */
@Throws(MalformedURLException::class)
fun Uri.toChannelSet(): ChannelSet {
    val urlStr = this.toString()

    val pathRegex = Regex("$URL_PREFIX(.*)", RegexOption.IGNORE_CASE)
    val (base64) = pathRegex.find(urlStr)?.destructured
        ?: throw MalformedURLException("Not a Meshtastic URL: ${urlStr.take(40)}")
    val bytes = Base64.decode(base64, BASE64FLAGS)

    return ChannelSet.parseFrom(bytes)
}

/**
 * @return A list of globally unique channel IDs usable with MQTT subscribe()
 */
val ChannelSet.subscribeList: List<String>
    get() = settingsList.filter { it.downlinkEnabled }.map { Channel(it, loraConfig).name }

/**
 * Return the primary channel info
 */
val ChannelSet.primaryChannel: Channel?
    get() = if (settingsCount > 0) Channel(getSettings(0), loraConfig) else null

/**
 * Return a URL that represents the [ChannelSet]
 * @param upperCasePrefix portions of the URL can be upper case to make for more efficient QR codes
 */
fun ChannelSet.getChannelUrl(upperCasePrefix: Boolean = false): Uri {
    val channelBytes = this.toByteArray() ?: ByteArray(0) // if unset just use empty
    val enc = Base64.encodeToString(channelBytes, BASE64FLAGS)
    val p = if (upperCasePrefix) URL_PREFIX.uppercase() else URL_PREFIX
    return Uri.parse("$p$enc")
}

val ChannelSet.qrCode: Bitmap?
    get() = try {
        val multiFormatWriter = MultiFormatWriter()

        val bitMatrix =
            multiFormatWriter.encode(
                getChannelUrl(false).toString(),
                BarcodeFormat.QR_CODE,
                960,
                960
            )
        val barcodeEncoder = BarcodeEncoder()
        barcodeEncoder.createBitmap(bitMatrix)
    } catch (ex: Throwable) {
        errormsg("URL was too complex to render as barcode")
        null
    }

@Singleton
class MyNodeDB @Inject constructor(
    processLifecycle: Lifecycle,
    private val nodeInfoDao: NodeInfoDao,
) {
    private val _myNodeInfo = MutableStateFlow<MyNodeInfo?>(null)
    val myNodeInfo: StateFlow<MyNodeInfo?> get() = _myNodeInfo
    private val _ourNodeInfo = MutableStateFlow<NodeInfo?>(null)
    val ourNodeInfo: StateFlow<NodeInfo?> get() = _ourNodeInfo
    private val _myId = MutableStateFlow<String?>(null)
    val myId: StateFlow<String?> get() = _myId
    private val _nodeDBbyNum = MutableStateFlow<Map<Int, NodeInfo>>(mapOf())
    val nodeDBbyNum: StateFlow<Map<Int, NodeInfo>> get() = _nodeDBbyNum
    private val _nodeDBbyID = MutableStateFlow<Map<String, NodeInfo>>(mapOf())
    val nodeDBbyID: StateFlow<Map<String, NodeInfo>> get() = _nodeDBbyID

    init {
        nodeInfoDao.getMyNodeInfo().onEach { _myNodeInfo.value = it }.launchIn(processLifecycle.coroutineScope)
        nodeInfoDao.nodeDBbyNum().onEach { _nodeDBbyNum.value = it }.launchIn(processLifecycle.coroutineScope)
        nodeInfoDao.nodeDBbyID().onEach { _nodeDBbyID.value = it }.launchIn(processLifecycle.coroutineScope)
    }

    fun myNodeInfoFlow(): Flow<MyNodeInfo?> = nodeInfoDao.getMyNodeInfo()
    fun nodeInfoFlow(): Flow<List<NodeInfo>> = nodeInfoDao.getNodes()
    suspend fun upsert(node: NodeInfo) = withContext(Dispatchers.IO) { nodeInfoDao.upsert(node) }

    suspend fun installNodeDB(mi: MyNodeInfo, nodes: List<NodeInfo>) = withContext(Dispatchers.IO) {
        nodeInfoDao.apply {
            clearNodeInfo()
            clearMyNodeInfo()
            putAll(nodes)
            setMyNodeInfo(mi)
        }
        val ourNodeInfo = nodes.find { it.num == mi.myNodeNum }
        _ourNodeInfo.value = ourNodeInfo
        _myId.value = ourNodeInfo?.user?.id
    }
}