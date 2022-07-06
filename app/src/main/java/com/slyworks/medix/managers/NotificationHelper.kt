package com.slyworks.medix.managers

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.RingtoneManager
import android.os.Bundle
import android.os.Parcelable
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.*
import androidx.core.content.ContextCompat
import com.slyworks.constants.*
import com.slyworks.medix.App
import com.slyworks.medix.R
import com.slyworks.medix.ui.activities.mainActivity.MainActivity
import com.slyworks.medix.ui.activities.videoCallActivity.VideoCallActivity
import com.slyworks.medix.ui.activities.requestsActivity.RequestsActivity
import com.slyworks.medix.broadcast_receivers.CloudMessageBroadcastReceiver
import com.slyworks.medix.broadcast_receivers.VideoCallRequestBroadcastReceiver
import com.slyworks.medix.broadcast_receivers.VoiceCallRequestBroadcastReceiver
import com.slyworks.medix.navigation.FragmentWrapper
import com.slyworks.medix.ui.activities.voiceCallActivity.VoiceCallActivity
import com.slyworks.models.room_models.FBUserDetails


/**
 *Created by Joshua Sylvanus, 11:45 PM, 1/11/2022.
 */
object NotificationHelper {
    //region Vars
    private val notificationManager:NotificationManager =
        App.getContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private val mColor:Int = ContextCompat.getColor(App.getContext(), R.color.appBlue)

    private val mChannelID1:String = App.getContext().getString(R.string.notification_channel_1_id)
    private val mChannelID2:String = App.getContext().getString(R.string.notification_channel_2_id)
    //endregion

    fun cancelNotification(notificationID:Int) = notificationManager.cancel(notificationID)

    fun createConsultationResponseNotification(fromUID:String,
                                               toUID:String,
                                               message:String,
                                               status:String,
                                               fullName:String){
        val intent = Intent(App.getContext(), RequestsActivity::class.java).apply {
            val b:Bundle = Bundle().apply {
                putString(EXTRA_CLOUD_MESSAGE_FROM_UID, fromUID)
                putString(EXTRA_CLOUD_MESSAGE_TO_UID, toUID)
                putString(EXTRA_CLOUD_MESSAGE_STATUS, status)
                putString(EXTRA_CLOUD_MESSAGE_FULLNAME, fullName)
            }
            putExtra(EXTRA_ACTIVITY, b)
            setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

            notificationManager.cancel(NOTIFICATION_CONSULTATION_REQUEST_RESPONSE)
        }

        val pendingIntent: PendingIntent = PendingIntent.getActivity(App.getContext(), 0, intent,  PendingIntent.FLAG_CANCEL_CURRENT)

        val builder:NotificationCompat.Builder = NotificationCompat.Builder(App.getContext(), mChannelID2)

        builder.setSmallIcon(R.drawable.splash_image_2)
            .setChannelId(mChannelID2)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setContentTitle("Consultation Request Response")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText(message))
            .setColor(mColor)
            .setAutoCancel(true)
            .setWhen(System.currentTimeMillis())
            .setVisibility(VISIBILITY_PUBLIC)
            .setContentIntent(pendingIntent)


