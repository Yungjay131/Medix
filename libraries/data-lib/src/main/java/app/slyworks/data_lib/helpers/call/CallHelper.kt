package app.slyworks.data_lib.helpers.call

import app.slyworks.data_lib.FCMClientApi
import app.slyworks.data_lib.FirebaseCloudMessage
import app.slyworks.data_lib.firebase.FirebaseUtils
import app.slyworks.data_lib.helpers.storage.IUserDetailsHelper
import app.slyworks.data_lib.model.fcm_models.VideoCallData
import app.slyworks.data_lib.model.fcm_models.VoiceCallData
import app.slyworks.data_lib.model.view_entities.CallHistoryVModel
import app.slyworks.data_lib.model.view_entities.FBUserDetailsVModel
import app.slyworks.data_lib.firebase.MChildEventListener
import app.slyworks.data_lib.firebase.MEventListener
import app.slyworks.toVideoCallData
import app.slyworks.toVoiceCallData
import app.slyworks.utils_lib.FBU_FIREBASE_UID
import app.slyworks.utils_lib.IDHelper
import app.slyworks.utils_lib.Outcome
import app.slyworks.utils_lib.REQUEST_PENDING
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ktx.toObject
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleEmitter
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber


/**
 * Created by Joshua Sylvanus, 1:24 PM, 07-Oct-2023.
 */
