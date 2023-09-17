package app.slyworks.data_lib.helpers.auth

import app.slyworks.utils_lib.Outcome
import io.reactivex.rxjava3.core.Single


/**
 *Created by Joshua Sylvanus, 9:32 AM, 17-Sep-2023.
 */
interface ILoginHelper {
    fun getLoggedInStatus():Boolean
    fun handleForgotPassword(email:String): Single<Outcome>
    fun logoutUser():Single<Outcome>
    fun loginUser(email:String, password:String): Single<Outcome>
}