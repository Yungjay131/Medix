package app.slyworks.base_feature.broadcast_receivers

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import app.slyworks.base_feature.NotificationHelper
import app.slyworks.base_feature._di.CloudMessageBRComponent
import app.slyworks.context_provider_lib.ContextProvider
import app.slyworks.data_lib.helpers.consultations.ConsultationsHelper
import app.slyworks.data_lib.helpers.storage.IUserDetailsHelper
import app.slyworks.data_lib.model.models.ConsultationResponse
import app.slyworks.utils_lib.*
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject


/**
 *Created by Joshua Sylvanus, 11:30 PM, 1/13/2022.
 */
class CloudMessageBroadcastReceiver() : BroadcastReceiver() {

    @Inject
    lateinit var userDetailsHelper: IUserDetailsHelper

    @Inject
    lateinit var consultationRequestsHelper: ConsultationsHelper

    init {
       CloudMessageBRComponent.getInitialBuilder()
           .build()
           .inject(this)
    }

    @SuppressLint("CheckResult")
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
        Single.just(
            ConsultationResponse(
                fromUID = userDetailsHelper.getUserDetailsProperty<String>("firebaseUID")!!,
                toUID = fromUID,
                toFCMRegistrationToken = toFCMRegistrationToken,
                status = responseType,
                fullName = fullName)
           )
            .flatMap {
                consultationRequestsHelper.sendResponseToConsultationRequest(it)
            }
            .observeOn(Schedulers.io())
            .subscribeOn(Schedulers.io())
            .subscribe({
                NotificationHelper.cancelNotification(notificationID, ContextProvider.getContext())
                pendingResult.finish()
            },{
                Timber.e(it)

                NotificationHelper.cancelNotification(notificationID, ContextProvider.getContext())
                pendingResult.finish()
            })


    }

}