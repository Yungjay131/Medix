package app.slyworks.base_feature.broadcast_receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import app.slyworks.base_feature._di.VoiceCallRequestBRComponent
import app.slyworks.communication_lib.CallManager
import app.slyworks.utils_lib.EXTRA_INCOMING_VOICE_CALL_FROM_UID
import app.slyworks.utils_lib.EXTRA_INCOMING_VOICE_CALL_RESPONSE_TYPE
import app.slyworks.utils_lib.TYPE_RESPONSE
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject

class VoiceCallRequestBroadcastReceiver : BroadcastReceiver() {
    //region Vars
    @Inject
    lateinit var callManager: CallManager
    //endregion

    init{
        VoiceCallRequestBRComponent.getInitialBuilder()
            .build()
            .inject(this)
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        val pendingResult: PendingResult = goAsync()
        val fromUID: String = intent!!.getStringExtra(EXTRA_INCOMING_VOICE_CALL_FROM_UID)!!
        val response: String = intent.getStringExtra(EXTRA_INCOMING_VOICE_CALL_RESPONSE_TYPE)!!

        callManager.processVoiceCallAsync(TYPE_RESPONSE, fromUID, response)
            .observeOn(Schedulers.io())
            .subscribeOn(Schedulers.io())
            .subscribe { pendingResult.finish() }
    }
}
