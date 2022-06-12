package com.slyworks.medix.broadcast_receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.firebase.database.FirebaseDatabase
import com.slyworks.constants.*
import com.slyworks.medix.CloudMessageManager
import com.slyworks.medix.UserDetailsUtils
import com.slyworks.medix.utils.*
import com.slyworks.models.models.ConsultationResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


/**
 *Created by Joshua Sylvanus, 11:30 PM, 1/13/2022.
 */
class CloudMessageBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {

        val fromUID:String = intent!!.getStringExtra(EXTRA_CLOUD_MESSAGE_FROM_UID)!!
        val toUID:String = intent.getStringExtra(EXTRA_CLOUD_MESSAGE_TO_UID)!!
        val status:String? = intent.getStringExtra(EXTRA_CLOUD_MESSAGE_STATUS)
        val fullName:String? = intent.getStringExtra(EXTRA_CLOUD_MESSAGE_FULLNAME)

        var responseType:String = REQUEST_PENDING
        val response_accept = intent.getStringExtra(EXTRA_CLOUD_MESSAGE_TYPE_ACCEPT)
        val response_decline = intent.getStringExtra(EXTRA_CLOUD_MESSAGE_TYPE_DECLINE)

        if(response_accept == null){
            responseType = response_decline!!
        }else if(response_decline == null)
            responseType = response_accept

        val pendingResult:PendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            val response: ConsultationResponse =
                ConsultationResponse(UserDetailsUtils.user!!.firebaseUID, toUID, status!!, fullName!!)
            CloudMessageManager.sendConsultationRequestResponse(response, pendingResult)
            uploadRequestResponseToDB(responseType, fromUID, toUID, pendingResult)
        }
    }

    private fun uploadRequestResponseToDB(responseType:String,fromUID:String, toUID:String, pendingResult: PendingResult ){
        val childNodeUpdate:HashMap<String, Any> = hashMapOf(
            "/requests/$fromUID/to/$toUID/status" to responseType,
            "/requests/$toUID/from/$fromUID/status" to responseType)

        FirebaseDatabase.getInstance().reference
            .updateChildren(childNodeUpdate)
            .addOnCompleteListener {
                pendingResult.finish()
            }
    }
}