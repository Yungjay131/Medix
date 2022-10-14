package com.slyworks.medix.helpers

import android.graphics.Bitmap
import com.bumptech.glide.Glide
import com.slyworks.communication.CallManager
import com.slyworks.communication.CloudMessageManager
import com.slyworks.communication.ConnectionStatusManager
import com.slyworks.medix.App
import com.slyworks.medix.utils.plusAssign
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers


/**
 *Created by Joshua Sylvanus, 7:30 PM, 20/05/2022.
 */
class ListenerManager(
    //region Vars
    private val callManager: CallManager,
    private val cloudMessageManager: CloudMessageManager,
    private val notificationHelper: NotificationHelper){
    private val disposables:CompositeDisposable = CompositeDisposable()
    //endregion

    companion object{
        /*should be set only once per app creation, preferably from the Application class
        * or from MainActivity (BaseActivity)*/
        var isInitialised:Boolean = false

        fun observeMyConnectionStatusChanges(connectionStatusManager:ConnectionStatusManager){
            connectionStatusManager.setMyConnectionStatusHandler()
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .subscribe()

            isInitialised = true
        }
    }

    fun start(){
        observeNewConsultationRequests()
        observeIncomingVideoCalls()
        observeIncomingVoiceCalls()
    }

    fun stop() = disposables.clear()

    private fun observeNewConsultationRequests(){
        disposables +=
        cloudMessageManager.listenForConsultationRequests()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe {
                notificationHelper.createConsultationRequestNotification(
                    fromUID = it.details.firebaseUID,
                    fullName = it.details.fullName,
                    toFCMRegistrationToken = it.details.FCMRegistrationToken,
                    message = "${it.details.fullName} would like a consultation with you")
            }
    }

    private fun observeIncomingVideoCalls(){
        disposables +=
        callManager.listenForVideoCallRequests()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe {
                notificationHelper.createIncomingVideoCallNotification(it)
            }
    }

    private fun observeIncomingVoiceCalls(){
        disposables +=
        callManager.listenForVoiceCallRequests()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe {
                val b:Bitmap = Glide.with(App.getContext())
                    .asBitmap()
                    .load(it.imageUri)
                    .submit()
                    .get()
                notificationHelper.createIncomingVoiceCallNotification(b,it)
            }
    }
}