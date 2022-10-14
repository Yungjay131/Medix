package com.slyworks.communication

import com.google.firebase.database.*
import com.slyworks.constants.*
import com.slyworks.controller.AppController
import com.slyworks.fcm_api.FCMClientApi
import com.slyworks.fcm_api.FirebaseCloudMessage
import com.slyworks.firebase_commons.FirebaseUtils
import com.slyworks.firebase_commons.MChildEventListener
import com.slyworks.firebase_commons.MValueEventListener
import com.slyworks.models.models.*
import com.slyworks.room.daos.ConsultationRequestDao
import com.slyworks.userdetails.UserDetailsUtils
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter
import kotlinx.coroutines.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject


/**
 *Created by Joshua Sylvanus, 10:16 PM, 1/19/2022.
 */
class CloudMessageManager(
    //region Vars
    private val firebaseDatabase: FirebaseDatabase,
    private val fcmClientApi: FCMClientApi,
    private val userDetailsUtils: UserDetailsUtils,
    private val firebaseUtils: FirebaseUtils,
    private val consultationRequestsDao:ConsultationRequestDao) {

    private lateinit var mConsultationRequestsValueEventListener:ValueEventListener
    private lateinit var mConsultationRequestChildEventListener:ChildEventListener
    //endregion

    fun getAllConsultationRequests():Observable<Outcome>
    = Observable.just(consultationRequestsDao.getConsultationRequests())
                .concatMap {
                    if(it.isNotEmpty())
                        return@concatMap consultationRequestsDao.observeConsultationRequests()
                                                                .toObservable()
                                                                .concatMap { it2:List<ConsultationRequest> -> Observable.just(Outcome.SUCCESS(value = it2)) }

                    else
                        return@concatMap  consultationRequestsDao.observeConsultationRequests()
                                                               .toObservable()
                                                               .concatMap { it3:List<ConsultationRequest> -> Observable.just(Outcome.SUCCESS(value = it3)) }
                                                               .startWithItem(Outcome.FAILURE(value = Unit, reason = "you currently have no consultation requests"))
                }


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

                        /* save to DB */
                        consultationRequestsDao.addConsultationRequest(request)
                            .subscribe()
                    })

          firebaseUtils.getUserReceivedConsultationRequestsRef(userDetailsUtils.user!!.firebaseUID)
                .addChildEventListener(mConsultationRequestChildEventListener)
        }

    fun detachConsultationRequestListener(){
         firebaseUtils.getUserReceivedConsultationRequestsRef(userDetailsUtils.user!!.firebaseUID)
            .removeEventListener(mConsultationRequestChildEventListener)
    }

    fun observeConsultationRequestStatus(UID:String):Observable<String>
    = Observable.create<String>{ emitter ->
            mConsultationRequestsValueEventListener =
              MValueEventListener(
                    onDataChangeFunc = {
                        //"REQUEST_PENDING,REQUEST_ACCEPTED,REQUEST_DECLINED,("NOT_SENT")
                        val result: ConsultationRequest? = it.getValue<ConsultationRequest>(
                            ConsultationRequest::class.java)

                        val status: String =
                            if (result == null)
                                REQUEST_NOT_SENT
                            else if (result.status == null)
                                REQUEST_NOT_SENT
                            else result.status

                        emitter.onNext(status)
                    })

          firebaseUtils.getUserSentConsultationRequestsRef(
                params = userDetailsUtils.user!!.firebaseUID,
                params2 = UID)
            .addValueEventListener(mConsultationRequestsValueEventListener)
        }

    fun detachCheckRequestStatusListener(UID:String){
      firebaseUtils.getUserSentConsultationRequestsRef(
            params = userDetailsUtils.user!!.firebaseUID,
            params2 = UID)
            .removeEventListener(mConsultationRequestsValueEventListener)
    }

    /* to do make this an atomic process */
    fun sendConsultationRequestResponse(response:ConsultationResponse):Observable<Outcome>
    = Observable.zip(
            sendConsultationRequestResponseViaFCM(response),
            sendConsultationRequestResponseToDB(response)
           )
        { o1: Outcome, o2: Outcome ->
            if (o1.isSuccess && o2.isSuccess)
                Outcome.SUCCESS<Nothing>(additionalInfo = "response delivered")
            else
                Outcome.FAILURE<Nothing>(reason = "response was not successfully delivered")
        }

    private fun sendConsultationRequestResponseViaFCM(response: ConsultationResponse):Observable<Outcome>
       =  Observable.create<Outcome> { emitter ->
           val fcMessage: FirebaseCloudMessage = mapConsultationResponseToFCMessage(response)

            fcmClientApi
                .sendCloudMessage(fcMessage)
                .enqueue(object : Callback<ResponseBody>{
                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        if (response.isSuccessful) {
                            Timber.e( "onResponse: cloud message consultation request sent successfully")
                            emitter.onNext(Outcome.SUCCESS(null))
                        } else {
                            Timber.e( "onResponse: cloud message consultation request did not send successfully")
                            emitter.onNext(Outcome.FAILURE(null))
                        }

                        emitter.onComplete()
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        Timber.e("onFailure: sending cloud message response failed", t)
                        emitter.onNext(Outcome.ERROR(null, additionalInfo = t.message))
                        emitter.onComplete()
                    }
                })
        }
        .onErrorReturn{ it:Throwable -> Outcome.FAILURE<Unit>(reason = it.message) }

    private fun sendConsultationRequestResponseToDB(response: ConsultationResponse):Observable<Outcome>
    = Observable.create<Outcome> { emitter ->
            val childNodeUpdate:HashMap<String, Any> = hashMapOf(
                "/requests/${userDetailsUtils.user!!.firebaseUID}/from/${response.toUID}/status" to response.status,
                "/requests/${response.toUID}/to/${userDetailsUtils.user!!.firebaseUID}/status" to response.status)

            val childNodeUpdateNull:HashMap<String, Any?> = hashMapOf(
                "/requests/${userDetailsUtils.user!!.firebaseUID}/from/${response.toUID}/status" to null,
                "/requests/${response.toUID}/to/${userDetailsUtils.user!!.firebaseUID}/status" to null)

        with(firebaseDatabase.reference){
            updateChildren(childNodeUpdate)
                .addOnSuccessListener {
                    Timber.e("sendConsultationRequestResponse: success")
                    emitter.onNext(Outcome.SUCCESS(null))
                    emitter.onComplete()
                }
                .addOnFailureListener { _ ->
                     updateChildren(childNodeUpdateNull)
                         .addOnCompleteListener {
                             emitter.onNext(Outcome.FAILURE(null))
                             emitter.onComplete()
                         }
                }
          }
      }
      .onErrorReturn { it:Throwable -> Outcome.FAILURE<Unit>(reason = it.message)  }


    fun sendConsultationRequest(request: ConsultationRequest, mode: MessageMode = MessageMode.DB_MESSAGE){
        when(mode){
            MessageMode.DB_MESSAGE -> sendConsultationRequestViaFB(request)
            MessageMode.CLOUD_MESSAGE -> sendConsultationRequestViaFCM(request)
        }
    }

    private fun sendConsultationRequestViaFB(request: ConsultationRequest) {
        val childNodeUpdate:HashMap<String, Any> = hashMapOf(
            "/requests/${userDetailsUtils.user!!.firebaseUID}/to/${request.toUID}" to request,
            "/requests/${request.toUID}/from/${userDetailsUtils.user!!.firebaseUID}" to request)

        firebaseDatabase
            .reference
            .updateChildren(childNodeUpdate)
            .addOnCompleteListener {
                if(it.isSuccessful){
                    consultationRequestsDao.addConsultationRequest(request)
                        .subscribe()

                    AppController.notifyObservers(EVENT_SEND_REQUEST, true)
                }else{
                    consultationRequestsDao.addConsultationRequest(request.apply { this.status = REQUEST_NOT_SENT })
                        .subscribe()

                    Timber.e("sendRequest: sending request failed", it.exception)
                    AppController.notifyObservers(EVENT_SEND_REQUEST, false)
                }
            }
    }

    private fun sendConsultationRequestViaFCM(request: ConsultationRequest){
        CoroutineScope(Dispatchers.IO).launch {
            val fcMessage: FirebaseCloudMessage = mapConsultationRequestToFCMessage(request)

            fcmClientApi
                .sendCloudMessage(fcMessage)
                .enqueue(object: Callback<ResponseBody> {
                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        if(response.isSuccessful) {
                            sendConsultationRequestResponseToDB(
                                ConsultationResponse(toUID = fcMessage.to,
                                                     fromUID = userDetailsUtils.user!!.firebaseUID,
                                                     fullName = request.details.fullName))

                            consultationRequestsDao.addConsultationRequest(request)
                                .subscribe()

                            //AppController.notifyObservers(EVENT_SEND_REQUEST, true)
                            AppController.pushToTopic(EVENT_SEND_REQUEST, true)
                        }
                        else {
                            consultationRequestsDao.addConsultationRequest(request.apply { this.status = REQUEST_NOT_SENT })
                                .subscribe()

                            //AppController.notifyObservers(EVENT_SEND_REQUEST, false)
                            AppController.pushToTopic(EVENT_SEND_REQUEST, false)
                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        consultationRequestsDao.addConsultationRequest(request.apply { this.status = REQUEST_NOT_SENT })
                            .subscribe()

                        Timber.e("onFailure: sending Cloud message request failed",t)

                        //AppController.notifyObservers(EVENT_SEND_REQUEST, false)
                        AppController.pushToTopic(EVENT_SEND_REQUEST, false)
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
        val message: String = "Hi i'm ${userDetailsUtils.user!!.fullName}. Please i would like a consultation with you"
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
            "/requests/${userDetailsUtils.user!!.firebaseUID}/to/$toUID/status" to status,
            "/requests/$toUID/from/${userDetailsUtils.user!!.firebaseUID}/status" to status )

        firebaseDatabase
            .reference
            .updateChildren(childNodes)
            .addOnCompleteListener {
                if(it.isSuccessful)
                    Timber.e("updateRequest_sender: updates REQUEST status in DB successful" )
                else
                    Timber.e("updateRequest_sender: update REQUEST status in DB failed", it.exception )
            }

    }


}