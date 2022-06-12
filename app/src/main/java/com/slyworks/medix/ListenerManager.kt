package com.slyworks.medix

import android.graphics.Bitmap
import com.bumptech.glide.Glide
import com.slyworks.constants.REQUEST_PENDING
import com.slyworks.medix.utils.NotificationHelper
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers


/**
 *Created by Joshua Sylvanus, 7:30 PM, 20/05/2022.
 */
class ListenerManager
private constructor(){
    //region Vars
    private var mSubscriptions:CompositeDisposable = CompositeDisposable()
    //endregion

    companion object{
        private var instance: ListenerManager? = null

        fun getInstance(): ListenerManager {
           if(instance == null)
               instance = ListenerManager()

            return instance!!
        }
    }

    init {
        observeNewConsultationRequests()
        observeIncomingVideoCalls()
        observeIncomingVoiceCalls()
    }

    fun nullify(){
        mSubscriptions.clear()
        instance = null
    }

    private fun observeNewConsultationRequests(){
        val d = CloudMessageManager.listenForConsultationRequests()
            .observeOn(Schedulers.io())
            .subscribeOn(Schedulers.io())
            .subscribe {
                NotificationHelper.createConsultationResponseNotification(
                    it.details.firebaseUID,
                    it.toUID,
                    message = "${it.details.fullName} would like a consultation with you",
                    REQUEST_PENDING,
                    it.details.fullName)
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