package com.slyworks.medix.managers

import android.util.Log
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.slyworks.constants.TYPE_REQUEST
import com.slyworks.constants.TYPE_RESPONSE
import com.slyworks.data.AppDatabase
import com.slyworks.medix.App
import com.slyworks.medix.utils.MChildEventListener
import com.slyworks.medix.utils.UserDetailsUtils
import com.slyworks.medix.getVideoCallRequestsRef
import com.slyworks.medix.network.ApiClient
import com.slyworks.models.models.*
import com.slyworks.models.room_models.CallHistory
import com.slyworks.models.room_models.FBUserDetails
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.PublishSubject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


object CallManager {
    private val TAG: String? = CallManager::class.simpleName

    private var mCurrentVideoCall:CallHistory? = null
    private var mCurrentVideoCallStartTime:Long? = null
    private var mCurrentVideoCallEndTime:Long? = null

    private var mCurrentVoiceCall:CallHistory? = null
    private var mCurrentVoiceCallStartTime:Long? = null
    private var mCurrentVoiceCallEndTime:Long? = null

    private var videoCallObserver:PublishSubject<FBUserDetails>? = PublishSubject.create()
    private var voiceCallObserver:PublishSubject<FBUserDetails>? = PublishSubject.create()

    private var mVideoCallRequestsChildEventListener:ChildEventListener? = null
    private var mVoiceCallRequestsChildEventListener:ChildEventListener? = null

    private var mObserveCallsHistory: Job? = null
    //endregion

    fun listenForVideoCallRequests(): Observable<FBUserDetails> {
        if(mVideoCallRequestsChildEventListener == null){
            mVideoCallRequestsChildEventListener =  MChildEventListener(
                onChildChangedFunc = { snapshot ->
                    val request: VideoCallRequest = snapshot.getValue(VideoCallRequest::class.java)!!

                    if(videoCallObserver == null)
                        videoCallObserver = PublishSubject.create()

                    videoCallObserver?.onNext(request.details)
                })
        }

        getVideoCallRequestsRef()
            .addChildEventListener(mVideoCallRequestsChildEventListener!!)

        return videoCallObserver!!
    }

    fun detachVideoCallRequestsListener(){
        getVideoCallRequestsRef()
            .removeEventListener(mVideoCallRequestsChildEventListener!!)

        mVideoCallRequestsChildEventListener = null
        videoCallObserver = null
    }

    fun processVideoCall(type:String,
                         firebaseUID: String,
                         status:String? = null,
                         request: VideoCallRequest? = null){
        processVideoCallAsync(type = type,
            firebaseUID = firebaseUID,
            status = status,
            request = request)
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe { _ -> }
    }


