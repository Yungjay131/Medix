package app.slyworks.payment_lib

import android.app.Activity
import app.slyworks.utils_lib.Outcome


/**
 * Created by Joshua Sylvanus, 10:28 PM, 29-Apr-2023.
 */
class PaymentManager(private val agent:PaymentAgent) {
   suspend fun pay(activity: Activity? = null, details:PaymentDetails):Outcome =
       agent.pay(activity, details)
}




