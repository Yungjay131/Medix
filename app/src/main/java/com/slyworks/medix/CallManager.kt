package com.slyworks.medix

import android.util.Log
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.FirebaseDatabase
import com.slyworks.constants.TYPE_REQUEST
import com.slyworks.constants.TYPE_RESPONSE
import com.slyworks.models.models.*
import com.slyworks.models.room_models.FBUserDetails
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


object CallManager {
    private val TAG: String? = CallManager::class.simpleName

    private var videoCallObserver:PublishSubject<FBUserDetails>? = PublishSubject.create()
    private var voiceCallObserver:PublishSubject<FBUserDetails>? = PublishSubject.create()

    private var mVideoCallRequestsChildEventListener:ChildEventListener? = null
    private var mVoiceCallRequestsChildEventListener:ChildEventListener? = null


    private fun initVideoCallListener(){
        mVideoCallRequestsChildEventListener =  MChildEventListener(
            onChildChangedFunc = { snapshot ->
                //since this method would only be called when the app is in foreground
                val request: VideoCallRequest = snapshot.getValue(VideoCallRequest::class.java)!!

                /*no longer necessary since i've done the filtering in the query*/
                //if(request.status != REQUEST_PENDING) return

                val userDetails: FBUserDetails = snapshot.child("details").getValue(FBUserDetails::class.java)!!
                //AppController.notifyObservers(EVENT_INCOMING_VIDEO_CALL, userDetails)
                if(videoCallObserver == null)
                    videoCallObserver = PublishSubject.create()

                videoCallObserver?.onNext(userDetails)
            })
    }

    private fun initVoiceCallListener(){
        mVoiceCallRequestsChildEventListener = MChildEventListener(
            onChildChangedFunc = { snapshot ->
                //since this method would only be called when the app is in foreground
                val request: VoiceCallRequest = snapshot.getValue(VoiceCallRequest::class.java)!!

                /*no longer necessary since ive done the filtering in the query*/
                //if(request.status != REQUEST_PENDING) return

                val userDetails: FBUserDetails = snapshot.child("details").getValue(FBUserDetails::class.java)!!
                //AppController.notifyObservers(EVENT_INCOMING_VOICE_CALL, userDetails)

                if(voiceCallObserver == null)
                    voiceCallObserver = PublishSubject.create()

                voiceCallObserver?.onNext(userDetails)
            })
    }


    fun processVideoCallAsync(type:String,
                              firebaseUID: String,
                              status:String? = null,
                              request: VideoCallRequest? = null):Observable<Outcome>{
        val o:PublishSubject<Outcome> = PublishSubject.create()
        CoroutineScope(Dispatchers.IO).launch {
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
                        o.onNext(r)
                        Log.e(TAG, "processVideoCall: video call status updated successfully in DB")
                    } else {
                        val r:Outcome = Outcome.FAILURE<Nothing>()
                        o.onNext(r)
                        Log.e(TAG, "processVideoCall: video call status updated with issues in DB")
                    }
                }

        }

        return o.hide()
    }

    fun processVideoCall(type:String,
                         firebaseUID: String,
                         status:String? = null,
                         request: VideoCallRequest? = null){
        CoroutineScope(Dispatchers.IO).launch {
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
                        Log.e(TAG, "processVideoCall: video call status updated successfully in DB")
                    } else {
                        Log.e(TAG, "processVideoCall: video call status updated with issues in DB")
                    }
                }

        }

    }

    fun listenForVideoCallRequests(): Observable<FBUserDetails> {
        if(mVideoCallRequestsChildEventListener == null)
            initVideoCallListener()

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

    fun processVoiceCallAsync(type:String,
                              firebaseUID:String,
                              status:String? = null,
                              request: VoiceCallRequest? = null):Observable<Outcome>{
        val o:PublishSubject<Outcome> = PublishSubject.create()

        CoroutineScope(Dispatchers.IO).launch {
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
                        o.onNext(r)
                        Log.e(TAG, "processVoiceCall: voice call status updated successfully in DB")
                    } else {
                        val r:Outcome = Outcome.FAILURE<Nothing>()
                        o.onNext(r)
                        Log.e(TAG, "processVoiceCall: voice call status updated with issues in DB")
                    }
                }
        }

        return o.hide()
    }

    /**
     * update DB with response or request and status,
     * type could be TYPE_REQUEST or TYPE_RESPONSE
     * and status could be  REQUEST_ACCEPTED, REQUEST_DECLINED or REQUEST_PENDING*/
    fun processVoiceCall(type:String,
                         firebaseUID:String,
                         status:String? = null,
                         request: VoiceCallRequest? = null){
        CoroutineScope(Dispatchers.IO).launch {
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
                        Log.e(TAG, "processVoiceCall: voice call status updated successfully in DB")
                    } else {
                        Log.e(TAG, "processVoiceCall: voice call status updated with issues in DB")
                    }
                }
        }

    }

    fun sendVoiceCallRequestViaFCM(request: FBUserDetails):Observable<Outcome>{
        val o:PublishSubject<Outcome> = PublishSubject.create()

        CoroutineScope(Dispatchers.IO).launch {
            val data: VoiceCallData = VoiceCallData.from(request)
            val fcMessage: FirebaseCloudMessage =
                FirebaseCloudMessage(request.firebaseUID, data)
            ApiClient().getApiInterface()
                .sendCloudMessage(fcMessage)
                .enqueue(object: Callback<ResponseBody> {
                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                         val r:Outcome = Outcome.FAILURE<Nothing>(reason = t.message)
                         o.onNext(r)
                    }
                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                         val r:Outcome = Outcome.SUCCESS<Nothing>()
                         o.onNext(r)
                    }
                })
        }

        return o
    }

    fun listenForVoiceCallRequests():Observable<FBUserDetails>{
        if(mVoiceCallRequestsChildEventListener == null)
            initVoiceCallListener()

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
}