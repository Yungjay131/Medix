package com.slyworks.medix

import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.slyworks.constants.EVENT_ROLLBACK_REGISTRATION
import com.slyworks.constants.EVENT_SEND_PASSWORD_RESET_EMAIL
import com.slyworks.constants.EVENT_USER_REGISTRATION
import com.slyworks.medix.concurrency.TaskManager
import com.slyworks.medix.concurrency.CompressImageCallable
import com.slyworks.medix.utils.*
import com.slyworks.models.models.AccountType
import com.slyworks.models.models.Observer
import com.slyworks.models.models.Outcome
import com.slyworks.models.models.TempUserDetails
import com.slyworks.models.room_models.FBUserDetails
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers


/**
 *Created by Joshua Sylvanus, 7:36 AM, 1/3/2022.
 */
/*
* "rules": {
    "users":{
       "$uid": {
        ".read":"$uid === auth.uid",
        ".write": "$uid === auth.uid"
      }
    },
    "user-names":{
       "$uid": {
        ".read":"$uid === auth.uid",
        ".write": "$uid === auth.uid"
      }
    }
  }*/
class  RegistrationManager {
    //region Vars
    private val TAG: String? = RegistrationManager::class.simpleName

    private val mFirebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val mFirebaseStorage: FirebaseStorage = FirebaseStorage.getInstance()
    private val mFirebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()

    private lateinit var mUser: TempUserDetails
    private lateinit var mCurrentFirebaseUserResult: AuthResult
    //endregion


    //fixme:this might not be the best way to run this sequentially
    fun register(userDetails: TempUserDetails): Observable<Outcome> {
        mUser = userDetails
        return createFirebaseUser()
            .concatMap {
                when {
                    it.isSuccess -> return@concatMap uploadUserProfileImage()
                    else -> {
                        return@concatMap deleteUser().concatMap { _ -> Observable.just(it) }
                    }
                }
            }.concatMap {
                when {
                    it.isSuccess -> return@concatMap createAgoraUser()
                    else ->{
                        return@concatMap deleteUserProfileImage().concatMap { _ ->  Observable.just(it)}
                    }
                }
            }.concatMap {
                when {
                    it.isSuccess -> return@concatMap getFCMRegistrationToken()
                    else -> return@concatMap Observable.just(it)

                }
            }.concatMap {
                when {
                    it.isSuccess -> return@concatMap uploadUserDetailsToFirebaseDB()
                    else -> return@concatMap Observable.just(it)
                }
            }.concatMap {
                when {
                    it.isSuccess -> return@concatMap sendVerificationEmail()
                    else ->{
                        return@concatMap deleteUserDetailsFromDB().concatMap { _ -> Observable.just(it) }
                    }
                }
            }

    }