        notificationManager.notify(NOTIFICATION_CONSULTATION_REQUEST_RESPONSE, builder.build())
    }

    fun createConsultationRequestNotification(fromUID:String,
                                              toFCMRegistrationToken:String,
                                              fullName:String,
                                              message:String){

        val intent = Intent(App.getContext(), RequestsActivity::class.java).apply {
            val b:Bundle = Bundle().apply {
                putString(EXTRA_CLOUD_MESSAGE_FROM_UID, fromUID)
                putString(EXTRA_CLOUD_MESSAGE_STATUS, REQUEST_PENDING)
                putString(EXTRA_CLOUD_MESSAGE_FULLNAME, fullName)
                putString(EXTRA_CLOUD_MESSAGE_TO_FCMTOKEN, toFCMRegistrationToken)
            }
            putExtra(EXTRA_ACTIVITY, b)
            setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

            notificationManager.cancel(NOTIFICATION_CONSULTATION_REQUEST)
        }


        val intentAccept = Intent(App.getContext(), CloudMessageBroadcastReceiver::class.java).apply {
            putExtra(EXTRA_CLOUD_MESSAGE_FROM_UID, fromUID)
            putExtra(EXTRA_CLOUD_MESSAGE_TYPE_ACCEPT, REQUEST_ACCEPTED)
            putExtra(EXTRA_CLOUD_MESSAGE_FULLNAME, fullName)
            putExtra(EXTRA_CLOUD_MESSAGE_TO_FCMTOKEN, toFCMRegistrationToken)
            putExtra(EXTRA_NOTIFICATION_IDENTIFIER, NOTIFICATION_CONSULTATION_REQUEST)

            setAction("com.slyworks.medix.BROADCAST_CLOUD_MESSAGE")

            notificationManager.cancel(NOTIFICATION_CONSULTATION_REQUEST)
        }

        val intentDecline = Intent(App.getContext(), CloudMessageBroadcastReceiver::class.java).apply {
            putExtra(EXTRA_CLOUD_MESSAGE_FROM_UID, fromUID)
            putExtra(EXTRA_CLOUD_MESSAGE_TYPE_DECLINE, REQUEST_DECLINED)
            putExtra(EXTRA_CLOUD_MESSAGE_FULLNAME, fullName)
            putExtra(EXTRA_CLOUD_MESSAGE_TO_FCMTOKEN, toFCMRegistrationToken)
            putExtra(EXTRA_NOTIFICATION_IDENTIFIER, NOTIFICATION_CONSULTATION_REQUEST)

            setAction("com.slyworks.medix.BROADCAST_CLOUD_MESSAGE")

            notificationManager.cancel(NOTIFICATION_CONSULTATION_REQUEST)
        }

        val pendingIntent: PendingIntent = PendingIntent.getActivity(App.getContext(), 100, intent,  PendingIntent.FLAG_CANCEL_CURRENT)
        val pendingIntentAccept:PendingIntent = PendingIntent.getBroadcast(App.getContext(), 1, intentAccept, PendingIntent.FLAG_CANCEL_CURRENT)
        val pendingIntentDecline:PendingIntent = PendingIntent.getBroadcast(App.getContext(), 2, intentDecline, PendingIntent.FLAG_CANCEL_CURRENT)

        val builder:NotificationCompat.Builder = NotificationCompat.Builder(App.getContext(), mChannelID2)

        builder.setSmallIcon(R.drawable.splash_image_2)
               .setChannelId(mChannelID2)
               .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
               .setContentTitle("Consultation Request")
               .setStyle(NotificationCompat.BigTextStyle()
                                           .bigText(message))
               .setColor(mColor)
               .setAutoCancel(true)
               .setWhen(System.currentTimeMillis())
               .setVisibility(VISIBILITY_PUBLIC)
               .setContentIntent(pendingIntent)
               .addAction(R.drawable.ic_check2,"Accept Request",pendingIntentAccept)
               .addAction(R.drawable.ic_close2,"Decline Request",pendingIntentDecline)


        val notification = builder.build()
        notification.flags = notification.flags or Notification.FLAG_AUTO_CANCEL

        notificationManager.notify(NOTIFICATION_CONSULTATION_REQUEST, notification)
    }

    fun createAppServiceNotification(): Notification {
        val builder:NotificationCompat.Builder = NotificationCompat.Builder(App.getContext(), mChannelID1)

        builder.setSmallIcon(R.drawable.splash_image_2)
            .setChannelId(mChannelID1)
            .setContentText("Medix is doing work in the background")
            .setColor(mColor)
            .setWhen(System.currentTimeMillis())
            .setAllowSystemGeneratedContextualActions(false)
            .setVisibility(VISIBILITY_PUBLIC)

        return builder.build()
    }

    fun createReceivedMessageNotification(){
        val intent = Intent(App.getContext(), MainActivity::class.java).apply {
            putExtra(EXTRA_MAIN_FRAGMENT, FragmentWrapper.CHAT_HOST as Parcelable)
            setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

            notificationManager.cancel(NOTIFICATION_NEW_MESSAGE)
        }

        val pendingIntent: PendingIntent = PendingIntent.getActivity(App.getContext(), 8,
            intent, PendingIntent.FLAG_CANCEL_CURRENT )

        val remoteView:RemoteViews = RemoteViews(App.getContext().packageName, R.layout.layout_message_heads_up)
        remoteView.setTextViewText(R.id.tvTitle_notification_heads_up, App.getContext().resources.getString(R.string.new_message_received))
        remoteView.setTextViewText(R.id.tvText_notification_heads_up, App.getContext().resources.getString(R.string.new_message_text))

        val builder:NotificationCompat.Builder = NotificationCompat.Builder(App.getContext(), mChannelID2)

        builder.setSmallIcon(R.drawable.splash_image_2)
            .setChannelId(mChannelID2)
           /* .setLargeIcon(BitmapFactory.decodeResource(
                App.getContext().getResources(),
                R.drawable.splash_image_2))*/
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setContentTitle("New Message Received")
            .setContentText("You have new messages. Tap to open")
            .setCustomHeadsUpContentView(remoteView)
            .setAutoCancel(true)
            .setColor(mColor)
            .setWhen(System.currentTimeMillis())
            .setVisibility(VISIBILITY_PUBLIC)
            .setContentIntent(pendingIntent)

        notificationManager.notify(NOTIFICATION_NEW_MESSAGE, builder.build())
    }

    fun createIncomingVideoCallNotification(userDetails: FBUserDetails){
        val intent = Intent(App.getContext(), VideoCallActivity::class.java).apply {
            val b:Bundle = Bundle().apply {
                putString(EXTRA_VIDEO_CALL_TYPE, VIDEO_CALL_INCOMING)
                putParcelable(EXTRA_VIDEO_CALL_USER_DETAILS, userDetails)
            }

            setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            notificationManager.cancel(NOTIFICATION_VIDEO_CALL_REQUEST)
        }

        val intentDecline = Intent(App.getContext(), VideoCallRequestBroadcastReceiver::class.java).apply {
            putExtra(EXTRA_INCOMING_VIDEO_CALL_FROM_UID, userDetails.firebaseUID)
            setAction("com.slyworks.medix.BROADCAST_VIDEO_CALL_REQUEST")
            putExtra(EXTRA_INCOMING_VIDEO_CALL_RESPONSE_TYPE, REQUEST_DECLINED)

            notificationManager.cancel(NOTIFICATION_VIDEO_CALL_REQUEST)
        }

        val pendingIntent: PendingIntent = PendingIntent.getActivity(App.getContext(), 5, intent, PendingIntent.FLAG_CANCEL_CURRENT )

        //val pendingIntentAccept:PendingIntent = PendingIntent.getActivity(App.getContext(), 6, intentAccept, PendingIntent.FLAG_CANCEL_CURRENT)
        val pendingIntentDecline:PendingIntent = PendingIntent.getBroadcast(App.getContext(), 7, intentDecline,  PendingIntent.FLAG_CANCEL_CURRENT)

        val remoteView:RemoteViews = RemoteViews(App.getContext().packageName, R.layout.layout_message_heads_up)
        remoteView.setTextViewText(R.id.tvTitle_notification_heads_up, "Incoming Video Call")
        remoteView.setTextViewText(R.id.tvText_notification_heads_up, "You have an incoming Video Call. Tap to answer")


        val builder:NotificationCompat.Builder = NotificationCompat.Builder(App.getContext(), mChannelID2)

        builder.setSmallIcon(R.drawable.splash_image_2)
            .setChannelId(mChannelID2)
            .setPriority(PRIORITY_HIGH)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            //{ delay, vibrate, sleep, vibrate, sleep } pattern???
            .setVibrate(longArrayOf(1_000, 1_000, 1_000, 1_000, 1_000))
            .setContentTitle("Incoming Video Call")
            .setContentText("${userDetails.fullName} would like a video call with you")
            .setCustomHeadsUpContentView(remoteView)
            .setColor(mColor)
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)
            .setWhen(System.currentTimeMillis())
            .setVisibility(VISIBILITY_PUBLIC)
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.ic_check2,"Accept",pendingIntent)
            .addAction(R.drawable.ic_close2,"Decline",pendingIntentDecline)


        notificationManager.notify(NOTIFICATION_VIDEO_CALL_REQUEST, builder.build())
    }

    fun createIncomingVoiceCallNotification(bitmap: Bitmap, details: FBUserDetails) {
        /*TODO:create notification with custom layout*/
        val intent = Intent(App.getContext(), VoiceCallActivity::class.java).apply {
            val b:Bundle = Bundle().apply {
                putString(EXTRA_VOICE_CALL_TYPE, VOICE_CALL_INCOMING)
                putParcelable(EXTRA_VOICE_CALL_USER_DETAILS, details)
            }

            putExtra(EXTRA_ACTIVITY, b)
            setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

            notificationManager.cancel(NOTIFICATION_VOICE_CALL_REQUEST)
        }

        val intentDecline = Intent(App.getContext(), VoiceCallRequestBroadcastReceiver::class.java).apply {
            putExtra(EXTRA_INCOMING_VOICE_CALL_FROM_UID, details.firebaseUID)
            setAction("com.slyworks.medix.BROADCAST_VOICE_CALL_REQUEST")
            putExtra(EXTRA_INCOMING_VOICE_CALL_RESPONSE_TYPE, REQUEST_DECLINED)

            notificationManager.cancel(NOTIFICATION_VOICE_CALL_REQUEST)
        }

        val pendingIntent:PendingIntent = PendingIntent.getActivity(App.getContext(), 9,
            intent, PendingIntent.FLAG_CANCEL_CURRENT )
        val pendingIntentDecline:PendingIntent = PendingIntent.getBroadcast(App.getContext(), 11,
            intentDecline, PendingIntent.FLAG_CANCEL_CURRENT)

        val remoteView:RemoteViews = RemoteViews(App.getContext().packageName, R.layout.layout_message_heads_up)
        remoteView.setTextViewText(R.id.tvTitle_notification_heads_up, "Incoming Voice Call")
        remoteView.setTextViewText(R.id.tvText_notification_heads_up, "You have an incoming Voice Call. Tap to answer")

        val builder:NotificationCompat.Builder = NotificationCompat.Builder(App.getContext(), mChannelID2)

        builder.setSmallIcon(R.drawable.splash_image_2)
            .setChannelId(mChannelID2)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setVibrate(longArrayOf(1_000, 1_000, 1_000, 1_000, 1_000))
            .setContentTitle("Incoming Voice Call")
            .setContentText("${details.fullName} would like a voice call with you")
            .setCustomHeadsUpContentView(remoteView)
            .setLargeIcon(bitmap)
            .setColor(mColor)
            .setAutoCancel(true)
            .setWhen(System.currentTimeMillis())
            .setVisibility(VISIBILITY_PUBLIC)
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.ic_check2, "Accept", pendingIntent)
            .addAction(R.drawable.ic_close2, "Decline", pendingIntentDecline)

        notificationManager.notify(NOTIFICATION_VOICE_CALL_REQUEST, builder.build())
    }


}