package app.slyworks.data_lib.helpers.auth

import app.slyworks.utils_lib.KEY_FCM_REGISTRATION
import app.slyworks.utils_lib.KEY_IS_THERE_NEW_FCM_REG_TOKEN
import app.slyworks.utils_lib.KEY_LAST_SIGN_IN_TIME
import app.slyworks.data_lib.firebase.FirebaseUtils
import app.slyworks.data_lib.vmodels.FBUserDetailsVModel
import app.slyworks.utils_lib.Outcome
import app.slyworks.utils_lib.PreferenceManager
import com.google.firebase.auth.FirebaseAuth
import io.reactivex.rxjava3.core.Single
import timber.log.Timber


/**
 * Created by Joshua Sylvanus, 6:07 PM, 16-Sep-2023.
 */
class LoginHelper(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseUtils: FirebaseUtils,
    private val authStateListener: MAuthStateListener,
    private val preferenceManager: PreferenceManager,
    private val userDetailsStore: IUserDetailsStore): ILoginHelper {

    private var loggedInStatus:Boolean = false

    init {
        firebaseAuth.addAuthStateListener(authStateListener)
    }

    override fun getLoggedInStatus():Boolean = authStateListener.getLoggedInStatus()

    override fun handleForgotPassword(email:String):Single<Outcome> =
        Single.create<Outcome> { emitter ->
            firebaseAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener {
                    val o:Outcome
                    if (it.isSuccessful) {
                        Timber.e("handleForgotPassword: password reset email successfully sent")
                        o = Outcome.SUCCESS(Unit)
                    } else {
                        Timber.e("handleForgotPassword: password reset email was not successfully sent", it.exception)
                        o = Outcome.FAILURE(Unit, it.exception?.message)
                    }

                    emitter.onSuccess(o)
                }
        }

    override fun logoutUser():Single<Outcome> =
        Single.fromCallable {
            firebaseAuth.signOut()
            preferenceManager.clearPreference(KEY_LAST_SIGN_IN_TIME)
            return@fromCallable Outcome.SUCCESS(Unit)
        }.concatMap {
            userDetailsStore.clearUserDetails()
        }

    override fun loginUser(email:String, password:String): Single<Outcome> {
        firebaseAuth.signOut()

        authStateListener.setEmailAndPassword(email,password)

        return loginUserWithFirebaseAuth(email, password)
            .concatMap {
                if (it.isSuccess)
                    checkVerificationStatus()
                else
                    Single.just(it)
            }
            .concatMap {
                return@concatMap when{
                    it.isSuccess -> uploadFCMRegistrationToken()
                    it.isFailure -> Single.just(it)
                    it.isError ->
                        uploadFCMRegistrationToken()
                            .concatMap { it2: Outcome ->
                                /* kind of including the second error(if there is) and the first */
                                Single.just(Outcome.ERROR(it2.getAdditionalInfo(), it.getAdditionalInfo()))
                            }
                    else -> throw IllegalArgumentException("don't know the Outcome type")
                }
            }
            .concatMap {
                return@concatMap when{
                    it.isSuccess -> retrieveUserDetails()
                    it.isFailure -> Single.just(it)
                    it.isError ->
                        /* kind of including the second error and the first and even the previous error */
                        Single.just(Outcome.ERROR(it.getTypedValue<String>(), it.getAdditionalInfo()))

                    else -> throw IllegalArgumentException("don't know the Outcome type")
                }
            }.concatMap {
                return@concatMap userDetailsStore.saveUserDetails(it.getTypedValue<FBUserDetailsVModel>())
            }
    }


    private fun loginUserWithFirebaseAuth(email:String, password: String):Single<Outcome> =
        Single.create { emitter ->
            firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    if (it.isSuccessful)
                        emitter.onSuccess(Outcome.SUCCESS(value = "login attempt successful"))
                    else
                        emitter.onSuccess(Outcome.FAILURE(value = "login attempt was not successful, please try again", it.exception?.message))
                }
        }

    private fun checkVerificationStatus():Single<Outcome> =
        Single.create { emitter ->
            firebaseUtils.getUserVerificationStatusRef(firebaseAuth.currentUser!!.uid)
                .get()
                .addOnCompleteListener {
                    if(!it.isSuccessful){
                        emitter.onSuccess(Outcome.FAILURE("sign in failed", it.exception?.message))
                    }else{
                        var isVerified: Boolean = false
                        if (it.result?.exists() == true)
                            isVerified = it.result!!.getValue(Boolean::class.java)!!

                        if (isVerified || firebaseAuth.currentUser!!.isEmailVerified) {
                            emitter.onSuccess(Outcome.SUCCESS("user is verified"))
                        }else{
                            /* using Outcome.ERROR as special case */
                            emitter.onSuccess(Outcome.ERROR("please verify your account before you can login", "please verify your account before you can login"))
                        }
                    }
                }
        }

    private fun uploadFCMRegistrationToken():Single<Outcome> =
        /* upload the FCMRegistration token specific to this phone, since
        * its not based on FirebaseUID, but on device */
        Single.create { emitter ->
            val isThereNewToken = preferenceManager.get(KEY_IS_THERE_NEW_FCM_REG_TOKEN, false)!!
            if(!isThereNewToken)
                emitter.onSuccess(Outcome.SUCCESS(value = "no token to upload"))

            val fcmToken:String? = preferenceManager.get(KEY_FCM_REGISTRATION)
            if(fcmToken == null) {
                Timber.e("synchronisation issues getting token")
                emitter.onSuccess(Outcome.SUCCESS(value = "synchronisation issues getting token,no token to upload"))
            }

            firebaseUtils.getUserDataRef(firebaseAuth.currentUser!!.uid)
                .setValue(fcmToken)
                .addOnCompleteListener {
                    if(it.isSuccessful) {
                        preferenceManager.set(KEY_IS_THERE_NEW_FCM_REG_TOKEN, false)
                        emitter.onSuccess(Outcome.SUCCESS(value = "token uploaded successfully"))
                    }else
                        emitter.onSuccess(Outcome.FAILURE(value = "token was not successfully uploaded", reason = it.exception?.message))
                }
        }

    private fun retrieveUserDetails():Single<Outcome> =
        Single.create { emitter ->
            firebaseUtils.getUserDataForUIDRef(firebaseAuth.currentUser!!.uid)
                .get()
                .addOnCompleteListener {
                    if(it.isSuccessful){
                        val user:FBUserDetailsVModel = it.result!!.getValue(FBUserDetailsVModel::class.java)!!

                        loggedInStatus = true

                        val r: Outcome = Outcome.SUCCESS(value = user)
                        emitter.onSuccess(r)
                    }else{
                        Timber.e("signInUser: user login failed",it.exception )
                        val r: Outcome = Outcome.FAILURE(value = "oops something went wrong on our end, please try again")
                        emitter.onSuccess(r)
                    }
                }
        }


}