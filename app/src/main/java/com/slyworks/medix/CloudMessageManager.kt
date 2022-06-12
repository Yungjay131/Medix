package com.slyworks.medix

import android.content.BroadcastReceiver
import android.util.Log
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.slyworks.constants.*
import com.slyworks.models.models.*
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject
import kotlinx.coroutines.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


/**
 *Created by Joshua Sylvanus, 10:16 PM, 1/19/2022.
 */
object CloudMessageManager {
    //region Vars
    private val TAG: String? = CloudMessageManager::class.simpleName

    private var mFirebaseDatabase:FirebaseDatabase = FirebaseDatabase.getInstance()

    private val o:PublishSubject<ConsultationRequest> = PublishSubject.create()
    //endregion

    private val mConsultationRequestChildEventListener:ChildEventListener = object : ChildEventListener{
        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
            //means request was accepted or declined
            CoroutineScope(Dispatchers.IO).launch {
                val request: ConsultationRequest = snapshot.getValue(ConsultationRequest::class.java)!!
                AppController.notifyObservers(EVENT_LISTEN_FOR_CONSULTATION_REQUESTS_ACCEPT, request)
            }
        }
        override fun onChildRemoved(snapshot: DataSnapshot) { }
        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) { }
        override fun onCancelled(error: DatabaseError) { }
        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
            CoroutineScope(Dispatchers.IO).launch {
                val request: ConsultationRequest = snapshot.getValue(ConsultationRequest::class.java)!!
                o.onNext(request)
               //AppController.notifyObservers(EVENT_LISTEN_FOR_CONSULTATION_REQUESTS, request)
            }

        }
    }

    private fun _listenForConsultationRequests(){
       getUserConsultationRequestsRef(UserDetailsUtils.user!!.firebaseUID)
            .addChildEventListener(mConsultationRequestChildEventListener)
    }

    fun listenForConsultationRequests():Observable<ConsultationRequest>{
        _listenForConsultationRequests()
        return o.hide()
    }

    fun getConsultationRequests(){
        getUserConsultationRequestsRef(UserDetailsUtils.user!!.firebaseUID)
            .get()
            .addOnCompleteListener {
                CoroutineScope(Dispatchers.IO).launch {
                    if(it.isSuccessful){
                        if(it.getResult()?.getValue() == null) return@launch
                        val result = it.result
                        result!!.children.forEach{
                            val consultationRequest: ConsultationRequest = it.getValue(ConsultationRequest::class.java)!!
                            AppController.notifyObservers(EVENT_LISTEN_FOR_CONSULTATION_REQUESTS, consultationRequest)
                        }
                    }
                    else{
                        Log.e(TAG, "getConsultationRequests: getting consultation request from DB failed", it.exception )
                    }
                }
            }

    }

    fun detachConsultationRequestListener(){
       getUserConsultationRequestsRef(UserDetailsUtils.user!!.firebaseUID)
            .removeEventListener(mConsultationRequestChildEventListener)
    }

    fun checkRequestStatus(UID:String){
             getUserSentConsultationRequestsRef(params = UserDetailsUtils.user!!.firebaseUID, params2 = UID)
            .get()
            .addOnCompleteListener {
                if(it.isSuccessful){
                    //"REQUEST_PENDING,REQUEST_ACCEPTED,REQUEST_DECLINED,("NOT_SENT")
                    val result: ConsultationRequest? = it.result!!.getValue<ConsultationRequest>(
                        ConsultationRequest::class.java)
                    val status:String = if(result == null) REQUEST_NOT_SENT else if(result.status == null) REQUEST_NOT_SENT else result.status
                    AppController.notifyObservers(EVENT_GET_CONSULTATION_REQUEST, status)
                }else{
                    Log.e(TAG, "checkRequestStatus: getting request status failed")
                    AppController.notifyObservers(EVENT_GET_CONSULTATION_REQUEST, REQUEST_FAILED)
                }
            }

    }

    fun sendConsultationRequestResponse(response: ConsultationResponse,
                                        pendingResult: BroadcastReceiver.PendingResult? = null,
                                        mode: MessageMode = MessageMode.CLOUD_MESSAGE){

       CoroutineScope(Dispatchers.IO).launch {
           val job:Deferred<Unit> = CoroutineScope(Dispatchers.IO).async {
               val fcMessage: FirebaseCloudMessage = mapConsultationResponseToFCMessage(response)

               ApiClient().getApiInterface()
                   .sendCloudMessage(fcMessage)
                   .enqueue(object : Callback<ResponseBody> {
                       override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                           if (response.isSuccessful) {
                               Log.e(TAG, "onResponse: cloud message consultation request sent successfully")
                           } else {
                               Log.e(TAG, "onResponse: cloud message consultation request did not send successfully")
                           }

                           this@async.cancel()
                       }

                       override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                           Log.e(TAG, "onFailure: sending cloud message response failed", t)
                           this@async.cancel()
                       }
                   })
           }

           job.await()

           val childNodeUpdate:HashMap<String, Any> = hashMapOf(
               "/requests/${UserDetailsUtils.user!!.firebaseUID}/to/${response.toUID}/status" to response.status,
               "/requests/${response.toUID}/from/${UserDetailsUtils.user!!.firebaseUID}/status" to response.status)

           mFirebaseDatabase.reference
               .updateChildren(childNodeUpdate)
               .addOnCompleteListener {
                   if(it.isSuccessful){
                       AppController.notifyObservers(EVENT_SEND_REQUEST, true)
                   }else{
                       Log.e(TAG, "sendRequest: sending request failed", it.exception)
                       AppController.notifyObservers(EVENT_SEND_REQUEST, false)
                   }

                   pendingResult?.finish()
               }
       }
    }

    fun sendConsultationRequest(request: ConsultationRequest, mode: MessageMode = MessageMode.DB_MESSAGE){
        when(mode){
            MessageMode.DB_MESSAGE -> sendConsultationRequestViaFB(request)
            MessageMode.CLOUD_MESSAGE -> sendConsultationRequestViaFCM(request)
        }
    }

    private fun sendConsultationRequestViaFB(request: ConsultationRequest) {
        val childNodeUpdate:HashMap<String, Any> = hashMapOf(
            "/requests/${UserDetailsUtils.user!!.firebaseUID}/to/${request.toUID}" to request,
            "/requests/${request.toUID}/from/${UserDetailsUtils.user!!.firebaseUID}" to request)

        mFirebaseDatabase.reference
            .updateChildren(childNodeUpdate)
            .addOnCompleteListener {
                if(it.isSuccessful){
                    AppController.notifyObservers(EVENT_SEND_REQUEST, true)
                }else{
                    Log.e(TAG, "sendRequest: sending request failed", it.exception)
                    AppController.notifyObservers(EVENT_SEND_REQUEST, false)
                }
            }
    }
    private fun sendConsultationRequestViaFCM(request: ConsultationRequest){
        CoroutineScope(Dispatchers.IO).launch {
            val fcMessage: FirebaseCloudMessage = mapConsultationRequestToFCMessage(request)

            ApiClient().getApiInterface()
                .sendCloudMessage(fcMessage)
                .enqueue(object: Callback<ResponseBody> {
                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        if(response.isSuccessful) {
                            AppController.notifyObservers(EVENT_SEND_CLOUD_MESSAGE, true)

                            updateRequestSender(fcMessage.to, REQUEST_PENDING)
                            updateRequestReceiver(UserDetailsUtils.user!!.firebaseUID, REQUEST_PENDING)
                        }
                        else
                            AppController.notifyObservers(EVENT_SEND_CLOUD_MESSAGE, false)
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        Log.e(TAG, "onFailure: sending Cloud message request failed",t)

                        AppController.notifyObservers(EVENT_SEND_CLOUD_MESSAGE, false)
                    }
                })
        }
    }

    private fun mapConsultationResponseToFCMessage(response: ConsultationResponse): FirebaseCloudMessage {
        val type:String = if(response.status == REQUEST_ACCEPTED) FCM_RESPONSE_ACCEPTED else FCM_RESPONSE_DECLINED
        val action:String = if(response.status == REQUEST_ACCEPTED) "accepted" else "declined"
        val message:String = "${response.fullName} $action your request for consultation"
        val data: ConsultationRequestData =
            ConsultationRequestData(message, response.fromUID, type = type)
        return FirebaseCloudMessage(response.toUID, data)
    }

    private fun mapConsultationRequestToFCMessage(request: ConsultationRequest): FirebaseCloudMessage {
        val message: String = "Hi i'm ${UserDetailsUtils.user!!.fullName}. Please i would like a consultation with you"
        val data: ConsultationRequestData =
            ConsultationRequestData(message, request.details.firebaseUID, type = FCM_REQUEST)
        return FirebaseCloudMessage(request.toUID, data)
    }

    fun updateRequestSender(toUID:String, status:String){
        val childNodes:HashMap<String, Any> = hashMapOf(
            "/users/${UserDetailsUtils.user!!.firebaseUID}/requests/to/$toUID" to status,
            "/users/$toUID/requests/from/${UserDetailsUtils.user!!.firebaseUID}" to status )

        mFirebaseDatabase.reference
            .updateChildren(childNodes)
            .addOnCompleteListener {
                if(it.isSuccessful)
                    Log.e(TAG, "updateRequest_sender: updates REQUEST status in DB successful" )
                else
                    Log.e(TAG, "updateRequest_sender: update REQUEST status in DB failed", it.exception )
            }

    }
    fun updateRequestReceiver(fromUID:String, status: String){
        val childNodes:HashMap<String, Any> = hashMapOf(
            "/users/${UserDetailsUtils.user!!.firebaseUID}/requests/from/$fromUID" to status,
            "/users/$fromUID/requests/to/${UserDetailsUtils.user!!.firebaseUID}" to status )

        mFirebaseDatabase.reference
            .updateChildren(childNodes)
            .addOnCompleteListener {
                if(it.isSuccessful)
                    Log.e(TAG, "updateRequest_receive: updates REQUEST status in DB successful" )
                else
                    Log.e(TAG, "updateRequest-receive: update REQUEST status in DB failed", it.exception )
            }
    }


}