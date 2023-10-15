package app.slyworks.base_feature.broadcast_receivers

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import app.slyworks.base_feature._di.VideoCallRequestBRComponent
import app.slyworks.data_lib.helpers.call.ICallHelper
import app.slyworks.utils_lib.EXTRA_INCOMING_VIDEO_CALL_FROM_UID
import app.slyworks.utils_lib.EXTRA_INCOMING_VIDEO_CALL_RESPONSE_TYPE
import app.slyworks.utils_lib.TYPE_RESPONSE
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject


/**
 *Created by Joshua Sylvanus, 12:00 PM, 1/22/2022.
 */
class VideoCallRequestBroadcastReceiver : BroadcastReceiver() {
    //region Vars
    @Inject
    lateinit var callHelper: ICallHelper
    //endregion

    init {
        VideoCallRequestBRComponent.getInitialBuilder()
            .build()
            .inject(this)
    }

    @SuppressLint("CheckResult")
    override fun onReceive(context: Context?, intent: Intent?) {
        val pendingResult:PendingResult = goAsync()

        val fromUID:String = intent!!.getStringExtra(EXTRA_INCOMING_VIDEO_CALL_FROM_UID)!!
        val response:String = intent.getStringExtra(EXTRA_INCOMING_VIDEO_CALL_RESPONSE_TYPE)!!

        callHelper.respondToVideoCall(fromUID,response)
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe({
                pendingResult.finish()
            },
            {
                Timber.e(it)

                pendingResult.finish()
            })
    }

}