    fun processVideoCallAsync(type:String,
                              firebaseUID: String,
                              status:String? = null,
                              request: VideoCallRequest? = null):Observable<Outcome> =
        Observable.create { emitter ->
            var childNodeUpdate:HashMap<String, Any> = hashMapOf()
            when(type){
                TYPE_REQUEST -> {
                    childNodeUpdate = hashMapOf(
                        "/video_call_requests/$firebaseUID/from/${UserDetailsUtils.user!!.firebaseUID}" to request!!,
                        "/video_call_requests/${UserDetailsUtils.user!!.firebaseUID}/to/$firebaseUID" to request
                    )
                }
                TYPE_RESPONSE ->{
                    childNodeUpdate = hashMapOf(
                        "/video_call_requests/${UserDetailsUtils.user!!.firebaseUID}/from/$firebaseUID/status" to status!!,
                        "/video_call_requests/$firebaseUID/to/${UserDetailsUtils.user!!.firebaseUID}/status" to status)

                }
            }

            FirebaseDatabase.getInstance()
                .reference
                .updateChildren(childNodeUpdate)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        val r:Outcome = Outcome.SUCCESS<Nothing>()
                        Log.e(TAG, "processVideoCall: video call status updated successfully in DB")
                        emitter.onNext(r)
                        emitter.onComplete()
                    } else {
                        val r:Outcome = Outcome.FAILURE<Nothing>()
                        Log.e(TAG, "processVideoCall: video call status updated with issues in DB")
                        emitter.onNext(r)
                        emitter.onComplete()
                    }
                }
        }


    fun listenForVoiceCallRequests():Observable<FBUserDetails>{
        if(mVoiceCallRequestsChildEventListener == null){
            mVoiceCallRequestsChildEventListener = MChildEventListener(
                onChildChangedFunc = { snapshot ->
                    val request: VoiceCallRequest = snapshot.getValue(VoiceCallRequest::class.java)!!

                    if(voiceCallObserver == null)
                        voiceCallObserver = PublishSubject.create()

                    voiceCallObserver?.onNext(request.details)
                })
        }


        getVideoCallRequestsRef()
            .addChildEventListener(mVoiceCallRequestsChildEventListener!!)

        return voiceCallObserver!!
    }

    fun detachVoiceCallRequestsListener(){
        getVideoCallRequestsRef()
            .removeEventListener(mVoiceCallRequestsChildEventListener!!)

        mVoiceCallRequestsChildEventListener = null
        voiceCallObserver = null
    }


    /**
     * update DB with response or request and status,
     * type could be TYPE_REQUEST or TYPE_RESPONSE
     * and status could be  REQUEST_ACCEPTED, REQUEST_DECLINED or REQUEST_PENDING*/
    fun processVoiceCall(type:String,
                         firebaseUID:String,
                         status:String? = null,
                         request: VoiceCallRequest? = null){
        processVoiceCallAsync(type = type,
            firebaseUID = firebaseUID,
            status = status,
            request = request)
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe { _ -> }
    }

    fun processVoiceCallAsync(type:String,
                              firebaseUID:String,
                              status:String? = null,
                              request: VoiceCallRequest? = null):Observable<Outcome> =
        Observable.create { emitter ->
            var childNodeUpdate:HashMap<String, Any> = hashMapOf()
            when(type){
                TYPE_REQUEST ->{
                    childNodeUpdate = hashMapOf(
                        "/voice_call_requests/$firebaseUID/from/${UserDetailsUtils.user!!.firebaseUID}" to request!!,
                        "/voice_call_requests/${UserDetailsUtils.user!!.firebaseUID}/to/$firebaseUID" to request)
                }
                TYPE_RESPONSE ->{
                    childNodeUpdate = hashMapOf(
                        "/voice_call_requests/${UserDetailsUtils.user!!.firebaseUID}/from/$firebaseUID/status" to status!!,
                        "/voice_call_requests/$firebaseUID/to/${UserDetailsUtils.user!!.firebaseUID}/status" to status)
                }
            }

            FirebaseDatabase.getInstance()
                .reference
                .updateChildren(childNodeUpdate)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        val r:Outcome = Outcome.SUCCESS<Nothing>()
                        Log.e(TAG, "processVoiceCall: voice call status updated successfully in DB")
                        emitter.onNext(r)
                        emitter.onComplete()
                    } else {
                        val r:Outcome = Outcome.FAILURE<Nothing>()
                        Log.e(TAG, "processVoiceCall: voice call status updated with issues in DB")
                        emitter.onNext(r)
                        emitter.onComplete()
                    }
                }
        }


    fun sendVoiceCallRequestViaFCM(request: FBUserDetails):Observable<Outcome> =
        Observable.create { emitter ->
            val data: VoiceCallData = VoiceCallData.from(request)
            val fcMessage: FirebaseCloudMessage =
                FirebaseCloudMessage(request.firebaseUID, data)

            ApiClient().getApiInterface()
                .sendCloudMessage(fcMessage)
                .enqueue(
                    object: Callback<ResponseBody> {
                        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                            val r:Outcome = Outcome.FAILURE<Nothing>(reason = t.message)
                            emitter.onNext(r)
                            emitter.onComplete()
                        }
                        override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                            val r:Outcome = Outcome.SUCCESS<Nothing>()
                            emitter.onNext(r)
                            emitter.onComplete()
                        }
                    })
        }




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

    private fun saveCallHistory(callHistory: CallHistory){
        Observable

    }

    private fun saveCallHistoryToDBObservable(callHistory: CallHistory):Observable<Outcome> =
        Observable.create { emitter ->
            AppDatabase.getInstance(App.getContext())
                .getCallHistoryDao()
                .addCallHistory(callHistory)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe()
        }
    private fun saveCallHistoryToFBObservable(callHistory: CallHistory):Observable<Outcome> =
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