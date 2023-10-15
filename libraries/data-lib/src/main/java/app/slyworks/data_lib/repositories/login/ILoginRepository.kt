package app.slyworks.data_lib.repositories.login

import app.slyworks.utils_lib.Outcome
import io.reactivex.rxjava3.core.Single


/**
 * Created by Joshua Sylvanus, 8:31 AM, 23-Sep-2023.
 */

interface ILoginRepository {
    fun login(email:String, password:String): Single<Outcome>
    fun handleForgotPassword(email:String): Single<Outcome>
}