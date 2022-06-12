package com.slyworks.medix

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.slyworks.constants.DELIVERED
import com.slyworks.constants.INCOMING_MESSAGE
import com.slyworks.constants.REQUEST_PENDING


/**
 *Created by Joshua Sylvanus, 2:06 PM, 03/06/2022.
 */

fun getUserDataRef(params:String = UserDetailsUtils.user!!.firebaseUID):DatabaseReference{
    return FirebaseDatabase.getInstance()
        .reference
        .child("users")
        .child(params)
        .child("details")
        .child("fcm_registration_token")
}


fun getVoiceCallRequestsRef(param: String = UserDetailsUtils.user!!.firebaseUID):Query{
    return FirebaseDatabase.getInstance()
        .reference
        .child("voice_call_requests")
        .child(param)
        .orderByChild("from")
        .equalTo(REQUEST_PENDING)
}

fun getVideoCallRequestsRef(param:String = UserDetailsUtils.user!!.firebaseUID):Query{
    return FirebaseDatabase.getInstance()
        .reference
        .child("video_call_requests")
        .child(param)
        .orderByChild("from")
        .equalTo(REQUEST_PENDING)
}

fun getFCMRegistrationTokenRefPath(params:String):String{
    return "/users/$params/details/FCMRegistrationToken"
}

fun getUserMessagesRef(params:String = UserDetailsUtils.user!!.firebaseUID):Query{
    return FirebaseDatabase.getInstance()
        .reference
        .child("messages")
        .child(UserDetailsUtils.user!!.firebaseUID)
        .orderByChild("type")
        .equalTo(INCOMING_MESSAGE)
        .orderByChild("status")
        .equalTo(DELIVERED)
}

fun getFCMRegistrationTokenRef(params:String):DatabaseReference{
    return FirebaseDatabase.getInstance()
        .reference
        .child("users")
        .child(params)
        .child("details")
        .child("FCMRegistrationToken")
}

fun getAllDoctorsRef(): Query{
    return FirebaseDatabase.getInstance()
        .reference
        .child("users")
        .orderByChild("details/accountType")
        .equalTo("DOCTOR")

}

fun getUserDataForUIDRef(params:String): DatabaseReference {
    return FirebaseDatabase.getInstance()
        .reference
        .child("users")
        .child(params)
        .child("details")
}

fun getUserConsultationRequestsRef(params:String): Query {
    return FirebaseDatabase.getInstance()
        .reference
        .child("users")
        .child(params)
        .child("requests")
        .child("from")
        .equalTo(REQUEST_PENDING)
}

fun getUserSentConsultationRequestsRef(params:String,params2:String):DatabaseReference{
    return FirebaseDatabase.getInstance()
        .reference
        .child("users")
        .child(params)
        .child("requests")
        .child("to")
        .child(params2)
}

fun getUserProfileImageStorageRef(params:String): StorageReference {
    return FirebaseStorage.getInstance()
        .reference
        .child("users")
        .child(params)
        .child("profile-image")
}