package com.slyworks.medix.managers

import android.graphics.Bitmap
import com.bumptech.glide.Glide
import com.slyworks.constants.REQUEST_ACCEPTED
import com.slyworks.constants.REQUEST_DECLINED
import com.slyworks.constants.REQUEST_PENDING
import com.slyworks.medix.App
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers


/**
 *Created by Joshua Sylvanus, 7:30 PM, 20/05/2022.
 */
class ListenerManager(){
    //region Vars
    private var mSubscriptions:CompositeDisposable = CompositeDisposable()
    //endregion

    companion object{
        /*should be set only once per app creation, preferably from the Application class
        * or from MainActivity (BaseActivity)*/
        var isInitialised:Boolean = false
        fun observeMyConnectionStatusChanges(){
            ConnectionStatusManager.setMyConnectionStatusHandler()
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

    fun stop(){
        mSubscriptions.clear()
    }

    private fun observeNewConsultationRequests(){
        val d = CloudMessageManager.listenForConsultationRequests()
            .observeOn(Schedulers.io())
            .subscribeOn(Schedulers.io())
            .subscribe {
                NotificationHelper.createConsultationRequestNotification(
                    fromUID = it.details.firebaseUID,
                    fullName = it.details.fullName,
                    toFCMRegistrationToken = it.details.FCMRegistrationToken,
                    message = "${it.details.fullName} would like a consultation with you")
            }

        mSubscriptions.add(d)
    }

    private fun observeIncomingVideoCalls(){
        val d = CallManager.listenForVideoCallRequests()
            .observeOn(Schedulers.io())
            .subscribeOn(Schedulers.io())
            .subscribe {
                NotificationHelper.createIncomingVideoCallNotification(it)
            }

        mSubscriptions.add(d)
    }

    private fun observeIncomingVoiceCalls(){
        val d = CallManager.listenForVoiceCallRequests()
            .observeOn(Schedulers.io())
            .subscribeOn(Schedulers.io())
            .subscribe {
                val b:Bitmap = Glide.with(App.getContext())
                    .asBitmap()
                    .load(it.imageUri)
                    .submit()
                    .get()
                NotificationHelper.createIncomingVoiceCallNotification(b,it)
            }

        mSubscriptions.add(d)
    }
}