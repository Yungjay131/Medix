package com.slyworks.communication

import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.FirebaseDatabase
import com.slyworks.constants.TYPE_REQUEST
import com.slyworks.constants.TYPE_RESPONSE
import com.slyworks.fcm_api.FCMClientApi
import com.slyworks.fcm_api.FirebaseCloudMessage
import com.slyworks.firebase_commons.FirebaseUtils
import com.slyworks.firebase_commons.MChildEventListener
import com.slyworks.models.models.*
import com.slyworks.models.room_models.FBUserDetails
import com.slyworks.userdetails.UserDetailsUtils
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.PublishSubject
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber
import javax.inject.Inject


class CallManager
constructor(
    private val firebaseDatabase: FirebaseDatabase,
    private val fcmClientApi: FCMClientApi,
    private val userDetailsUtils:UserDetailsUtils,
    private val firebaseUtils: FirebaseUtils
) {
    //region Vars
    private var videoCallObserver:PublishSubject<FBUserDetails>? = PublishSubject.create()
    private var voiceCallObserver:PublishSubject<FBUserDetails>? = PublishSubject.create()

    private var mVideoCallRequestsChildEventListener:ChildEventListener? = null
    private var mVoiceCallRequestsChildEventListener:ChildEventListener? = null
    //endregion

    fun listenForVideoCallRequests(): Observable<FBUserDetails> {
        if(mVideoCallRequestsChildEventListener == null){
            mVideoCallRequestsChildEventListener = MChildEventListener(
                onChildChangedFunc = { snapshot ->
                    val request: VideoCallRequest = snapshot.getValue(VideoCallRequest::class.java)!!

                    if (videoCallObserver == null)
                        videoCallObserver = PublishSubject.create()

                    videoCallObserver?.onNext(request.details)
                })
        }

        firebaseUtils.getVideoCallRequestsRef(userDetailsUtils.user!!.firebaseUID)
            .addChildEventListener(mVideoCallRequestsChildEventListener!!)

        return videoCallObserver!!
    }

    fun detachVideoCallRequestsListener(){
        firebaseUtils.getVideoCallRequestsRef(userDetailsUtils.user!!.firebaseUID)
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
                        "/video_call_requests/$firebaseUID/from/${userDetailsUtils.user!!.firebaseUID}" to request!!,
                        "/video_call_requests/${userDetailsUtils.user!!.firebaseUID}/to/$firebaseUID" to request
                    )
                }
                TYPE_RESPONSE ->{
                    childNodeUpdate = hashMapOf(
                        "/video_call_requests/${userDetailsUtils.user!!.firebaseUID}/from/$firebaseUID/status" to status!!,
                        "/video_call_requests/$firebaseUID/to/${userDetailsUtils.user!!.firebaseUID}/status" to status)

                }
            }

            firebaseDatabase
                .reference
                .updateChildren(childNodeUpdate)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        val r:Outcome = Outcome.SUCCESS<Nothing>()
                        Timber.e( "processVideoCall: video call status updated successfully in DB")
                        emitter.onNext(r)
                        emitter.onComplete()
                    } else {
                        val r:Outcome = Outcome.FAILURE<Nothing>()
                        Timber.e( "processVideoCall: video call status updated with issues in DB")
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

                    if (voiceCallObserver == null)
                        voiceCallObserver = PublishSubject.create()

                    voiceCallObserver?.onNext(request.details)
                })
        }


        firebaseUtils.getVideoCallRequestsRef(userDetailsUtils.user!!.firebaseUID)
            .addChildEventListener(mVoiceCallRequestsChildEventListener!!)

        return voiceCallObserver!!
    }

    fun detachVoiceCallRequestsListener(){
        firebaseUtils.getVideoCallRequestsRef(userDetailsUtils.user!!.firebaseUID)
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
                        "/voice_call_requests/$firebaseUID/from/${userDetailsUtils.user!!.firebaseUID}" to request!!,
                        "/voice_call_requests/${userDetailsUtils.user!!.firebaseUID}/to/$firebaseUID" to request)
                }
                TYPE_RESPONSE ->{
                    childNodeUpdate = hashMapOf(
                        "/voice_call_requests/${userDetailsUtils.user!!.firebaseUID}/from/$firebaseUID/status" to status!!,
                        "/voice_call_requests/$firebaseUID/to/${userDetailsUtils.user!!.firebaseUID}/status" to status)
                }
            }

            firebaseDatabase
                .reference
                .updateChildren(childNodeUpdate)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        val r:Outcome = Outcome.SUCCESS<Nothing>()
                        Timber.e( "processVoiceCall: voice call status updated successfully in DB")
                        emitter.onNext(r)
                        emitter.onComplete()
                    } else {
                        val r:Outcome = Outcome.FAILURE<Nothing>()
                        Timber.e( "processVoiceCall: voice call status updated with issues in DB")
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

            fcmClientApi
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
}