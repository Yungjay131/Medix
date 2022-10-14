package com.slyworks.auth

import com.google.firebase.database.*
import com.slyworks.constants.EVENT_GET_DOCTOR_USERS
import com.slyworks.constants.EVENT_UPDATE_FCM_REGISTRATION_TOKEN
import com.slyworks.constants.OUTGOING_MESSAGE
import com.slyworks.controller.AppController
import com.slyworks.firebase_commons.*
import com.slyworks.room.daos.MessageDao
import com.slyworks.room.daos.PersonDao
import com.slyworks.models.models.Outcome
import com.slyworks.models.room_models.FBUserDetails
import com.slyworks.models.room_models.FBUserDetailsWrapper
import com.slyworks.models.room_models.Message
import com.slyworks.models.room_models.Person
import com.slyworks.userdetails.UserDetailsUtils
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Named


/**
 *Created by Joshua Sylvanus, 3:18 AM, 1/8/2022.
 */
class UsersManager(
    private val personDao: PersonDao,
    private val messageDao:MessageDao,
    private val userDetailsUtils: UserDetailsUtils,
    private val firebaseUtils: FirebaseUtils) {
    //region Vars
    private val TAG: String? = UsersManager::class.simpleName

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

    fun observeUserDataForUID(firebaseUID:String): Observable<FBUserDetails> {
        val o:PublishSubject<FBUserDetails> = PublishSubject.create()

        firebaseUtils.getUserDataForUIDRef(firebaseUID)
            .addValueEventListener(userDataValueEventListener(o))

        return o.hide()
    }

    fun detachUserDataListener(firebaseUID: String){
       firebaseUtils.getUserDataForUIDRef(firebaseUID)
           .removeEventListener(mUserDataValueEventListener!!)

        mUserDataValueEventListener = null
    }

    fun getUserDataForUID(firebaseUID:String):Observable<Outcome> =
        Observable.create { emitter ->
           firebaseUtils.getUserDataForUIDRef(firebaseUID)
               .get()
               .addOnCompleteListener {
                   if (it.isSuccessful){
                       val userDetails: FBUserDetails = it.result!!.getValue(FBUserDetails::class.java)!!
                       emitter.onNext(Outcome.SUCCESS(value = userDetails))
                   } else{
                       emitter.onNext(Outcome.FAILURE(value = it.exception?.message))
                   }

                   emitter.onComplete()
               }
        }

    fun getAllDoctors(){
           firebaseUtils.getAllDoctorsRef()
           .get()
           .addOnCompleteListener {
               if(it.isSuccessful){
                   val list:MutableList<FBUserDetails> = mutableListOf()
                   it.result!!.children.forEach { child ->
                       Timber.e("getAllDoctors: $child" )
                       val doctor: FBUserDetailsWrapper = child.getValue(FBUserDetailsWrapper::class.java)!!
                       list.add(doctor.details)

                       AppController.notifyObservers(
                           EVENT_GET_DOCTOR_USERS,
                           Pair<Boolean, MutableList<FBUserDetails>?>(true, list)
                       )
                   }
               }else{
                   Timber.e("error occurred getting doctor users from DB", it.exception)

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
        firebaseUtils.getAllDoctorsRef()
            .addChildEventListener(doctorChildEventListener(o))

        return o.hide()
    }

    fun detachGetAllDoctorsListener() {
         firebaseUtils.getAllDoctorsRef()
             .removeEventListener(mDoctorChildEventListener!!)

        mDoctorChildEventListener = null
    }

    fun clearUserDetails():Unit  = userDetailsUtils.clearUserDetails()

    suspend fun saveUserToDataStore(userDetails: FBUserDetails){
            updateUserLocalData(userDetails)
            userDetailsUtils.saveUserToDataStore(userDetails)
    }

    private fun updateUserLocalData(userDetails: FBUserDetails){
       userDetailsUtils.user = userDetails
    }

    fun getUserFromDataStore(): Flow<FBUserDetails> = userDetailsUtils.getUserFromDataStore()

    /*TODO:enqueue work if the user isnt logged in yet*/
    fun sendFCMTokenToServer(token:String){
        val address:String =
        if(!userDetailsUtils.user?.firebaseUID.isNullOrEmpty())
            userDetailsUtils.user!!.firebaseUID
        else return

       firebaseUtils.getFCMRegistrationTokenRef(address)
            .setValue(token)
            .addOnCompleteListener {
                if(it.isSuccessful){
                    AppController.notifyObservers(EVENT_UPDATE_FCM_REGISTRATION_TOKEN, true)
                }else{
                    Timber.e("sendFCMTokenToServer: uploading FCMToken to DB failed", it.exception)
                    AppController.notifyObservers(EVENT_UPDATE_FCM_REGISTRATION_TOKEN, true)
                }
            }
    }


    fun updateUserInfo(details: FBUserDetails):Observable<Boolean> =
        getMessagesForUIDObservable(details)
            .flatMap {
                if(it)
                    getUpdatePersonObservable(details)
                else
                    Observable.just(false)
            }

    private fun getMessagesForUIDObservable(details: FBUserDetails):Observable<Boolean> =
        Observable.create { emitter ->
            CoroutineScope(Dispatchers.IO).launch {
                val l:List<Message> =
                    messageDao
                        .getMessagesForUID(details.firebaseUID)

                if(l.isNullOrEmpty()){
                    emitter.onNext(false)
                    emitter.onComplete()
                    this.cancel()
                }

                l.map {
                    it.accountType = details.accountType
                    it.senderImageUri = details.imageUri
                    if(it.type == OUTGOING_MESSAGE)
                        it.senderFullName = details.fullName
                    else
                        it.receiverFullName = details.fullName

                    it
                }

               messageDao
                    .updateMessage(*l.toTypedArray())

                emitter.onNext(true)
                emitter.onComplete()
                this.cancel()
            }
        }

    private fun getUpdatePersonObservable(details: FBUserDetails):Observable<Boolean> =
        Observable.create { emitter ->
            CoroutineScope(Dispatchers.IO).launch {
                val p: Person? = personDao
                    .getPersonByID(details.firebaseUID)

                if(p == null){
                    emitter.onNext(false)
                    emitter.onComplete()
                    this.cancel()
                }

                p!!
                p.fullName = details.fullName
                p.imageUri = details.imageUri

               personDao
                    .updatePerson(p)
            }
        }

}