package app.slyworks.payment_lib

import android.app.Activity
import app.slyworks.utils_lib.Outcome

interface PaymentAgent{
    suspend fun pay(activity: Activity? = null, details: PaymentDetails): Outcome
}