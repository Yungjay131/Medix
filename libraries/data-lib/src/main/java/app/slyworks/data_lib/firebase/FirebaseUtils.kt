package app.slyworks.data_lib.firebase

import app.slyworks.utils_lib.*
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference


/**
 * Created by Joshua Sylvanus, 2:06 PM, 03/06/2022.
 */

class FirebaseUtils(private val firebaseDB: FirebaseDatabase,
                    private val firebaseStorage: FirebaseStorage,
                    private val firebaseFS: FirebaseFirestore) {

    fun createNewCallHistoryRef(uid:String, docID:String):DocumentReference =
        firebaseFS.collection("call_history")
            .document("$uid/$docID")

    fun getCallHistoryRef(uid:String):DocumentReference =
        firebaseFS.collection("call_history")
            .document(uid)

    fun getMissedCallHistoryRef(uid:String):com.google.firebase.firestore.Query =
        firebaseFS.collection("call_history")
            .whereEqualTo("type", MISSED_CALL)

    fun getEncryptionDetailsRef():DocumentReference =
        firebaseFS.collection("encryption_details")
            .document("details")


    fun getIsUserTypingRef(myUID:String,
                           userUID:String):DatabaseReference =
        firebaseDB.reference
            .child("typing")
            .child(userUID)
            .child(myUID)


    fun getUserFCMRegistrationRef(uid: String): DatabaseReference {
        return firebaseDB
            .reference
            .child("users")
            .child(uid)
            .child("details")
            .child("fcm_registration_token")
    }


    fun getVoiceCallRequestsRef(uid: String): Query {
        return firebaseDB
            .reference
            .child("voice_call_requests")
            .child(uid)
            .orderByChild("from")
            .equalTo(REQUEST_PENDING)
    }

    fun getVideoCallRequestsRef(uid: String): Query {
        return firebaseDB
            .reference
            .child("video_call_requests")
            .child(uid)
            .orderByChild("from")
            .equalTo(REQUEST_PENDING)
    }

    fun getFCMRegistrationTokenRefPath(params: String): String {
        return "/users/$params/details/FCMRegistrationToken"
    }

    fun getUserMessagesRef(params: String): Query {
        return firebaseDB
            .reference
            .child("messages")
            .child(params)
            .orderByChild("type")
            .equalTo(INCOMING_MESSAGE)
            .orderByChild("status")
            .equalTo(DELIVERED)
    }

    fun getAllDoctorsRef(): Query {
        return firebaseDB
            .reference
            .child("users")
            .orderByChild("details/accountType")
            .equalTo("DOCTOR")

    }

    fun getUserDataForUIDRef(params: String): DatabaseReference {
        return firebaseDB
            .reference
            .child("users")
            .child(params)
            .child("details")
    }

    fun getUserVerificationStatusRef(UID:String):DatabaseReference =
        firebaseDB
            .reference
            .child("users")
            .child(UID)
            .child("is_verified")

    /* received consultation requests */
    fun getConsultationRequestsRef(uid:String):DatabaseReference{
        return firebaseDB
            .reference
            .child("requests")
            .child(uid)
            .child("from")
    }

    /* responses to sent consultation requests */
    fun getConsultationResponsesRef(uid:String):DatabaseReference{
        return firebaseDB
            .reference
            .child("requests")
            .child(uid)
            .child("to")
    }

    fun getReceivedConsultationRequestsRef(uid: String): Query {
        return firebaseDB
            .reference
            .child("requests")
            .child(uid)
            .child("from")
            .orderByChild("status")
            .equalTo(REQUEST_PENDING)
    }

    fun getUserProfileImageStorageRef(params: String): StorageReference {
        return firebaseStorage
            .reference
            .child("users")
            .child(params)
            .child("profile-image")
    }
}