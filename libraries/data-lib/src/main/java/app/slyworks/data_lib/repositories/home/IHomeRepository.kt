package app.slyworks.data_lib.repositories.home

import app.slyworks.data_lib.model.view_entities.CallHistoryVModel
import app.slyworks.data_lib.model.view_entities.FBUserDetailsVModel
import io.reactivex.rxjava3.core.Observable


/**
 * Created by Joshua Sylvanus, 7:26 PM, 11-Oct-2023.
 */
interface IHomeRepository {
    fun getUserFullName():String
    fun getUserProfilePicUri():String
    fun observeUserDetails(): Observable<FBUserDetailsVModel>
    fun observeCallsHistory(): Observable<List<CallHistoryVModel>>
}