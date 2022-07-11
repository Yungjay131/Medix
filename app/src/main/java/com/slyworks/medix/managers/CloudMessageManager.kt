package com.slyworks.medix.managers

import com.google.firebase.database.*
import com.slyworks.constants.*
import com.slyworks.medix.utils.AppController
import com.slyworks.medix.utils.UserDetailsUtils
import com.slyworks.medix.getUserReceivedConsultationRequestsRef
import com.slyworks.medix.getUserSentConsultationRequestsRef
import com.slyworks.medix.network.ApiClient
import com.slyworks.medix.utils.MChildEventListener
import com.slyworks.medix.utils.MValueEventListener
import com.slyworks.models.models.*
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter
import kotlinx.coroutines.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber


/**
 *Created by Joshua Sylvanus, 10:16 PM, 1/19/2022.
 */
object CloudMessageManager {
    //region Vars
    private val TAG: String? = CloudMessageManager::class.simpleName

    private var mFirebaseDatabase:FirebaseDatabase = FirebaseDatabase.getInstance()

    private lateinit var mConsultationRequestsValueEventListener:ValueEventListener
    private lateinit var mConsultationRequestChildEventListener:ChildEventListener

    //endregion

    /*TODO:there is a function that observes the consultation request for a SPECIFIC user,
    *   create one that listens for ALL consultation request status,
    *  the refs have been created in FirebaseUtils*/
    fun listenForConsultationRequests():Observable<ConsultationRequest> =
        Observable.create { emitter:ObservableEmitter<ConsultationRequest> ->
            mConsultationRequestChildEventListener =
                MChildEventListener(
                    onChildAddedFunc = { snapshot ->
                        val request: ConsultationRequest = snapshot.getValue(ConsultationRequest::class.java)!!
                        emitter.onNext(request)
                    })

            getUserReceivedConsultationRequestsRef(UserDetailsUtils.user!!.firebaseUID)
                .addChildEventListener(mConsultationRequestChildEventListener)
        }

    fun detachConsultationRequestListener(){
       getUserReceivedConsultationRequestsRef(UserDetailsUtils.user!!.firebaseUID)
            .removeEventListener(mConsultationRequestChildEventListener)
    }

    fun observeConsultationRequestStatus(UID:String):Observable<String> =
        Observable.create<String>{ emitter ->
            mConsultationRequestsValueEventListener =
                MValueEventListener(
                    onDataChangeFunc = {
                        //"REQUEST_PENDING,REQUEST_ACCEPTED,REQUEST_DECLINED,("NOT_SENT")
                        val result: ConsultationRequest? = it.getValue<ConsultationRequest>(
                            ConsultationRequest::class.java)
                        val status:String =
                            if(result == null)
                                REQUEST_NOT_SENT
                            else if(result.status == null)
                                REQUEST_NOT_SENT
                            else result.status

                        emitter.onNext(status)
                    })

            getUserSentConsultationRequestsRef(
                params = UserDetailsUtils.user!!.firebaseUID,
                params2 = UID
            )
            .addValueEventListener(mConsultationRequestsValueEventListener)
        }

    fun detachCheckRequestStatusListener(UID:String){
        getUserSentConsultationRequestsRef(
            params = UserDetailsUtils.user!!.firebaseUID,
            params2 = UID
        )
            .removeEventListener(mConsultationRequestsValueEventListener)
    }

    fun sendConsultationRequestResponse(response:ConsultationResponse):Observable<Outcome> =
        Observable.combineLatest(
            sendConsultationRequestResponseViaFCM(response),
            sendConsultationRequestResponseToDB(response),
            { o1:Outcome, o2:Outcome ->
                if (o1.isSuccess && o2.isSuccess)
                    Outcome.SUCCESS<Nothing>(additionalInfo = "response delivered")
                else
                    Outcome.FAILURE<Nothing>(reason = "response was not successfully delivered")
            })

