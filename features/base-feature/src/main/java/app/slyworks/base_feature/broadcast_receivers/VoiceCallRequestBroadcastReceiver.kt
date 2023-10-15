package app.slyworks.base_feature.broadcast_receivers

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import app.slyworks.base_feature._di.VoiceCallRequestBRComponent
import app.slyworks.data_lib.helpers.call.ICallHelper
import app.slyworks.utils_lib.EXTRA_INCOMING_VOICE_CALL_FROM_UID
import app.slyworks.utils_lib.EXTRA_INCOMING_VOICE_CALL_RESPONSE_TYPE
import io.reactivex.rxjava3.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

class VoiceCallRequestBroadcastReceiver : BroadcastReceiver() {
    //region Vars
    @Inject
    lateinit var callHelper: ICallHelper
    //endregion

    init{
        VoiceCallRequestBRComponent.getInitialBuilder()
            .build()
            .inject(this)
    }

    @SuppressLint("CheckResult")
    override fun onReceive(context: Context?, intent: Intent?) {
        val pendingResult: PendingResult = goAsync()
        val fromUID: String = intent!!.getStringExtra(EXTRA_INCOMING_VOICE_CALL_FROM_UID)!!
        val response: String = intent.getStringExtra(EXTRA_INCOMING_VOICE_CALL_RESPONSE_TYPE)!!

        callHelper.respondToVoiceCall(fromUID, response)
            .observeOn(Schedulers.io())
            .subscribeOn(Schedulers.io())
            .subscribe(
                {
                    pendingResult.finish()
                },{
                    Timber.e(it)

                    pendingResult.finish()
                })
    }
}
