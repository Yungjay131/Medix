package com.slyworks.medix

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.slyworks.constants.EVENT_GET_DOCTOR_USERS
import com.slyworks.constants.EVENT_UPDATE_FCM_REGISTRATION_TOKEN
import com.slyworks.constants.OUTGOING_MESSAGE
import com.slyworks.medix.utils.*
import com.slyworks.data.AppDatabase
import com.slyworks.models.models.Outcome
import com.slyworks.models.room_models.FBUserDetails
import com.slyworks.models.room_models.FBUserDetailsWrapper
import com.slyworks.models.room_models.Message
import com.slyworks.models.room_models.Person
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*


/**
 *Created by Joshua Sylvanus, 3:18 AM, 1/8/2022.
 */
object UsersManager {
    //region Vars
    private val TAG: String? = UsersManager::class.simpleName

    private val mFirebaseAuth:FirebaseAuth = FirebaseAuth.getInstance()
    private val mFirebaseDatabase:FirebaseDatabase = FirebaseDatabase.getInstance()
    private val mReference:DatabaseReference =  mFirebaseDatabase.reference
                                                                 .child("user-names")

    private val Context.userDetailsProtoDataStore: DataStore<UserDetails> by
    dataStore(
        fileName = "user_details.pb",
        serializer = UserDataSerializer)

    private var mDoctorChildEventListener:ChildEventListener? = null
    private var mUserDataValueEventListener:ValueEventListener? = null
    //endregion

    private fun userDataValueEventListener(o:PublishSubject<FBUserDetails>):ValueEventListener{
        mUserDataValueEventListener = MValueEventListener(
            onDataChangeFunc = {
                val userDetails: FBUserDetails = it.getValue(FBUserDetails::class.java)!!
                o.onNext(userDetails)
            })

        return mUserDataValueEventListener!!
    }

    fun getUserDataForUID(firebaseUID:String): Observable<FBUserDetails> {
        val o:PublishSubject<FBUserDetails> = PublishSubject.create()

        getUserDataForUIDRef(firebaseUID)
            .addValueEventListener(userDataValueEventListener(o))

        return o.hide()
    }

    fun detachUserDataListener(firebaseUID: String){
       getUserDataForUIDRef(firebaseUID)
           .removeEventListener(mUserDataValueEventListener!!)

        mUserDataValueEventListener = null
    }

    fun getAllDoctors(){
           getAllDoctorsRef()
           .get()
           .addOnCompleteListener {
               if(it.isSuccessful){
                   val list:MutableList<FBUserDetails> = mutableListOf()
                   it.result!!.children.forEach { child ->
                       Log.e(TAG, "getAllDoctors: $child" )
                       val doctor: FBUserDetailsWrapper = child.getValue(FBUserDetailsWrapper::class.java)!!
                       list.add(doctor.details)

                       AppController.notifyObservers(
                           EVENT_GET_DOCTOR_USERS,
                           Pair<Boolean, MutableList<FBUserDetails>?>(true, list)
                       )
                   }
               }else{
                   Log.e(TAG, "error occurred getting doctor users from DB", it.exception)

                   AppController.notifyObservers(
                       EVENT_GET_DOCTOR_USERS,
                       Pair<Boolean, MutableList<FBUserDetails>?>(false, null)
                   )
               }
           }
    }

    private fun doctorChildEventListener(o:PublishSubject<Outcome>):ChildEventListener{
           mDoctorChildEventListener = MChildEventListener(
               onChildAddedFunc = { snapshot ->
                       val list:MutableList<FBUserDetails> = mutableListOf()
                       snapshot.children.forEach {
                           val user = snapshot.getValue(FBUserDetails::class.java)
                           list.add(user!!)
                       }

                       o.onNext(Outcome.SUCCESS(list))
               })

           return mDoctorChildEventListener!!
    }

    fun observeDoctors():Observable<Outcome>{
        val o = PublishSubject.create<Outcome>()
        getAllDoctorsRef()
            .addChildEventListener(doctorChildEventListener(o))

        return o.hide()
    }

    fun detachGetAllDoctorsListener() {
         getAllDoctorsRef()
             .removeEventListener(mDoctorChildEventListener!!)

        mDoctorChildEventListener = null
    }

