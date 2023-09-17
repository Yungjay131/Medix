package app.slyworks.base_feature.broadcast_receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import app.slyworks.base_feature._di.VideoCallRequestBRComponent
import app.slyworks.communication_lib.CallManager
import app.slyworks.utils_lib.EXTRA_INCOMING_VIDEO_CALL_FROM_UID
import app.slyworks.utils_lib.EXTRA_INCOMING_VIDEO_CALL_RESPONSE_TYPE
import app.slyworks.utils_lib.TYPE_RESPONSE
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject


/**
 *Created by Joshua Sylvanus, 12:00 PM, 1/22/2022.
 */
class VideoCallRequestBroadcastReceiver : BroadcastReceiver() {
    //region Vars
    @Inject
    lateinit var callManager: CallManager
    //endregion
    init {
        VideoCallRequestBRComponent.getInitialBuilder()
            .build()
            .inject(this)
    }

    override fun onReceive(context: Context?, intent: Intent?) {

        val pendingResult:PendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            val fromUID:String = intent!!.getStringExtra(EXTRA_INCOMING_VIDEO_CALL_FROM_UID)!!
            val response:String = intent.getStringExtra(EXTRA_INCOMING_VIDEO_CALL_RESPONSE_TYPE)!!

           callManager.processVideoCallAsync(TYPE_RESPONSE, fromUID, response)
               .subscribeOn(Schedulers.io())
               .observeOn(Schedulers.io())
               .subscribe { pendingResult.finish() }
        }
    }

}