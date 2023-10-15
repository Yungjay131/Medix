package app.slyworks.base_feature

import android.content.Context
import android.graphics.Bitmap
import app.slyworks.data_lib.helpers.call.ICallHelper
import app.slyworks.data_lib.helpers.connection_listener.IConnectionStatusHelper
import app.slyworks.data_lib.helpers.consultations.IConsultationsHelper
import app.slyworks.utils_lib.utils.plusAssign
import com.bumptech.glide.Glide
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers


/**
 * Created by Joshua Sylvanus, 7:30 PM, 20/05/2022.
 */
class ListenerManager(
    private val context: Context,
    private val callHelper: ICallHelper,
    private val consultationsHelper: IConsultationsHelper,
    private val notificationHelper: NotificationHelper,
    private val connectionStatusManager: IConnectionStatusHelper){

    private val disposables:CompositeDisposable = CompositeDisposable()
    private val disposables2:CompositeDisposable = CompositeDisposable()

    companion object{
        var isInitialised:Boolean = false
    }

    fun start(){
        if(!isInitialised) {
            observeMyConnectionStatusChanges()
            isInitialised = true
        }
        observeNewConsultationRequests()
        observeIncomingVideoCalls()
        observeIncomingVoiceCalls()
    }

    fun stop() = disposables.clear()

    private fun observeMyConnectionStatusChanges(){
        /* should be set only once per app creation, preferably from the Application class
         * or from MainActivity (BaseActivity)*/
      /*  disposables2 +=
        connectionStatusManager.setMyConnectionStatusHandler()
            .observeOn(Schedulers.io())
            .subscribeOn(Schedulers.io())
            .subscribe()*/
    }

    private fun observeNewConsultationRequests(){
      /*  disposables +=
        consultationsHelper.listenForConsultationRequests()
            .observeOn(Schedulers.io())
            .subscribeOn(Schedulers.io())
            .subscribe {
                notificationHelper.createConsultationRequestNotification(
                    fromUID = it.details.firebaseUID,
                    fullName = it.details.fullName,
                    toFCMRegistrationToken = it.details.fcm_registration_token,
                    message = "${it.details.fullName} would like a consultation with you")
            }*/
    }

    private fun observeIncomingVideoCalls(){
        /*disposables +=
        callHelper.listenForVideoCallRequests()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe {
                notificationHelper.createIncomingVideoCallNotification(it)
            }*/
    }

    private fun observeIncomingVoiceCalls(){
     /*   disposables +=
        callHelper.listenForVoiceCallRequests()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe {
                val bitmap:Bitmap = Glide.with(context)
                    .asBitmap()
                    .load(it)
                    .submit()
                    .get()
                notificationHelper.createIncomingVoiceCallNotification(it, bitmap)
            }*/
    }
}