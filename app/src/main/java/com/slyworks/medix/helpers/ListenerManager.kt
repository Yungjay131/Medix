package com.slyworks.medix.helpers

import android.graphics.Bitmap
import com.bumptech.glide.Glide
import com.slyworks.communication.CallManager
import com.slyworks.communication.CloudMessageManager
import com.slyworks.communication.ConnectionStatusManager
import com.slyworks.medix.App
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject


/**
 *Created by Joshua Sylvanus, 7:30 PM, 20/05/2022.
 */
class ListenerManager(
    private val callManager: CallManager,
    private val cloudMessageManager: CloudMessageManager,
    private val notificationHelper: NotificationHelper){
    //region Vars
    private var mSubscriptions:CompositeDisposable = CompositeDisposable()
    //endregion

    companion object{
        /*should be set only once per app creation, preferably from the Application class
        * or from MainActivity (BaseActivity)*/
        var isInitialised:Boolean = false

        lateinit var connectionStatusManager: ConnectionStatusManager

        fun observeMyConnectionStatusChanges(){
            connectionStatusManager.setMyConnectionStatusHandler()
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .subscribe { _ -> }

            isInitialised = true
        }
    }

    fun start(){
        observeNewConsultationRequests()
        observeIncomingVideoCalls()
        observeIncomingVoiceCalls()
    }

    fun stop() = mSubscriptions.clear()

    private fun observeNewConsultationRequests(){
        val d = cloudMessageManager.listenForConsultationRequests()
            .observeOn(Schedulers.io())
            .subscribeOn(Schedulers.io())
            .subscribe {
                notificationHelper.createConsultationRequestNotification(
                    fromUID = it.details.firebaseUID,
                    fullName = it.details.fullName,
                    toFCMRegistrationToken = it.details.FCMRegistrationToken,
                    message = "${it.details.fullName} would like a consultation with you")
            }

        mSubscriptions.add(d)
    }

    private fun observeIncomingVideoCalls(){
        val d = callManager.listenForVideoCallRequests()
            .observeOn(Schedulers.io())
            .subscribeOn(Schedulers.io())
            .subscribe {
                notificationHelper.createIncomingVideoCallNotification(it)
            }

        mSubscriptions.add(d)
    }

    private fun observeIncomingVoiceCalls(){
        val d = callManager.listenForVoiceCallRequests()
            .observeOn(Schedulers.io())
            .subscribeOn(Schedulers.io())
            .subscribe {
                val b:Bitmap = Glide.with(App.getContext())
                    .asBitmap()
                    .load(it.imageUri)
                    .submit()
                    .get()
                notificationHelper.createIncomingVoiceCallNotification(b,it)
            }

        mSubscriptions.add(d)
    }
}