    fun clearUserDetails() {
        CoroutineScope(Dispatchers.IO).launch{
            App.getContext().userDetailsProtoDataStore.updateData {
                it.toBuilder()
                    .clear()
                    .build()
            }
        }
    }

    suspend fun saveUserToDataStore(userDetails: FBUserDetails){
        //updateUserLocalData(userDetails)
            App.getContext().userDetailsProtoDataStore.updateData { details ->
                val _details:UserDetails.Builder = details.toBuilder()
                    .clearHistory()
                    .clearSpecialization()
                    .setAccountType(userDetails.accountType)
                    .setFirstName(userDetails.firstName)
                    .setLastName(userDetails.lastName)
                    .setFullName(userDetails.fullName)
                    .setEmail(userDetails.email)
                    .setSex(userDetails.sex)
                    .setAge(userDetails.age)
                    .setFirebaseUID(userDetails.firebaseUID)
                    .setAgoraUID(userDetails.agoraUID)
                    .setFBRegistrationToken(userDetails.FCMRegistrationToken)
                    .setImageUri(userDetails.imageUri)

                if(userDetails.accountType == "PATIENT")
                    _details.addAllHistory(userDetails.history)
                else
                    _details.addAllSpecialization(userDetails.specialization)

                _details.build()
            }
    }

    private fun updateUserLocalData(userDetails: FBUserDetails){
       UserDetailsUtils.user = userDetails
    }

    fun getUserFromDataStore():Flow<FBUserDetails>{
        return App.getContext().userDetailsProtoDataStore
                               .data
                               .map { userDetails ->
                                   var history: MutableList<String>? = null
                                   var specialization: MutableList<String>? = null
                                   if (userDetails.accountType == "PATIENT")
                                       history = userDetails.historyList
                                   else
                                       specialization = userDetails.specializationList

                                   FBUserDetails(
                                       userDetails.accountType,
                                       userDetails.firstName,
                                       userDetails.lastName,
                                       userDetails.fullName,
                                       userDetails.email,
                                       userDetails.sex,
                                       userDetails.age,
                                       userDetails.firebaseUID,
                                       userDetails.agoraUID,
                                       userDetails.fbRegistrationToken,
                                       userDetails.imageUri,
                                       history,
                                       specialization
                                   )

                               }
    }

    fun sendFCMTokenToServer(token:String){
        val address:String =
        if(!UserDetailsUtils.user?.firebaseUID.isNullOrEmpty())
            UserDetailsUtils.user!!.firebaseUID
        else return

       getFCMRegistrationTokenRef(address)
            .setValue(token)
            .addOnCompleteListener {
                if(it.isSuccessful){
                    AppController.notifyObservers(EVENT_UPDATE_FCM_REGISTRATION_TOKEN, true)
                }else{
                    Log.e(TAG, "sendFCMTokenToServer: uploading FCMToken to DB failed", it.exception)
                    AppController.notifyObservers(EVENT_UPDATE_FCM_REGISTRATION_TOKEN, true)
                }
            }
    }


    fun updateUserInfo(details: FBUserDetails) {
        /*update user details in Person and Messages*/
        /*TODO:do from WorkManager*/
        CoroutineScope(Dispatchers.IO).launch {
            /*TODO:make both tasks run at the same time within this coroutine*/
            val childJob_1 = launch {
                val l:List<Message> =
                    AppDatabase.getInstance(App.getContext())
                        .getMessageDao()
                        .getMessagesForUID(details.firebaseUID)
                        .map {
                            it.accountType = details.accountType
                            it.senderImageUri = details.imageUri
                            if(it.type == OUTGOING_MESSAGE){
                                it.senderFullName = details.fullName
                            }else{
                                it.receiverFullName = details.fullName
                            }

                            it
                        }

                AppDatabase.getInstance(App.getContext())
                    .getMessageDao()
                    .updateMessage(*l.toTypedArray())
            }

            val childJob_2 = launch {
                val p: Person = AppDatabase.getInstance(App.getContext())
                    .getPersonDao()
                    .getPersonByID(details.firebaseUID)

                p.fullName = details.fullName
                p.senderImageUri = details.imageUri

                AppDatabase.getInstance(App.getContext())
                    .getPersonDao()
                    .updatePerson(p)
            }

            listOf(childJob_1,childJob_2).joinAll()
        }

    }




}