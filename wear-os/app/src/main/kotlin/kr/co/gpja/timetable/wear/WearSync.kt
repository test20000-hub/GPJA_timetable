package kr.co.gpja.timetable.wear

import android.content.Context
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataItem
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

object WearSync {
    const val PATH_SYNC = "/gpja/sync"
    const val PATH_REQUEST_SYNC = "/gpja/request/sync"
    const val PATH_ACK = "/gpja/ack"
    const val PROTOCOL_VERSION = 1

    suspend fun requestSync(context: Context) {
        val nodes = Wearable.getNodeClient(context).connectedNodes.await()
        require(nodes.isNotEmpty()) { "휴대폰 앱이 연결되어 있지 않습니다." }
        nodes.forEach { node ->
            Wearable.getMessageClient(context)
                .sendMessage(node.id, PATH_REQUEST_SYNC, PROTOCOL_VERSION.toString().toByteArray())
                .await()
        }
    }

    suspend fun sendAck(context: Context, sourceNodeId: String) {
        Wearable.getMessageClient(context)
            .sendMessage(sourceNodeId, PATH_ACK, PROTOCOL_VERSION.toString().toByteArray())
            .await()
    }
}

class PhoneSyncListenerService : WearableListenerService() {
    override fun onDataChanged(events: DataEventBuffer) {
        events.forEach { event ->
            if (event.type != DataEvent.TYPE_CHANGED) return@forEach
            if (event.dataItem.uri.path != WearSync.PATH_SYNC) return@forEach
            val map = com.google.android.gms.wearable.DataMapItem.fromDataItem(event.dataItem).dataMap
            val protocol = map.getInt("protocolVersion", 0)
            if (protocol != WearSync.PROTOCOL_VERSION) return@forEach
            getSharedPreferences("wear_sync", Context.MODE_PRIVATE).edit()
                .putString("date", map.getString("date", ""))
                .putString("scheduleJson", map.getString("scheduleJson", "[]"))
                .putString("mealJson", map.getString("mealJson", "{\"ok\":false,\"data\":[]}"))
                .putLong("sentAt", map.getLong("sentAt", 0L))
                .putLong("receivedAt", System.currentTimeMillis())
                .apply()
            CoroutineScope(Dispatchers.IO).launch {
                runCatching { WearSync.sendAck(this@PhoneSyncListenerService, event.dataItem.uri.host ?: return@launch) }
            }
        }
    }
}
