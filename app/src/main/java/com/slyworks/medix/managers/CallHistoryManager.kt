package com.slyworks.medix.managers

import com.google.firebase.database.FirebaseDatabase
import com.slyworks.data.AppDatabase
import com.slyworks.medix.App
import com.slyworks.medix.utils.UserDetailsUtils
import com.slyworks.models.models.Outcome
import com.slyworks.models.room_models.CallHistory
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch


/**
 *Created by Joshua Sylvanus, 4:27 PM, 06/07/2022.
 */
object CallHistoryManager {
    //region Vars
    private var mCurrentVideoCall:CallHistory? = null
    private var mCurrentVideoCallStartTime:Long? = null
    private var mCurrentVideoCallEndTime:Long? = null

    private var mCurrentVoiceCall:CallHistory? = null
    private var mCurrentVoiceCallStartTime:Long? = null
    private var mCurrentVoiceCallEndTime:Long? = null

    private var mObserveCallsHistory: Job? = null
    //endregion

    fun observeCallsHistory(): Observable<List<CallHistory>> =
        Observable.create { emitter ->
            mObserveCallsHistory =
                CoroutineScope(Dispatchers.IO).launch {
                    AppDatabase.getInstance(App.getContext())
                        .getCallHistoryDao()
                        .observeCallHistory()
                        .distinctUntilChanged()
                        .collectLatest {
                            emitter.onNext(it)
                        }
                }
        }


    fun detachObserveCallsHistoryObserver(){
        mObserveCallsHistory!!.cancel()
        mObserveCallsHistory = null
    }

    private fun saveCallHistory(callHistory: CallHistory) {
        Observable.merge(
            saveCallHistoryToDBObservable(callHistory),
            saveCallHistoryToFBObservable(callHistory)
        )
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe()
    }


    private fun saveCallHistoryToDBObservable(callHistory: CallHistory): Observable<Outcome> =
        Observable.create { emitter ->
            AppDatabase.getInstance(App.getContext())
                .getCallHistoryDao()
                .addCallHistory(callHistory)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .doOnComplete {
                    emitter.onNext(Outcome.SUCCESS<Nothing>())
                    emitter.onComplete()
                }
                .doOnError {
                    emitter.onNext(Outcome.FAILURE(value = it.message))
                    emitter.onComplete()
                }
                .subscribe()
        }

    private fun saveCallHistoryToFBObservable(callHistory: CallHistory): Observable<Outcome> =
        Observable.create { emitter ->
            val key:String = FirebaseDatabase.getInstance()
                .reference
                .child("/call_history/${UserDetailsUtils.user!!.firebaseUID}")
                .push()
                .key!!

            FirebaseDatabase.getInstance()
                .reference
                .child("/call_history/${UserDetailsUtils.user!!.firebaseUID}")
                .child(key)
                .setValue(callHistory)
                .addOnCompleteListener {
                    if(it.isSuccessful){
                        emitter.onNext(Outcome.SUCCESS<Nothing>())
                        emitter.onComplete()
                    }else
                        emitter.onNext(Outcome.FAILURE(value = it.exception?.message))
                    emitter.onComplete()
                }
        }

    fun onVideoCallStarted(callHistory: CallHistory){
        mCurrentVideoCallStartTime = System.currentTimeMillis()
        mCurrentVideoCall = callHistory
    }

    fun onVideoCallStopped(){
        mCurrentVideoCallEndTime = System.currentTimeMillis()

        val duration:Long = mCurrentVideoCallEndTime!! - mCurrentVideoCallStartTime!!
        mCurrentVideoCall!!.duration = duration.toString()
        saveCallHistory(mCurrentVideoCall!!)
    }

    fun onVoiceCallStarted(callHistory: CallHistory){
        mCurrentVoiceCallStartTime = System.currentTimeMillis()
        mCurrentVoiceCall = callHistory
    }

    fun onVoiceCallStopped(){
        mCurrentVoiceCallEndTime = System.currentTimeMillis()

        val duration:Long = mCurrentVoiceCallEndTime!! - mCurrentVoiceCallStartTime!!
        mCurrentVoiceCall!!.duration = duration.toString()

        saveCallHistory(mCurrentVoiceCall!!)
    }
}