package com.slyworks.communication

import android.annotation.SuppressLint
import com.google.firebase.database.FirebaseDatabase
import com.slyworks.room.daos.CallHistoryDao
import com.slyworks.models.models.Outcome
import com.slyworks.models.room_models.CallHistory
import com.slyworks.userdetails.UserDetailsUtils
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.SingleSource
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.Job
import javax.inject.Inject


/**
 *Created by Joshua Sylvanus, 4:27 PM, 06/07/2022.
 */
class CallHistoryManager(
    private val callHistoryDao: CallHistoryDao,
    private val firebaseDatabase: FirebaseDatabase,
    private val userDetailsUtils: UserDetailsUtils
) {
    //region Vars
    private var mCurrentVideoCall: CallHistory? = null
    private var mCurrentVideoCallStartTime:Long? = null
    private var mCurrentVideoCallEndTime:Long? = null

    private var mCurrentVoiceCall: CallHistory? = null
    private var mCurrentVoiceCallStartTime:Long? = null
    private var mCurrentVoiceCallEndTime:Long? = null

    private var mObserveCallsHistory: Job? = null

    //endregion

    fun observeCallsHistory(): Observable<List<CallHistory>> =
        Observable.create { emitter ->
                   callHistoryDao
                       .observeCallHistory()
                       .startWith (
                           callHistoryDao.getCallHistoryCount()
                               .map {
                                   if (it == 0)
                                       emptyList<CallHistory>()
                               } as SingleSource<List<CallHistory>>
                        )
                        .distinctUntilChanged()
                        .subscribe {
                            emitter.onNext(it)
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
            callHistoryDao
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
                .child("/call_history/${userDetailsUtils.user!!.firebaseUID}")
                .push()
                .key!!

            firebaseDatabase
                .reference
                .child("/call_history/${userDetailsUtils.user!!.firebaseUID}")
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