    private fun sendConsultationRequestResponseViaFCM(response: ConsultationResponse):Observable<Outcome>
       =  Observable.create<Outcome> { emitter ->
           val fcMessage:FirebaseCloudMessage = mapConsultationResponseToFCMessage(response)

            ApiClient().getApiInterface()
                .sendCloudMessage(fcMessage)
                .enqueue(object : Callback<ResponseBody>{
                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        if (response.isSuccessful) {
                            Timber.e( "onResponse: cloud message consultation request sent successfully")
                            emitter.onNext(Outcome.SUCCESS(null))
                            emitter.onComplete()
                        } else {
                            Timber.e( "onResponse: cloud message consultation request did not send successfully")
                            emitter.onNext(Outcome.FAILURE(null))
                            emitter.onComplete()

                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        Timber.e("onFailure: sending cloud message response failed", t)
                        emitter.onNext(Outcome.ERROR(null, additionalInfo = t.message))
                        emitter.onComplete()
                    }
                })
        }

    private fun sendConsultationRequestResponseToDB(response: ConsultationResponse):Observable<Outcome> =
        Observable.create<Outcome> { emitter ->
            val childNodeUpdate:HashMap<String, Any> = hashMapOf(
                "/requests/${UserDetailsUtils.user!!.firebaseUID}/from/${response.toUID}/status" to response.status,
                "/requests/${response.toUID}/to/${UserDetailsUtils.user!!.firebaseUID}/status" to response.status)

            mFirebaseDatabase.reference
                .updateChildren(childNodeUpdate)
                .addOnCompleteListener {
                    if(it.isSuccessful){
                        Timber.e("sendConsultationRequestResponse: success")
                        emitter.onNext(Outcome.SUCCESS(null))
                        emitter.onComplete()
                    }else{
                        Timber.e("sendConsultationRequestResponse", it.exception)
                        emitter.onNext(Outcome.FAILURE(null))
                        emitter.onComplete()
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
                    Timber.e("sendRequest: sending request failed", it.exception)
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
                            //AppController.notifyObservers(EVENT_SEND_REQUEST, true)
                            AppController.pushToTopic(EVENT_SEND_REQUEST, true)

                            sendConsultationRequestResponseToDB(
                                ConsultationResponse(toUID = fcMessage.to,
                                                     fromUID = UserDetailsUtils.user!!.firebaseUID,
                                                     fullName = request.details.fullName))
                        }
                        else
                            //AppController.notifyObservers(EVENT_SEND_REQUEST, false)
                            AppController.pushToTopic(EVENT_SEND_REQUEST, true)
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        Timber.e("onFailure: sending Cloud message request failed",t)

                        //AppController.notifyObservers(EVENT_SEND_REQUEST, false)
                        AppController.pushToTopic(EVENT_SEND_REQUEST, true)
                    }
                })
        }
    }

    private fun mapConsultationResponseToFCMessage(response: ConsultationResponse): FirebaseCloudMessage {
        val _type:String = if(response.status == REQUEST_ACCEPTED) FCM_RESPONSE_ACCEPTED else FCM_RESPONSE_DECLINED
        val action:String = if(response.status == REQUEST_ACCEPTED) "accepted" else "declined"
        val message:String = "${response.fullName} $action your request for consultation"
        val data: Data =
            ConsultationRequestData(message = message,
                                    fromUID =  response.fromUID,
                                     fullName = response.fullName,
                                      toFCMRegistrationToken = response.toFCMRegistrationToken,
                                     type = _type)
        return FirebaseCloudMessage(response.toFCMRegistrationToken, data)
    }

    private fun mapConsultationRequestToFCMessage(request: ConsultationRequest): FirebaseCloudMessage {
        val message: String = "Hi i'm ${UserDetailsUtils.user!!.fullName}. Please i would like a consultation with you"
        val data: Data =
            ConsultationRequestData(message = message,
                                    fromUID = request.details.firebaseUID,
                                    fullName = request.details.fullName,
                                     toFCMRegistrationToken = request.details.FCMRegistrationToken,
                                    type = FCM_REQUEST)
        return FirebaseCloudMessage(request.details.FCMRegistrationToken, data)
    }

    fun updateRequestSender(toUID:String, status:String){
        val childNodes:HashMap<String, Any> = hashMapOf(
            "/requests/${UserDetailsUtils.user!!.firebaseUID}/to/$toUID/status" to status,
            "/requests/$toUID/from/${UserDetailsUtils.user!!.firebaseUID}/status" to status )

        mFirebaseDatabase.reference
            .updateChildren(childNodes)
            .addOnCompleteListener {
                if(it.isSuccessful)
                    Timber.e("updateRequest_sender: updates REQUEST status in DB successful" )
                else
                    Timber.e("updateRequest_sender: update REQUEST status in DB failed", it.exception )
            }

    }


}