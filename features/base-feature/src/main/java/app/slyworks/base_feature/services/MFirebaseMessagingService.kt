package app.slyworks.base_feature.services

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.widget.Toast
import app.slyworks.base_feature.ActivityHelper
import app.slyworks.base_feature.NotificationHelper
import app.slyworks.base_feature.WorkInitializer
import app.slyworks.base_feature._di.MFirebaseMSComponent
import app.slyworks.data_lib.DataManager
import app.slyworks.data_lib.model.view_entities.FBUserDetailsVModel
import app.slyworks.data_lib.model.view_entities.MessageVModel
import app.slyworks.base_feature.network_register.NetworkRegister
import app.slyworks.data_lib.firebase.FirebaseUtils
import app.slyworks.data_lib.helpers.auth.ILoginHelper
import app.slyworks.data_lib.helpers.storage.IUserDetailsHelper
import app.slyworks.data_lib.helpers.users.IUsersHelper
import app.slyworks.data_lib.helpers.users.UsersHelper
import app.slyworks.utils_lib.*
import com.bumptech.glide.Glide
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.CompletableEmitter
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.*
import timber.log.Timber
import java.lang.Exception
import javax.inject.Inject


/**
 *Created by Joshua Sylvanus, 10:09 PM, 11/1/2022.
 */
class MFirebaseMessagingService : FirebaseMessagingService(){
    //region Vars
      @Inject
      lateinit var preferenceHelper: PreferenceHelper

      @Inject
      lateinit var loginHelper: ILoginHelper

      @Inject
      lateinit var notificationHelper: NotificationHelper

      @Inject
      lateinit var userDetailsHelper: IUserDetailsHelper

      @Inject
      lateinit var usersHelper: IUsersHelper

      @Inject
      lateinit var networkRegister: NetworkRegister

      @Inject
      lateinit var workInitializer: WorkInitializer
    //endregion

      init {
         MFirebaseMSComponent.getInitialBuilder()
             .build()
             .inject(this)
      }

    @SuppressLint("CheckResult")
    override fun onNewToken(token: String) {
        super.onNewToken(token)

        preferenceHelper.set(KEY_IS_THERE_NEW_FCM_REG_TOKEN, true)
        preferenceHelper.set(KEY_FCM_REGISTRATION, token)

        if (networkRegister.getNetworkStatus() && loginHelper.getLoggedInStatus()) {
            preferenceHelper.set(KEY_IS_THERE_NEW_FCM_REG_TOKEN, false)
            preferenceHelper.clearPreference(KEY_FCM_REGISTRATION)

            usersHelper.sendFCMTokenToServer(token)
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .subscribe(
                    {
                        preferenceHelper.set(KEY_IS_THERE_NEW_FCM_REG_TOKEN, false)
                        preferenceHelper.clearPreference(KEY_FCM_REGISTRATION)
                    },
                    {
                        Timber.e(it)
                    })
        } else
            /*enqueue task for upload since there is no network connection or user is not logged in */
            workInitializer.initFCMTokenUploadWork(token)
    }

    @SuppressLint("CheckResult")
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
       if(ActivityHelper.isAppInForeground())
            return

       when(remoteMessage.data.get("type")!!){
           FCM_NEW_MESSAGE -> {
               val message: MessageVModel =
                   MessageVModel(
                       type = remoteMessage.data["type"]!!,
                       fromUID = remoteMessage.data["from_uid"]!!,
                       toUID = remoteMessage.data["to_uid"]!!,
                       senderFullName = remoteMessage.data["sender_fullname"]!!,
                       receiverFullName = remoteMessage.data["receiver_fullname"]!!,
                       content = remoteMessage.data["content"]!!,
                       timeStamp = remoteMessage.data["time_stamp"]!!,
                       messageID = remoteMessage.data["message_id"]!!,
                       status = remoteMessage.data["status"]!!.toDouble(),
                       senderImageUri = remoteMessage.data["sender_image_uri"]!!,
                       accountType = remoteMessage.data["account_type"]!!,
                       FCMRegistrationToken = remoteMessage.data["sender_fcm_registration_token"]!!,
                       receiverImageUri = remoteMessage.data["receiver_image_uri"]!!
                   )

               Completable.fromCallable{
                   val bitmap: Bitmap = Glide.with(this@MFirebaseMessagingService.applicationContext)
                       .asBitmap()
                       .load(message.senderImageUri)
                       .submit()
                       .get()
                   notificationHelper.createNewMessageNotification(message, bitmap)
               }
               .observeOn(Schedulers.io())
               .subscribeOn(Schedulers.io())
               .subscribe()
           }

           FCM_REQUEST ->{
             val fromUID:String = remoteMessage.data["fromUID"]!!
             val toFCMRegistrationToken:String = remoteMessage.data["toFCMRegistrationToken"]!!
             val fullName:String = remoteMessage.data["fullName"]!!
             val message:String = remoteMessage.data["message"]!!
             notificationHelper.createConsultationRequestNotification( fromUID, fullName, message, toFCMRegistrationToken)
           }

           FCM_RESPONSE_ACCEPTED, FCM_RESPONSE_DECLINED ->{
               val fromUID:String = remoteMessage.data["fromUID"]!!
               val toUID:String = userDetailsHelper.getUserDetailsProperty<String>("firebaseUID")!!
               val message:String = remoteMessage.data["message"]!!
               val status:String = remoteMessage.data["status"]!!
               val fullName:String = remoteMessage.data["fullName"]!!
               notificationHelper.createConsultationResponseNotification(fromUID, toUID, message, status, fullName)
           }

           FCM_VOICE_CALL_REQUEST ->{
              val details: FBUserDetailsVModel = FBUserDetailsVModel(
                  accountType = remoteMessage.data["accountType"]!!,
                  firstName = remoteMessage.data["firstName"]!!,
                  lastName = remoteMessage.data["lastName"]!!,
                  fullName = remoteMessage.data["fullName"]!!,
                  email = remoteMessage.data["email"]!!,
                  sex = remoteMessage.data["sex"]!!,
                  age = remoteMessage.data["age"]!!,
                  firebaseUID = remoteMessage.data["firebaseUID"]!!,
                  agoraUID = remoteMessage.data["agoraUID"]!!,
                  fcm_registration_token = remoteMessage.data["fcmRegistrationToken"]!!,
                  imageUri = remoteMessage.data["imageUri"]!!,
                  history = null,
                  specialization = null
              )

               Completable.fromCallable{
                   val bitmap: Bitmap = Glide.with(this@MFirebaseMessagingService.applicationContext)
                       .asBitmap()
                       .load(details.imageUri)
                       .submit()
                       .get()
                   notificationHelper.createIncomingVoiceCallNotification(details, bitmap)
               }
               .observeOn(Schedulers.io())
               .subscribeOn(Schedulers.io())
               .subscribe()

           }
           FCM_NEW_UPDATE_MESSAGE ->{
               /* TODO:retrieve new Url to download new version of app from */
           }
       }
    }

    override fun onDeletedMessages() {
        super.onDeletedMessages()
    }

    override fun onMessageSent(p0: String) {
        super.onMessageSent(p0)

        Toast.makeText(this.applicationContext,"cloud message sent", Toast.LENGTH_LONG).show()
    }

    override fun onSendError(p0: String, p1: Exception) {
        super.onSendError(p0, p1)

        Toast.makeText(this.applicationContext,"cloud message not sent", Toast.LENGTH_LONG).show()
    }
}