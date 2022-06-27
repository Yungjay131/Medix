package com.slyworks.medix.broadcast_receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.slyworks.constants.EXTRA_INCOMING_VOICE_CALL_FROM_UID
import com.slyworks.constants.EXTRA_INCOMING_VOICE_CALL_RESPONSE_TYPE
import com.slyworks.constants.TYPE_RESPONSE
import com.slyworks.medix.managers.CallManager
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class VoiceCallRequestBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val pendingResult:PendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            val fromUID:String = intent!!.getStringExtra(EXTRA_INCOMING_VOICE_CALL_FROM_UID)!!
            val response:String = intent.getStringExtra(EXTRA_INCOMING_VOICE_CALL_RESPONSE_TYPE)!!

            CallManager.processVoiceCallAsync(TYPE_RESPONSE, fromUID, response)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe{ pendingResult.finish() }
        }
    }
}
