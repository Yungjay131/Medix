package com.slyworks.medix.broadcast_receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.slyworks.constants.*
import com.slyworks.medix.helpers.NotificationHelper
import com.slyworks.communication.CloudMessageManager
import com.slyworks.models.models.ConsultationResponse
import com.slyworks.userdetails.UserDetailsUtils
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject


/**
 *Created by Joshua Sylvanus, 11:30 PM, 1/13/2022.
 */
class CloudMessageBroadcastReceiver() : BroadcastReceiver() {

    @Inject
    lateinit var userDetailsUtils: UserDetailsUtils
    @Inject
    lateinit var cloudMessageManager: CloudMessageManager

    override fun onReceive(context: Context?, intent: Intent?) {
        val fromUID:String = intent!!.getStringExtra(EXTRA_CLOUD_MESSAGE_FROM_UID)!!
        val toFCMRegistrationToken:String = intent.getStringExtra(EXTRA_CLOUD_MESSAGE_TO_FCMTOKEN)!!
        val fullName:String = intent.getStringExtra(EXTRA_CLOUD_MESSAGE_FULLNAME)!!
        val notificationID:Int = intent.getIntExtra(EXTRA_NOTIFICATION_IDENTIFIER, -1)

        var responseType:String = REQUEST_PENDING
        val response_accept = intent.getStringExtra(EXTRA_CLOUD_MESSAGE_TYPE_ACCEPT)
        val response_decline = intent.getStringExtra(EXTRA_CLOUD_MESSAGE_TYPE_DECLINE)

        if(response_accept == null){
            responseType = response_decline!!
        }else if(response_decline == null)
            responseType = response_accept

        val pendingResult: PendingResult = goAsync()
        Observable.just(
            ConsultationResponse(
                fromUID = userDetailsUtils.user!!.firebaseUID,
                toUID = fromUID,
                toFCMRegistrationToken = toFCMRegistrationToken,
                status = responseType,
                fullName = fullName)
           )
            .observeOn(Schedulers.io())
            .flatMap {
                cloudMessageManager.sendConsultationRequestResponse(it)
            }
            .subscribeOn(Schedulers.io())
            .subscribe{
                NotificationHelper.cancelNotification(notificationID)
                pendingResult.finish()
            }


    }

}