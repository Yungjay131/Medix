package app.slyworks.data_lib.helpers.call

import app.slyworks.data_lib.model.view_entities.CallHistoryVModel
import app.slyworks.data_lib.model.view_entities.FBUserDetailsVModel
import app.slyworks.utils_lib.Outcome
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single


/**
 * Created by Joshua Sylvanus, 4:20 PM, 07-Oct-2023.
 */
interface ICallHelper {
    fun listenForVideoCallRequests(): Observable<List<FBUserDetailsVModel>>
    fun listenForVoiceCallRequests(): Observable<List<FBUserDetailsVModel>>
    fun listenForCallHistory(): Observable<List<CallHistoryVModel>>

    fun respondToVideoCall(firebaseUID: String,
                           status:String): Single<Outcome>
    fun respondToVoiceCall(firebaseUID: String,
                           status:String):Single<Outcome>

    fun sendVideoCallRequest(firebaseUID: String,
                             details:FBUserDetailsVModel):Single<Outcome>
    fun sendVoiceCallRequest(firebaseUID: String,
                             details:FBUserDetailsVModel):Single<Outcome>


    fun onVideoCallStarted(history:CallHistoryVModel)
    fun onVoiceCallStarted(history:CallHistoryVModel)

    fun onVideoCallStopped(): Single<Outcome>
    fun onVoiceCallStopped(): Single<Outcome>

    fun saveCallHistory(history:CallHistoryVModel):Single<Outcome>
}