class CallHelper(private val firebaseDB: FirebaseDatabase,
                 private val fcmClientApi: FCMClientApi,
                 private val firebaseUtils:FirebaseUtils,
                 private val idHelper: IDHelper,
                 private val userDetailsHelper:IUserDetailsHelper) : ICallHelper {
    //region Vars
    private var currentVideoCall: CallHistoryVModel? = null
    private var currentVideoCallStartTime:Long? = null
    private var currentVideoCallEndTime:Long? = null

    private var currentVoiceCall: CallHistoryVModel? = null
    private var currentVoiceCallStartTime:Long? = null
    private var currentVoiceCallEndTime:Long? = null
    //endregion

    override fun listenForVideoCallRequests(): Observable<List<FBUserDetailsVModel>> {
        return Observable.create { emitter ->
            val func:(DataSnapshot) -> Unit = { snapshot:DataSnapshot ->
                val l:MutableList<FBUserDetailsVModel> = mutableListOf()
                snapshot.children.forEach {
                    val request: FBUserDetailsVModel = it.getValue(FBUserDetailsVModel::class.java)!!
                    l.add(request)
                }
            }

            val uid:String = userDetailsHelper.getUserDetailsProperty<String>(FBU_FIREBASE_UID)!!
            firebaseUtils.getVideoCallRequestsRef(uid)
                .addChildEventListener(
                    MChildEventListener(
                        onChildAddedFunc = func,
                        onChildChangedFunc = func
                    )
                )
        }
    }

    override fun listenForVoiceCallRequests():Observable<List<FBUserDetailsVModel>>{
        return Observable.create{ emitter ->
            val func:(DataSnapshot) -> Unit = { snapshot:DataSnapshot ->
                val l:MutableList<FBUserDetailsVModel> = mutableListOf()
                snapshot.children.forEach {
                    val request: FBUserDetailsVModel = it.getValue(FBUserDetailsVModel::class.java)!!
                    l.add(request)
                }
            }

            val uid:String = userDetailsHelper.getUserID()!!
            firebaseUtils.getVoiceCallRequestsRef(uid)
                .addChildEventListener(
                    MChildEventListener(
                        onChildAddedFunc = func,
                        onChildChangedFunc = func
                    )
                )
        }
    }

    override fun listenForCallHistory(): Observable<List<CallHistoryVModel>> {
        return Observable.create { emitter ->
            val func:(DocumentSnapshot) -> Unit = {
               /* TODO: handle issue of multiple calls */
               val l:List<CallHistoryVModel> = it.data?.values as List<CallHistoryVModel>
               emitter.onNext(l)
            }

            val uid:String = userDetailsHelper.getUserID()!!
            firebaseUtils.getCallHistoryRef(uid)
                .addSnapshotListener(
                    MEventListener(
                        onEventFunc = func
                    )
                )
        }
    }

    override fun respondToVideoCall(firebaseUID:String,
                                    status:String):Single<Outcome>{
        return Single.create<Outcome> { emitter ->
            /* first save to firebaseDB */
            val childNodeUpdate:HashMap<String, Any> = hashMapOf(
                "/video_call_requests/${userDetailsHelper.getUserDetailsProperty<String>("firebaseUID")}/from/$firebaseUID/status" to status,
                "/video_call_requests/$firebaseUID/to/${userDetailsHelper.getUserDetailsProperty<String>("firebaseUID")}/status" to status
            )

            firebaseDB.reference
                .updateChildren(childNodeUpdate)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        val o: Outcome = Outcome.SUCCESS(Unit)
                        emitter.onSuccess(o)
                    } else {
                        Timber.e( it.exception)

                        val o: Outcome = Outcome.FAILURE(Unit, "an error occurred trying to answer your voice call")
                        emitter.onSuccess(o)
                    }
                }
        }
    }

    override fun respondToVoiceCall(firebaseUID:String,
                                    status:String):Single<Outcome>{
        return Single.create<Outcome> { emitter ->
            /* first save to firebaseDB */
            val childNodeUpdate:HashMap<String, Any> = hashMapOf(
                "/voice_call_requests/${userDetailsHelper.getUserDetailsProperty<String>("firebaseUID")}/from/$firebaseUID/status" to status,
                "/voice_call_requests/$firebaseUID/to/${userDetailsHelper.getUserDetailsProperty<String>("firebaseUID")}/status" to status
            )

            firebaseDB.reference
                .updateChildren(childNodeUpdate)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        val o: Outcome = Outcome.SUCCESS(Unit)
                        emitter.onSuccess(o)
                    } else {
                        Timber.e( it.exception)

                        val o: Outcome = Outcome.FAILURE(Unit, "an error occurred trying to answer your voice call")
                        emitter.onSuccess(o)
                    }
                }
        }
    }

    override fun sendVideoCallRequest(firebaseUID:String,
                                      details: FBUserDetailsVModel):Single<Outcome> {
        return Single.create { emitter:SingleEmitter<Outcome> ->
           val childNodeUpdate:HashMap<String, Any> = hashMapOf(
                   "/video_call_requests/${userDetailsHelper.getUserDetailsProperty<String>("firebaseUID")}/from/$firebaseUID/status" to REQUEST_PENDING,
                   "/video_call_requests/$firebaseUID/to/${userDetailsHelper.getUserDetailsProperty<String>("firebaseUID")}/status" to REQUEST_PENDING)

            firebaseDB.reference
                .updateChildren(childNodeUpdate)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        val o: Outcome = Outcome.SUCCESS(Unit)
                        emitter.onSuccess(o)
                    } else {
                        Timber.e( it.exception)

                        val o: Outcome = Outcome.FAILURE(Unit, "an error occurred placing your voice call")
                        emitter.onSuccess(o)
                    }
                }
        }
        .flatMap{
            if(!it.isSuccess)
                return@flatMap Single.just(it)
            else{
                Single.create<Outcome> {emitter:SingleEmitter<Outcome> ->
                    val data: VideoCallData = details.toVideoCallData()
                    val fcMessage: FirebaseCloudMessage =
                        FirebaseCloudMessage(
                            to = details.firebaseUID,
                            data = data)

                    fcmClientApi
                        .sendCloudMessage(fcMessage)
                        .enqueue(
                            object: Callback<ResponseBody> {
                                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                                    val o: Outcome = Outcome.SUCCESS(Unit)
                                    emitter.onSuccess(o)
                                }

                                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                                    Timber.e(t)

                                    val o: Outcome = Outcome.FAILURE(Unit, "an error occurred placing your voice call")
                                    emitter.onSuccess(o)
                                }

                            })
                }
            }
        }
    }

    override fun sendVoiceCallRequest(firebaseUID:String,
                                      details: FBUserDetailsVModel):Single<Outcome> {
        return Single.create { emitter:SingleEmitter<Outcome> ->
           val childNodeUpdate:HashMap<String, Any> = hashMapOf(
                   "/voice_call_requests/${userDetailsHelper.getUserDetailsProperty<String>("firebaseUID")}/from/$firebaseUID/status" to REQUEST_PENDING,
                   "/voice_call_requests/$firebaseUID/to/${userDetailsHelper.getUserDetailsProperty<String>("firebaseUID")}/status" to REQUEST_PENDING)

            firebaseDB.reference
                .updateChildren(childNodeUpdate)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        val o: Outcome = Outcome.SUCCESS(Unit)
                        emitter.onSuccess(o)
                    } else {
                        Timber.e( it.exception)

                        val o: Outcome = Outcome.FAILURE(Unit, "an error occurred placing your video call")
                        emitter.onSuccess(o)
                    }
                }
        }
        .flatMap{
            if(!it.isSuccess)
                return@flatMap Single.just(it)
            else{
                Single.create<Outcome> {emitter:SingleEmitter<Outcome> ->
                    val data: VoiceCallData = details.toVoiceCallData()
                    val fcMessage: FirebaseCloudMessage =
                        FirebaseCloudMessage(
                            to = details.firebaseUID,
                            data = data)

                    fcmClientApi
                        .sendCloudMessage(fcMessage)
                        .enqueue(
                            object: Callback<ResponseBody> {
                                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                                    val o: Outcome = Outcome.SUCCESS(Unit)
                                    emitter.onSuccess(o)
                                }

                                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                                    Timber.e(t)

                                    val o: Outcome = Outcome.FAILURE(Unit, "an error occurred placing you video call")
                                    emitter.onSuccess(o)
                                }

                            })
                }
            }
        }
    }

    override fun onVideoCallStarted(history: CallHistoryVModel) {
       currentVideoCallStartTime = System.currentTimeMillis()
       currentVideoCall = history
    }

    override fun onVoiceCallStarted(history: CallHistoryVModel) {
        currentVoiceCallStartTime = System.currentTimeMillis()
        currentVoiceCall = history
    }

    override fun onVideoCallStopped():Single<Outcome> {
       currentVideoCallEndTime = System.currentTimeMillis()

       val duration:Long = currentVideoCallEndTime!! - currentVideoCallStartTime!!
       currentVideoCall!!.duration = duration.toString()

       return saveCallHistory(currentVideoCall!!)
    }

    override fun onVoiceCallStopped():Single<Outcome> {
        currentVoiceCallEndTime = System.currentTimeMillis()

        val duration:Long = currentVoiceCallEndTime!! - currentVoiceCallStartTime!!
        currentVoiceCall!!.duration = duration.toString()

        return saveCallHistory(currentVoiceCall!!)
    }

    override fun saveCallHistory(history: CallHistoryVModel): Single<Outcome> {
       return Single.create { emitter ->
           val uid:String = userDetailsHelper.getUserID()!!
           val docID:String = idHelper.generateRandomID(24)
           firebaseUtils.createNewCallHistoryRef(uid, docID)
               .set(history)
               .addOnCompleteListener {
                   if(it.isSuccessful){
                       emitter.onSuccess(Outcome.SUCCESS(Unit))
                   }else{
                       Timber.e(it.exception)
                       emitter.onSuccess(
                           Outcome.FAILURE(Unit, "an error occurred saving your call history")
                       )
                   }
               }
       }
    }
}