package app.slyworks.data_lib.helpers.consultations

import app.slyworks.data_lib.FCMClientApi
import app.slyworks.data_lib.FirebaseCloudMessage
import app.slyworks.data_lib.firebase.FirebaseUtils
import app.slyworks.data_lib.helpers.storage.IUserDetailsHelper
import app.slyworks.data_lib.model.fcm_models.ConsultationRequestData
import app.slyworks.data_lib.model.models.ConsultationResponse
import app.slyworks.data_lib.model.view_entities.ConsultationRequestVModel
import app.slyworks.data_lib.firebase.MChildEventListener
import app.slyworks.utils_lib.*
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber


/**
 * Created by Joshua Sylvanus, 6:37 AM, 07-Oct-2023.
 */
class ConsultationsHelper(private val firebaseDB:FirebaseDatabase,
                          private val fcmClientApi: FCMClientApi,
                          private val firebaseUtils: FirebaseUtils,
                          private val userDetailsHelper:IUserDetailsHelper) : IConsultationsHelper {
    //region Vars

    //endregion

    override fun listenForConsultationRequestsUpdates()
    : Observable<List<ConsultationRequestVModel>> {
        return Observable.create { emitter ->
            val func:(DataSnapshot) -> Unit = {snapshot:DataSnapshot ->
                val request: ConsultationRequestVModel =
                    snapshot.getValue(ConsultationRequestVModel::class.java)!!

                emitter.onNext(listOf())
            }

            val uid:String = userDetailsHelper.getUserDetailsProperty<String>(FBU_FIREBASE_UID)!!
            firebaseUtils.getConsultationRequestsRef(uid)
                .addChildEventListener(
                    MChildEventListener(
                        onChildAddedFunc = func,
                        onChildChangedFunc = func,
                    )
                )
        }
    }

    override fun listenForResponsesToSentConsultationRequests(): Observable<List<ConsultationRequestVModel>> {
        return Observable.create { emitter ->
            val func:(DataSnapshot) -> Unit = {snapshot:DataSnapshot ->
                val request: ConsultationRequestVModel =
                    snapshot.getValue(ConsultationRequestVModel::class.java)!!

                emitter.onNext(listOf())
            }

            val uid:String = userDetailsHelper.getUserDetailsProperty<String>(FBU_FIREBASE_UID)!!
            firebaseUtils.getConsultationResponsesRef(uid)
                .addChildEventListener(
                    MChildEventListener(
                        onChildAddedFunc = func,
                        onChildChangedFunc = func,
                    )
                )
        }
    }

    /* not sure zip() is the right approach here, come back and review */
    override fun sendResponseToConsultationRequest(response: ConsultationResponse): Single<Outcome> {
        return Single.zip(
            sendResponseToConsultationRequestViaFCM(response),
            sendResponseToConsultationRequestViaFirebaseDB(response)
        ) { o1: Outcome, o2: Outcome ->
            /* only successful if both were sent */
            if (o1.isSuccess && o2.isSuccess) {
                return@zip Outcome.SUCCESS(Unit)
            } else {
                /* logging of the reasons for failure would be in the send methods */
                return@zip Outcome.FAILURE(Unit, "response was not successfully sent, please try again")
            }

        }
    }

    override fun sendConsultationRequest(request: ConsultationRequestVModel): Single<Outcome> {
        return Single.create<Outcome> { emitter ->
            val childNodeUpdate:HashMap<String, Any> = hashMapOf(
                "/requests/${userDetailsHelper.getUserDetailsProperty<String>("firebaseUID")}/to/${request.toUID}" to request,
                "/requests/${request.toUID}/from/${userDetailsHelper.getUserDetailsProperty<String>("firebaseUID")}" to request)

            firebaseDB.reference
                .updateChildren(childNodeUpdate)
                .addOnCompleteListener {
                    if(it.isSuccessful){
                      emitter.onSuccess(Outcome.SUCCESS(Unit))
                    }else{
                      Timber.e(it.exception)
                      emitter.onSuccess(Outcome.FAILURE(Unit, it.exception?.message ?: "an error occurred sending your consultation request.\nPlease try again"))
                    }
                }
        }
        .flatMap{
            if(!it.isSuccess){
                return@flatMap Single.just(it)
            } else {
                /* if we were able to send to FirebaseDB, send the cloud message */
                return@flatMap Single.create { emitter ->
                    val fcmMessage: FirebaseCloudMessage = request.toFirebaseCloudMessage()

                    fcmClientApi.sendCloudMessage(fcmMessage)
                        .enqueue(object : Callback<ResponseBody> {
                            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                                if (response.isSuccessful) {
                                   emitter.onSuccess(Outcome.SUCCESS(Unit))
                                }else{
                                    emitter.onSuccess(Outcome.FAILURE(Unit, "an error occurred sending your consultation request.\nPlease try again"))
                                }
                            }

                            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                                Timber.e(t)
                                emitter.onSuccess(Outcome.ERROR(Unit, "an error occurred sending your consultation request.\nPlease try again"))
                            }
                        })
                }
            }
        }
        .onErrorReturn {
            Timber.e(it)
            Outcome.FAILURE(Unit, "an error occurred sending your consultation request.\nPlease try again")
        }
    }

    private fun sendResponseToConsultationRequestViaFCM(response: ConsultationResponse): Single<Outcome> {
       return Single.create<Outcome> { emitter ->
           val fcmMessage:FirebaseCloudMessage = response.toFirebaseCloudMessage()

           fcmClientApi.sendCloudMessage(fcmMessage)
               .enqueue(object : Callback<ResponseBody> {
                   override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                       if (response.isSuccessful) {
                           emitter.onSuccess(Outcome.SUCCESS(Unit))
                       } else {
                           Timber.e(response.errorBody().toString())
                           emitter.onSuccess(Outcome.FAILURE(Unit, "an error occurred sending your consultation request"))
                       }
                   }

                   override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                       emitter.onSuccess(Outcome.ERROR(Unit, t.message))
                   }
               })
        }
        .onErrorReturn {
            Timber.e(it)
            Outcome.FAILURE(Unit, it.message)
        }
    }

    private fun sendResponseToConsultationRequestViaFirebaseDB(response: ConsultationResponse):Single<Outcome>{
        return Single.create<Outcome> { emitter ->
            val childNodeUpdate:HashMap<String, Any> = hashMapOf(
                "/requests/${userDetailsHelper.getUserDetailsProperty<String>("firebaseUID")}/from/${response.toUID}/status" to response.status,
                "/requests/${response.toUID}/to/${userDetailsHelper.getUserDetailsProperty<String>("firebaseUID")}/status" to response.status)

            /* if the update failed, delete the newly added fields */
            val childNodeUpdateNull:HashMap<String, Any?> = hashMapOf(
                "/requests/${userDetailsHelper.getUserDetailsProperty<String>("firebaseUID")}/from/${response.toUID}/status" to null,
                "/requests/${response.toUID}/to/${userDetailsHelper.getUserDetailsProperty<String>("firebaseUID")}/status" to null)

            firebaseDB.reference
                .updateChildren(childNodeUpdate)
                .addOnCompleteListener {
                    if(it.isSuccessful){
                        emitter.onSuccess(Outcome.SUCCESS(Unit))
                    }else{
                        Timber.e(it.exception)

                        /* something went wrong, try to delete the newly added node */
                        firebaseDB.reference
                            .updateChildren(childNodeUpdateNull)
                            .addOnCompleteListener {it2: Task<Void> ->
                                emitter.onSuccess(Outcome.FAILURE(it.exception?.message,it2.exception?.message))
                            }
                    }
                }
        }
        .onErrorReturn {
            Timber.e(it)
            Outcome.FAILURE(Unit, it.message)
        }
    }

    private fun ConsultationResponse.toFirebaseCloudMessage():FirebaseCloudMessage {
        val _type:String = if(this.status == REQUEST_ACCEPTED) FCM_RESPONSE_ACCEPTED else FCM_RESPONSE_DECLINED
        val action:String = if(this.status == REQUEST_ACCEPTED) "accepted" else "declined"
        val message:String = this.fullName + " $action your request for consultation"

        return FirebaseCloudMessage(
            to = this.toFCMRegistrationToken,
            data = ConsultationRequestData(
                message = message,
                fromUID = this.fromUID,
                fullName = this.fullName,
                toFCMRegistrationToken = this.toFCMRegistrationToken,
                type = _type
            )
        )
    }

    private fun ConsultationRequestVModel.toFirebaseCloudMessage():FirebaseCloudMessage {
        val message: String = "Hi i'm ${userDetailsHelper.getUserDetailsProperty<String>("fullName")}. Please i would like a consultation with you"

        return FirebaseCloudMessage(
            to = this.details.fcm_registration_token,
            data = ConsultationRequestData(
               message = message,
               fromUID = this.details.firebaseUID,
               fullName = this.details.fullName,
               toFCMRegistrationToken = this.details.fcm_registration_token,
               type = FCM_REQUEST,
            )
        )
    }
}