package app.slyworks.data_lib.repositories.registration

import android.app.Activity
import app.slyworks.data_lib.model.models.TempUserDetails
import app.slyworks.utils_lib.Outcome
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single


/**
 * Created by Joshua Sylvanus, 7:07 AM, 25-Sep-2023.
 */
interface IRegistrationRepository {
    fun registerUser(details: TempUserDetails): Single<Outcome>
    fun verifyViaEmail(email:String): Single<Outcome>
    fun resendOTP()
    fun receiveSMSCodeForOTP(smsCode:String)
    fun verifyViaOTP(phoneNumber:String, activity: Activity): Observable<Outcome>
}