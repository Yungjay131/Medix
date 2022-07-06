package com.slyworks.medix.services

import android.graphics.Bitmap
import com.bumptech.glide.Glide
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.slyworks.constants.*
import com.slyworks.medix.App
import com.slyworks.medix.utils.UserDetailsUtils
import com.slyworks.medix.managers.UsersManager
import com.slyworks.medix.managers.NotificationHelper
import com.slyworks.medix.managers.PreferenceManager
import com.slyworks.models.room_models.FBUserDetails
import com.slyworks.network.NetworkRegister
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.Exception


/**
 *Created by Joshua Sylvanus, 10:09 PM, 1/11/2022.
 */
class MFirebaseMessagingService : FirebaseMessagingService(){
    //region Vars
    //endregion

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        GlobalScope.launch(Dispatchers.IO) {
            /*TODO:save offline to DB, maybe use WorkManager*/
            if(NetworkRegister(App.getContext()).getNetworkStatus()) {
                PreferenceManager.set(KEY_FCM_REGISTRATION, token)
                UsersManager.sendFCMTokenToServer(token)
            }else{
                /*enqueue task for upload since there is no network connection*/
                PreferenceManager.set(KEY_FCM_REGISTRATION, token)
                App.initFCMTokenUploadWork(token)
            }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
       when(getMessageType(remoteMessage)){
           FCM_REQUEST ->{
             val fromUID:String = remoteMessage.data["fromUID"]!!
             val toFCMRegistrationToken:String = remoteMessage.data["toFCMRegistrationToken"]!!
             val fullName:String = remoteMessage.data["fullName"]!!
             val message:String = remoteMessage.data["message"]!!
             NotificationHelper.createConsultationRequestNotification( fromUID, fullName, message, toFCMRegistrationToken)
           }
           FCM_RESPONSE_ACCEPTED, FCM_RESPONSE_DECLINED ->{
               val fromUID:String = remoteMessage.data["fromUID"]!!
               val toUID:String = UserDetailsUtils.user!!.firebaseUID
               val message:String = remoteMessage.data["message"]!!
               val status:String = remoteMessage.data["status"]!!
               val fullName:String = remoteMessage.data["fullName"]!!
               NotificationHelper.createConsultationResponseNotification( fromUID,toUID, message, status, fullName)
           }
           FCM_VOICE_CALL_REQUEST ->{
              val details: FBUserDetails = FBUserDetails(
                  accountType = remoteMessage.data["accountType"]!!,
                  firstName = remoteMessage.data["firstName"]!!,
                  lastName = remoteMessage.data["lastName"]!!,
                  fullName = remoteMessage.data["fullName"]!!,
                  email = remoteMessage.data["email"]!!,
                  sex = remoteMessage.data["sex"]!!,
                  age = remoteMessage.data["age"]!!,
                  firebaseUID = remoteMessage.data["firebaseUID"]!!,
                  agoraUID = remoteMessage.data["agoraUID"]!!,
                  FCMRegistrationToken = remoteMessage.data["fcmRegistrationToken"]!!,
                  imageUri = remoteMessage.data["imageUri"]!!,
                  history = null,
                  specialization = null
              )

               CoroutineScope(Dispatchers.IO).launch {
                 val bitmap: Bitmap = Glide.with(App.getContext())
                     .asBitmap()
                     .load(details.imageUri)
                     .submit()
                     .get()

                 NotificationHelper.createIncomingVoiceCallNotification(bitmap, details)
               }
           }
           FCM_NEW_UPDATE_MESSAGE ->{
               /*TODO:retrieve new Url to download new app from*/
           }
       }
    }

    private fun getMessageType(remoteMessage:RemoteMessage):String{
        return remoteMessage.data.get("type")!!
    }

    override fun onDeletedMessages() {
        super.onDeletedMessages()
    }

    override fun onMessageSent(p0: String) {
        super.onMessageSent(p0)
    }

    override fun onSendError(p0: String, p1: Exception) {
        super.onSendError(p0, p1)
    }
}