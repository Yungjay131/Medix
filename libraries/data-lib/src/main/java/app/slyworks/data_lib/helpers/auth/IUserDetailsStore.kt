package app.slyworks.data_lib.helpers.auth

import app.slyworks.data_lib.vmodels.FBUserDetailsVModel
import app.slyworks.utils_lib.FBU_USER_DETAILS
import app.slyworks.utils_lib.Outcome
import io.reactivex.rxjava3.core.Single


/**
 * Created by Joshua Sylvanus, 12:14 PM, 17-Sep-2023.
 */
interface IUserDetailsStore {
    fun saveUserDetails(userDetails: FBUserDetailsVModel): Single<Outcome>
    fun getUserDetails(): FBUserDetailsVModel
    fun <T> getUserDetailsProperty(propertyKey:String = FBU_USER_DETAILS):T?
    fun clearUserDetails():Single<Outcome>
}