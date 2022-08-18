package com.slyworks.medix.services

import android.graphics.Bitmap
import com.bumptech.glide.Glide
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.slyworks.auth.LoginManager
import com.slyworks.auth.UsersManager
import com.slyworks.constants.*
import com.slyworks.medix.App
import com.slyworks.medix.appComponent
import com.slyworks.medix.helpers.NotificationHelper
import com.slyworks.models.room_models.FBUserDetails
import com.slyworks.utils.PreferenceManager
import com.slyworks.network.NetworkRegister
import com.slyworks.userdetails.UserDetailsUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.Exception
import javax.inject.Inject


/**
 *Created by Joshua Sylvanus, 10:09 PM, 1/11/2022.
 */
class MFirebaseMessagingService : FirebaseMessagingService(){
    //region Vars
      @Inject
      lateinit var preferenceManager: PreferenceManager
      @Inject
      lateinit var loginManager: LoginManager
      @Inject
      lateinit var usersManager: UsersManager
      @Inject
      lateinit var notificationHelper: NotificationHelper
      @Inject
      lateinit var userDetailsUtils: UserDetailsUtils
    //endregion

      init {
         application.appComponent
             .serviceComponentBuilder()
             .build()
             .inject(this)
      }

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        GlobalScope.launch(Dispatchers.IO) {
            //TODO:save offline to DB, maybe use WorkManager
            if(NetworkRegister(App.getContext()).getNetworkStatus()) {
                preferenceManager.set(KEY_FCM_REGISTRATION, token)

                if(loginManager.getLoginStatus())
                   usersManager.sendFCMTokenToServer(token)
                else
                    App.initFCMTokenUploadWork(token)
            }else{
                /* enqueue task for upload since there is no network connection */
                preferenceManager.set(KEY_FCM_REGISTRATION, token)
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
             notificationHelper.createConsultationRequestNotification( fromUID, fullName, message, toFCMRegistrationToken)
           }

           FCM_RESPONSE_ACCEPTED, FCM_RESPONSE_DECLINED ->{
               val fromUID:String = remoteMessage.data["fromUID"]!!
               val toUID:String = userDetailsUtils.user!!.firebaseUID
               val message:String = remoteMessage.data["message"]!!
               val status:String = remoteMessage.data["status"]!!
               val fullName:String = remoteMessage.data["fullName"]!!
               notificationHelper.createConsultationResponseNotification(fromUID, toUID, message, status, fullName)
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

                 notificationHelper.createIncomingVoiceCallNotification(bitmap, details)
               }
           }
           FCM_NEW_UPDATE_MESSAGE ->{
               /*TODO:retrieve new Url to download new version of app from*/
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