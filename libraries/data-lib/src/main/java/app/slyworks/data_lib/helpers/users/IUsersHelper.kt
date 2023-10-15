package app.slyworks.data_lib.helpers.users

import app.slyworks.data_lib.model.view_entities.FBUserDetailsVModel
import app.slyworks.utils_lib.Outcome
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single


/**
 * Created by Joshua Sylvanus, 5:35 PM, 07-Oct-2023.
 */
interface IUsersHelper {
    fun sendFCMTokenToServer(token: String): Single<Outcome>

    fun updateUsersData(details: FBUserDetailsVModel): Single<Outcome>

    fun getAnotherUsersData(uid: String): Single<Outcome>

    fun listenForChangesToUsersData():Observable<FBUserDetailsVModel>

    fun listenForChangesToAnotherUsersData(userUID: String): Observable<FBUserDetailsVModel>

    fun getAllDoctorsData(): Single<Outcome>

    fun listenForNewDoctors(): Observable<List<FBUserDetailsVModel>>

}