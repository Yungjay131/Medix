package app.slyworks.auth_lib

import app.slyworks.constants_lib.KEY_FCM_REGISTRATION
import app.slyworks.constants_lib.KEY_IS_THERE_NEW_FCM_REG_TOKEN
import app.slyworks.constants_lib.KEY_LAST_SIGN_IN_TIME
import app.slyworks.constants_lib.KEY_LOGGED_IN_STATUS
import app.slyworks.crypto_lib.CryptoHelper
import app.slyworks.data_lib.DataManager
import app.slyworks.data_lib.models.FBUserDetailsVModel
import app.slyworks.firebase_commons_lib.FirebaseUtils
import app.slyworks.models_commons_lib.models.Outcome
import app.slyworks.utils_lib.PreferenceManager
import app.slyworks.utils_lib.TimeHelper
import app.slyworks.utils_lib.utils.onNextAndComplete
import com.google.firebase.auth.*
import io.reactivex.rxjava3.core.Observable
import timber.log.Timber


/**
 *Created by Joshua Sylvanus, 12:11 PM, 12/10/2021.
 */
class LoginManager(
    private val preferenceManager: PreferenceManager,
    private val firebaseAuth:FirebaseAuth,
    private val usersManager: UsersManager,
    private val firebaseUtils: FirebaseUtils,
    private val timeHelper: TimeHelper,
    private val cryptoHelper: CryptoHelper,
    private val dataManager: DataManager) {

    //region Vars
    private var loggedInStatus:Boolean = false
    private var authStateListener:FirebaseAuth.AuthStateListener
    //endregion

    init {
        authStateListener = FirebaseAuth.AuthStateListener { p0 ->
            loggedInStatus = p0.currentUser == null
            preferenceManager.set(KEY_LOGGED_IN_STATUS, loggedInStatus)
        }
        firebaseAuth.addAuthStateListener(authStateListener)
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

                    emitter.onNextAndComplete(r)
                }
        }
    }

    fun getLoginStatus():Boolean = loggedInStatus

    fun getLoginStatus2():Boolean {
        return preferenceManager.get(KEY_LOGGED_IN_STATUS, false)!!  &&
                with(preferenceManager.get(KEY_LAST_SIGN_IN_TIME, System.currentTimeMillis())){
                    timeHelper.isWithin3DayPeriod(this!!)
                }
    }

    /* TODO: retrieve encryption details from FB first*/
    fun loginUser(email:String, password:String):Observable<Outcome> =
          cryptoHelper.hashAsync(password)
              .concatMap { signInUser(email, it) }
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
                    val r:Outcome
                    if (it.isSuccessful) {
                        if (firebaseAuth.currentUser!!.isEmailVerified) {
                            preferenceManager.set(KEY_LAST_SIGN_IN_TIME, System.currentTimeMillis())
                            r = Outcome.SUCCESS(value = "sign in was successful")
                        } else
                            r = Outcome.FAILURE(value = "please verify your email before you can login")
                    } else
                        r = Outcome.FAILURE(value = "login attempt was not successful, please try again")

                    emitter.onNextAndComplete(r)
                }
        }

    private fun uploadFCMRegistrationToken():Observable<Outcome> =
        /* upload the FCMRegistration token specific to  this phone, since
        * its not based on FirebaseUID, but on device */
        Observable.create { emitter ->
           val isThereNewToken = preferenceManager.get(KEY_IS_THERE_NEW_FCM_REG_TOKEN, false)!!
           if(!isThereNewToken)
               emitter.onNextAndComplete(Outcome.SUCCESS(value = "no token to upload"))

           val fcmToken:String? = preferenceManager.get(KEY_FCM_REGISTRATION)!!
            /*fcmToken?.equals(null)?.not() ?: let{
                emitter.onNextAndComplete(Outcome.SUCCESS(value = "no token to upload"))
            }*/

            firebaseUtils.getUserDataRef(dataManager.getUserDetailsParam<String>("firebaseUID")!!)
                .setValue(fcmToken)
                .addOnCompleteListener {
                    if(it.isSuccessful) {
                        preferenceManager.set(KEY_IS_THERE_NEW_FCM_REG_TOKEN, false)
                        emitter.onNextAndComplete(Outcome.SUCCESS(value = "token uploaded successfully"))
                    }else
                        emitter.onNextAndComplete(Outcome.FAILURE(value = "token was not successfully uploaded", reason = it.exception?.message))
                }
        }

    private fun retrieveUserDetails():Observable<Outcome> =
        Observable.create { emitter ->
           firebaseUtils.getUserDataForUIDRef(firebaseAuth.currentUser!!.uid)
               .get()
               .addOnCompleteListener {
                   if(it.isSuccessful){
                       val user = it.result!!.getValue(FBUserDetailsVModel::class.java)
                           dataManager.saveUserToDataStore(user!!)
                               .subscribe()

                       val r:Outcome = Outcome.SUCCESS(value = "login successful")
                       emitter.onNextAndComplete(r)
                   }else{
                       Timber.e("signInUser: user login failed",it.exception )
                       val r:Outcome = Outcome.FAILURE(value = "oops something went wrong on our end, please try again")
                       emitter.onNextAndComplete(r)
                   }
               }
         }

    fun logoutUser(){
            firebaseAuth.signOut()
            usersManager.clearUserDetails()
            dataManager.clearUserDetailsFromDataStore().subscribe()
            preferenceManager.clearPreference(KEY_LAST_SIGN_IN_TIME)
    }

    fun onDestroy(){
        firebaseAuth.removeAuthStateListener(authStateListener)
    }
}