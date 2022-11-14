package com.slyworks.auth

import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.slyworks.constants.KEY_FCM_REGISTRATION
import com.slyworks.constants.KEY_LAST_SIGN_IN_TIME
import com.slyworks.constants.KEY_LOGGED_IN_STATUS
import com.slyworks.firebase_commons.FirebaseUtils
import com.slyworks.models.models.Outcome
import com.slyworks.models.room_models.FBUserDetails
import com.slyworks.userdetails.UserDetailsUtils
import com.slyworks.utils.PreferenceManager
import com.slyworks.utils.TimeUtils
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit


/**
 *Created by Joshua Sylvanus, 12:11 PM, 12/10/2021.
 */
class LoginManager(
    private val preferenceManager: PreferenceManager,
    private val firebaseAuth:FirebaseAuth,
    private val usersManager:UsersManager,
    private val userDetailsUtils: UserDetailsUtils,
    private val firebaseUtils: FirebaseUtils,
    private val timeUtils: TimeUtils) {

    //region Vars
    private var mLoggedInStatus:Boolean = false
    private var mAuthStateListener:FirebaseAuth.AuthStateListener
    //endregion

    init {
        mAuthStateListener = FirebaseAuth.AuthStateListener { p0 ->
            mLoggedInStatus = p0.currentUser == null
            preferenceManager.set(KEY_LOGGED_IN_STATUS, mLoggedInStatus)
        }
        firebaseAuth.addAuthStateListener(mAuthStateListener)
    }

    fun handleForgotPassword(email: String): Observable<Boolean> {
        return Observable.create<Boolean> { emitter ->
            firebaseAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener {
                    val r:Boolean

                    if (it.isSuccessful) {
                        Timber.e("handleForgotPassword: password reset email successfully sent")
                        r = true
                    } else {
                        Timber.e("handleForgotPassword: password reset email was not successfully sent", it.exception)
                        r = false
                    }

                    emitter.onNext(r)
                    emitter.onComplete()
                }
        }
    }

    fun getLoginStatus():Boolean = mLoggedInStatus

    fun getLoginStatus2():Boolean {
        return preferenceManager.get(KEY_LOGGED_IN_STATUS, false)!!  &&
                with(preferenceManager.get(KEY_LAST_SIGN_IN_TIME, System.currentTimeMillis())){
                    timeUtils.isWithin3DayPeriod(this!!)
                }
    }

    fun loginUser(email:String, password:String):Observable<Outcome> =
          signInUser(email, password)
              .concatMap {
                  if(it.isSuccess)
                      uploadFCMRegistrationToken()
                  else
                      Observable.just(it)
              }
              .concatMap {
                  if(it.isSuccess)
                      retrieveUserDetails()
                  else
                      Observable.just(it)
              }


    private fun signInUser(email:String, password: String):Observable<Outcome> =
        Observable.create { emitter ->
            firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        if (firebaseAuth.currentUser!!.isEmailVerified) {
                            preferenceManager.set(KEY_LAST_SIGN_IN_TIME, System.currentTimeMillis())
                            val r:Outcome = Outcome.SUCCESS(value = "sign in was successful")
                            emitter.onNext(r)
                            emitter.onComplete()
                        } else {
                            val r: Outcome = Outcome.FAILURE(value = "please verify your email before you can login")
                            emitter.onNext(r)
                            emitter.onComplete()
                        }
                    } else {
                        val r:Outcome = Outcome.FAILURE(value = "login attempt was not successful, please try again")
                        emitter.onNext(r)
                        emitter.onComplete()
                    }
                }
        }

    private fun uploadFCMRegistrationToken():Observable<Outcome> =
        /* upload the FCMRegistration token specific to  this phone, since
        * its not based on FirebaseUID, but on device */
        Observable.create { emitter ->
           val fcmToken:String? = preferenceManager.get(KEY_FCM_REGISTRATION)
            fcmToken?.equals(null)?.not() ?: let{
                emitter.onNext(Outcome.SUCCESS(value = "no token to upload"))
                emitter.onComplete()
                return@create
            }

            firebaseUtils.getUserDataRef(userDetailsUtils.user!!.firebaseUID)
                .setValue(fcmToken)
                .addOnCompleteListener {
                    if(it.isSuccessful){
                        emitter.onNext(Outcome.SUCCESS(value = "token uploaded successfully"))
                        emitter.onComplete()
                    }else{
                        emitter.onNext(Outcome.FAILURE(value = "token was not successfully uploaded", reason = it.exception?.message))
                        emitter.onComplete()
                    }
                }
        }

    private fun retrieveUserDetails():Observable<Outcome> =
        Observable.create { emitter ->
           firebaseUtils.getUserDataForUIDRef(firebaseAuth.currentUser!!.uid)
               .get()
               .addOnCompleteListener {
                   if(it.isSuccessful){
                       val user = it.result!!.getValue(FBUserDetails::class.java)
                       CoroutineScope(Dispatchers.IO).launch {
                           userDetailsUtils.user = user
                           usersManager.saveUserToDataStore(user!!)
                           emitter.onComplete()
                       }

                       val r:Outcome = Outcome.SUCCESS(value = "login successful")
                       emitter.onNext(r)
                       emitter.onComplete()
                   }else{
                       Timber.e("signInUser: user login failed",it.exception )
                       val r:Outcome = Outcome.FAILURE(value = "oops something went wrong on our end, please try again")
                       emitter.onNext(r)
                       emitter.onComplete()
                   }
               }
         }

    fun logoutUser(){
            firebaseAuth.signOut()
            usersManager.clearUserDetails()
            userDetailsUtils.clearUserData()
            preferenceManager.clearPreference(KEY_LAST_SIGN_IN_TIME)
    }

    fun onDestroy(){
        firebaseAuth.removeAuthStateListener(mAuthStateListener)
    }
}