    private fun createFirebaseUser(): Observable<Outcome> {
        return Observable.create { emitter ->
            mFirebaseAuth.createUserWithEmailAndPassword(mUser.email, mUser.password)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        mCurrentFirebaseUserResult = it.result!!
                        mUser.firebaseUID = it.result!!.user!!.uid
                        Log.e(TAG, "_createFirebaseUser: createUserWithEmailAndPassword() successful")

                        val r = Outcome.SUCCESS(value = "firebase user created successfully")
                        emitter.onNext(r)
                        emitter.onComplete()
                    } else {
                        Log.e(TAG, "_createFirebaseUser: createUserWithEmailAndPassword() completed but was not successful")
                        //AppController.notifyObservers(EVENT_USER_REGISTRATION, false)

                        val r = Outcome.FAILURE(value = "firebase user was not created", reason = it.exception?.message)
                        emitter.onNext(r)
                        emitter.onComplete()
                    }
                }
        }

    }

    private fun uploadUserProfileImage(): Observable<Outcome> {
        return Observable.create { emitter ->
            try {
                val imageByteArray: ByteArray = TaskManager
                    .runOnThreadPool(CompressImageCallable(mUser.image_uri_init!!))

                val storageReference = getUserProfileImageStorageRef(mCurrentFirebaseUserResult.user!!.uid)

                storageReference
                    .putBytes(imageByteArray)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            storageReference.downloadUrl
                                .addOnCompleteListener { it2 ->
                                    if (it2.isSuccessful) {
                                        mUser.imageUri = it2.result.toString()

                                        val r = Outcome.SUCCESS(value = "user profile image url downloaded successfully")
                                        emitter.onNext(r)
                                        emitter.onComplete()
                                    } else {
                                        Log.e(TAG, "_uploadUserProfileImage: query retrieving downloadUrl for image completed but wasnt successful")
                                        //AppController.notifyObservers(EVENT_USER_REGISTRATION, false)

                                        val r = Outcome.FAILURE(value = "user profile image url was not downloaded", reason = it.exception?.message)
                                        emitter.onNext(r)
                                        emitter.onComplete()
                                    }
                                }
                        } else {
                            Log.e(TAG, "_uploadUserProfileImage: query uploading user image completed but wasnt successful")
                            //AppController.notifyObservers(EVENT_USER_REGISTRATION, false)

                            val r = Outcome.FAILURE(value = "user profile image was not uploaded", reason = it.exception?.message)
                            emitter.onNext(r)
                            emitter.onComplete()
                        }
                    }
            } catch (e: Exception) {
                Log.e(TAG, "_uploadUserProfileImage: _uploadUserProfileImage().catch(): error occurred", e)
                //AppController.notifyObservers(EVENT_USER_REGISTRATION, false)

                val r = Outcome.ERROR(value = "an error occurred uploading user profile image", error = e)
                emitter.onNext(r)
                emitter.onComplete()
            }
        }
    }

    private fun createAgoraUser(): Observable<Outcome> {
        return with(IDUtils.generateNewUserID()) {
            mUser.agoraUID = this
            Observable.just(Outcome.SUCCESS(value = "user agoraID generated successfully", additionalInfo = this))
        }
    }

    private fun getFCMRegistrationToken(): Observable<Outcome> {
        return Observable.create { emitter ->
            FirebaseMessaging.getInstance()
                .getToken()
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        mUser.FBRegistrationToken = it.result

                        val r = Outcome.SUCCESS(value = "getting user FCM registration token", additionalInfo = it.result)
                        emitter.onNext(r)
                        emitter.onComplete()
                    } else {
                        Log.e(TAG, "getFCMRegistrationToken: getting user FCM Registration token completed but failed")
                        //AppController.notifyObservers(EVENT_USER_REGISTRATION, false)

                        val r = Outcome.FAILURE(value = "getting user FCM registration token failed", reason = it.exception?.message)
                        emitter.onNext(r)
                        emitter.onComplete()
                    }
                }
        }
    }

    private fun uploadUserDetailsToFirebaseDB(): Observable<Outcome> {
        return Observable.create { emitter ->
            val user: FBUserDetails = parseUserDetails()
            val databaseAddress: String = mCurrentFirebaseUserResult.user!!.uid

            getUserDataForUIDRef(databaseAddress)
                .setValue(user)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        val r = Outcome.SUCCESS(value = "uploading user details to Firebase was successful")
                        emitter.onNext(r)
                        emitter.onComplete()
                    } else {
                        Log.e(TAG, "uploadUserDetailsToFirebaseDB2: uploading user details to multiple locations in the DB, completed but failed")
                        //AppController.notifyObservers(EVENT_USER_REGISTRATION, false)
                        val r = Outcome.FAILURE(value = "uploading user details to Firebase failed", reason = it.exception?.message)
                        emitter.onNext(r)
                        emitter.onComplete()
                    }
                }
        }
    }

    private fun parseUserDetails(): FBUserDetails {
        val user = FBUserDetails(
            mUser.accountType.toString(),
            mUser.firstName!!,
            mUser.lastName!!,
            "${mUser.firstName} ${mUser.lastName}",
            mUser.email,
            mUser.sex.toString(),
            mUser.age,
            mUser.firebaseUID!!,
            mUser.agoraUID!!,
            mUser.FBRegistrationToken!!,
            mUser.imageUri!!,
            null,
            null
        )

        if (mUser.accountType == AccountType.PATIENT)
            user.history = mUser.history
        else
            user.specialization = mUser.specialization

        return user
    }

    private fun sendVerificationEmail(): Observable<Outcome> {
        return Observable.create { emitter ->
            mCurrentFirebaseUserResult.user!!.sendEmailVerification()
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        mFirebaseAuth.signOut()
                        //AppController.notifyObservers(EVENT_USER_REGISTRATION, true)

                        val r = Outcome.SUCCESS(value = "user verification email sent successfully")
                        emitter.onNext(r)
                        emitter.onComplete()
                    } else {
                        Log.e(TAG, "_sendVerificationEmail: sending email verification completed but failed")
                        //AppController.notifyObservers(EVENT_USER_REGISTRATION, false)
                        val r = Outcome.FAILURE(value = "user verification email was not sent", reason = it.exception?.message)
                        emitter.onNext(r)
                        emitter.onComplete()
                    }
                }
        }

    }

    private fun deleteUser(user:TempUserDetails = mUser):Observable<Outcome>{
        if(mFirebaseAuth.currentUser == null)
            return Observable.empty()

        return Observable.create{ emitter ->
            mFirebaseAuth.currentUser!!
                .delete()
                .addOnCompleteListener {
                    if(it.isSuccessful){
                        Log.e(TAG, "deleteUser: delete successful")

                        val r = Outcome.SUCCESS(value = true)
                        emitter.onNext(r)
                        emitter.onComplete()
                    }else{
                        Log.e(TAG, "deleteUser: delete unsuccessful", it.exception)

                        val r = Outcome.FAILURE(value = false, reason = it.exception?.message)
                        emitter.onNext(r)
                        emitter.onComplete()
                    }
                }
        }

    }

    private fun deleteUserProfileImage():Observable<Outcome>{
        if(mCurrentFirebaseUserResult?.user?.uid == null)
            return Observable.empty()

        return Observable.create { emitter ->
            val storageReference = getUserProfileImageStorageRef(mCurrentFirebaseUserResult.user!!.uid)
            storageReference
                .delete()
                .addOnCompleteListener {
                    if(it.isSuccessful){
                        Log.e(TAG, "deleteUserProfileImage: delete successful")

                        val r = Outcome.SUCCESS(value = true)
                        emitter.onNext(r)
                        emitter.onComplete()
                    }else{
                        Log.e(TAG, "deleteUserProfileImage: delete unsuccessful", it.exception )

                        val r = Outcome.FAILURE(value = false, reason = it.exception?.message)
                        emitter.onNext(r)
                        emitter.onComplete()
                    }
                }
        }

    }


    private fun deleteUserDetailsFromDB():Observable<Outcome>{
        if(mCurrentFirebaseUserResult?.user?.uid == null)
            return Observable.empty()

        return Observable.create { emitter ->
            val databaseAddress: String = mCurrentFirebaseUserResult.user!!.uid
            getUserDataForUIDRef(databaseAddress)
                .setValue(null)
                .addOnCompleteListener {
                    if(it.isSuccessful){
                        Log.e(TAG, "deleteUserDetailsFromDB: delete successful" )

                        val r = Outcome.SUCCESS(value = true)
                        emitter.onNext(r)
                        emitter.onComplete()
                    }else{
                        Log.e(TAG, "deleteUserDetailsFromDB: delete unsuccessful")

                        val r = Outcome.FAILURE(value = false, reason = it.exception?.message)
                        emitter.onNext(r)
                        emitter.onComplete()
                    }
                }
        }

    }

    fun handleForgotPassword(email: String): Observable<Boolean> {
        return Observable.create<Boolean> { emitter ->
            mFirebaseAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        Log.e(TAG, "handleForgotPassword: password reset email successfully sent")
                        emitter.onNext(true)
                        emitter.onComplete()
                    } else {
                        Log.e(TAG, "handleForgotPassword: password reset email was not successfully sent", it.exception)
                        emitter.onNext(false)
                        emitter.onComplete()
                    }
                }
        }
    }
}

