package app.slyworks.auth_lib

import app.slyworks.crypto_lib.CryptoDetails
import app.slyworks.crypto_lib.CryptoHelper
import app.slyworks.data_lib.models.FBUserDetailsVModel
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.messaging.FirebaseMessaging
import app.slyworks.firebase_commons_lib.FirebaseUtils
import app.slyworks.models_commons_lib.models.AccountType
import app.slyworks.models_commons_lib.models.Outcome
import app.slyworks.models_commons_lib.models.TempUserDetails
import app.slyworks.utils_lib.IDHelper
import app.slyworks.utils_lib.TaskManager
import app.slyworks.utils_lib.utils.onNextAndComplete
import app.slyworks.utils_lib.utils.plusAssign
import io.reactivex.rxjava3.core.*
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.subjects.PublishSubject
import timber.log.Timber
import java.util.concurrent.TimeUnit


/**
 * Created by Joshua Sylvanus, 7:36 AM, 1/3/2022.
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

enum class OTPVerificationStage{
    ENTER_OTP, PROCESSING, VERIFICATION_SUCCESS,VERIFICATION_FAILURE
}

enum class VerificationDetails{
    OTP{
        private lateinit var phoneNumber:String
        override fun setDetails(details: String) { phoneNumber = details }
        override fun getDetails(): String = phoneNumber
    },
    EMAIL{
        private lateinit var email:String
        override fun setDetails(details: String) { email = details }
        override fun getDetails(): String = email
    };
    abstract fun getDetails():String
    abstract fun setDetails(details:String)
}

class RegistrationManager(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseMessaging: FirebaseMessaging,
    private val firebaseUtils: FirebaseUtils,
    private val taskManager: TaskManager,
    private val cryptoHelper:CryptoHelper) {

    //region Vars
    private lateinit var user: TempUserDetails
    private lateinit var verificationDetails: VerificationDetails
    private lateinit var currentFirebaseUserResult: AuthResult

    private val disposables:CompositeDisposable = CompositeDisposable()

    private lateinit var verificationID:String
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken

     var isVerifyOTPInProgress:Boolean = false

     private val internalSubject:PublishSubject<Outcome> = PublishSubject.create()
     private val otpResultSubject:PublishSubject<Outcome> = PublishSubject.create()
     val otpSubject:PublishSubject<String> = PublishSubject.create()
     val resendSubject:PublishSubject<String> = PublishSubject.create()
    //endregion

    fun unbind():Unit = disposables.clear()

    fun register(userDetails: TempUserDetails): Observable<Outcome> {
        user = userDetails
        return createFirebaseUser()
            .concatMap {
                when{
                    it.isSuccess -> return@concatMap signOutCreatedUser()
                    else -> return@concatMap Observable.just(it)
                }
            }
            .concatMap {
                when {
                    it.isSuccess -> return@concatMap uploadUserProfileImage()
                    else -> return@concatMap deleteUser().concatMap { _ -> Observable.just(it) }
                }
            }.concatMap {
                when {
                    it.isSuccess -> return@concatMap downloadImageUrl()
                    else -> return@concatMap deleteUser().concatMap { _ -> Observable.just(it) }
                }
            }.concatMap {
                when {
                    it.isSuccess -> return@concatMap createAgoraUser()
                    else -> return@concatMap deleteUserProfileImage().concatMap { _ ->  Observable.just(it)}
                }
            }.concatMap {
                when {
                    it.isSuccess -> return@concatMap getFCMRegistrationToken()
                    else -> return@concatMap Observable.just(it)

                }
            }.concatMap {
                when {
                    it.isSuccess -> return@concatMap uploadUserDetailsToFirebaseDB()
                    else -> return@concatMap deleteUserDetailsFromDB()
                }
            }

    }

    private fun createFirebaseUser(): Observable<Outcome>{
        if(!cryptoHelper.isInitialized())
            return getEncryptionDetails()
                .flatMap {
                    if(it.isFailure)
                      Observable.just(it)
                    else
                      cryptoHelper.initializeAsync(it.getTypedValue<CryptoDetails>())
                          .andThen(cryptoHelper.hashAsync(user.password!!))
                }
                .flatMap { signupOnFB() }

        else
            return cryptoHelper.hashAsync(user.password!!)
                .flatMap { signupOnFB() }
    }

    private fun getEncryptionDetails():Observable<Outcome> =
        Observable.create<Outcome> { emitter ->
            firebaseUtils.getEncryptionDetailsRef()
                .get()
                .addOnCompleteListener {
                    val o:Outcome
                    if(it.isSuccessful)
                       o = Outcome.SUCCESS(value = it.result!!.getValue(CryptoDetails::class.java))
                    else
                       o = Outcome.FAILURE(value = "unable to get configuration details", reason = it.exception?.message)

                    emitter.onNextAndComplete(o)
                }
        }

    private fun signupOnFB():Observable<Outcome> =
        Observable.create { emitter ->
            firebaseAuth.createUserWithEmailAndPassword(user.email!!, user.password!!)
                .addOnCompleteListener {
                    val r:Outcome

                    if (it.isSuccessful) {
                        currentFirebaseUserResult = it.result!!
                        user.firebaseUID = it.result!!.user!!.uid
                        Timber.e("_createFirebaseUser: createUserWithEmailAndPassword() successful")
                        r = Outcome.SUCCESS(value = "firebase user created successfully")
                    } else {
                        Timber.e("_createFirebaseUser: createUserWithEmailAndPassword() completed but was not successful")
                        //AppController.notifyObservers(EVENT_USER_REGISTRATION, false)

                        r = Outcome.FAILURE(value = "firebase user was not created", reason = it.exception?.message)
                    }

                    emitter.onNextAndComplete(r)
                }
        }

    private fun signOutCreatedUser():Observable<Outcome> =
        Observable.fromCallable{
            firebaseAuth.signOut()
            Outcome.SUCCESS(value = "created user logged out successfully")
        }

    private fun uploadUserProfileImage(): Observable<Outcome> {
        return Observable.create { emitter ->
            try {
                val imageByteArray: ByteArray = app.slyworks.utils_lib.TaskManager.runOnThreadPool(app.slyworks.utils_lib.CompressImageCallable(user.image_uri_init!!))

                val storageReference = firebaseUtils.getUserProfileImageStorageRef(currentFirebaseUserResult.user!!.uid)

                storageReference
                    .putBytes(imageByteArray)
                    .addOnCompleteListener {
                        val r:Outcome

                        if (it.isSuccessful) {
                            r = Outcome.SUCCESS(value = "user profile image uploaded successfully")
                        } else {
                            Timber.e("_uploadUserProfileImage: query uploading user image completed but wasn't successful")
                            r = Outcome.FAILURE(value = "user profile image was not uploaded", reason = it.exception?.message)
                        }

                        emitter.onNextAndComplete(r)
                    }
            } catch (e: Exception) {
                Timber.e("_uploadUserProfileImage: _uploadUserProfileImage().catch(): error occurred", e)

                val r = Outcome.ERROR(value = "an error occurred uploading user profile image")
                emitter.onNextAndComplete(r)
            }
        }
    }

    private fun downloadImageUrl():Observable<Outcome> =
        Observable.create { emitter ->
            val storageReference = firebaseUtils.getUserProfileImageStorageRef(currentFirebaseUserResult.user!!.uid)

            storageReference.downloadUrl
                .addOnCompleteListener { it ->
                    val r:Outcome

                    if (it.isSuccessful) {
                        user.imageUri = it.result.toString()
                        r = Outcome.SUCCESS(value = "user profile image url downloaded successfully")
                    } else {
                        Timber.e("_uploadUserProfileImage: query retrieving downloadUrl for image completed but wasn't successful")
                        r = Outcome.FAILURE(value = "user profile image url was not downloaded", reason = it.exception?.message)
                    }

                    emitter.onNextAndComplete(r)
                }
        }

    private fun createAgoraUser(): Observable<Outcome> =
        with(IDHelper.generateNewUserID()) {
            user.agoraUID = this
            Observable.just(Outcome.SUCCESS(value = "user agoraID generated successfully", additionalInfo = this))
        }

    private fun getFCMRegistrationToken(): Observable<Outcome> {
        return Observable.create { emitter ->
            firebaseMessaging
                .getToken()
                .addOnCompleteListener {
                    val r:Outcome

                    if (it.isSuccessful) {
                        user.FBRegistrationToken = it.result

                        r = Outcome.SUCCESS(value = "getting user FCM registration token", additionalInfo = it.result)
                    } else {
                        Timber.e("getFCMRegistrationToken: getting user FCM Registration token completed but failed")
                        //AppController.notifyObservers(EVENT_USER_REGISTRATION, false)

                        r = Outcome.FAILURE(value = "getting user FCM registration token failed", reason = it.exception?.message)
                    }

                    emitter.onNextAndComplete(r)
                }
        }
    }

    private fun uploadUserDetailsToFirebaseDB(): Observable<Outcome> {
        return Observable.create { emitter ->
            val user: FBUserDetailsVModel = parseUserDetails()
            val databaseAddress: String = currentFirebaseUserResult.user!!.uid

            firebaseUtils.getUserDataForUIDRef(databaseAddress)
                .setValue(user)
                .addOnCompleteListener {
                    val r:Outcome

                    if (it.isSuccessful) {
                        r = Outcome.SUCCESS(value = "user details successfully uploaded")
                    } else {
                        Timber.e("uploadUserDetailsToFirebaseDB2: uploading user details to multiple locations in the DB, completed but failed")
                        r = Outcome.FAILURE(value = "uploading user details to Firebase failed", reason = it.exception?.message)
                    }

                    emitter.onNextAndComplete(r)
                }
        }
    }

    private fun parseUserDetails(): FBUserDetailsVModel {
        val user = FBUserDetailsVModel(
            user.accountType.toString(),
            user.firstName!!,
            user.lastName!!,
            "${user.firstName} ${user.lastName}",
            user.email!!,
            user.sex.toString(),
            user.age!!,
            user.firebaseUID!!,
            user.agoraUID!!,
            user.FBRegistrationToken!!,
            user.imageUri!!,
            null,
            null)

        if (this.user.accountType == AccountType.PATIENT)
            user.history = this.user.history
        else
            user.specialization = this.user.specialization

        return user
    }

    private fun deleteUser(user:TempUserDetails = this.user):Observable<Outcome>{
        if(firebaseAuth.currentUser == null)
            return Observable.just(Outcome.FAILURE(value = false, reason = "no user currently signed in"))

        return Observable.create{ emitter ->
            firebaseAuth.currentUser!!
                .delete()
                .addOnCompleteListener {
                    val r:Outcome

                    if(it.isSuccessful){
                        Timber.e("deleteUser: delete successful")

                        r = Outcome.SUCCESS(value = true)
                    }else{
                        Timber.e("deleteUser: delete unsuccessful", it.exception)

                        r = Outcome.FAILURE(value = false, reason = it.exception?.message)
                    }

                    emitter.onNextAndComplete(r)
                }
        }

    }

    private fun deleteUserProfileImage():Observable<Outcome>{
        if(currentFirebaseUserResult.user?.uid == null)
            return Observable.just(Outcome.FAILURE(value = false, reason = "no user currently signed in"))

        return Observable.create { emitter ->
            val storageReference = firebaseUtils.getUserProfileImageStorageRef(currentFirebaseUserResult.user!!.uid)
            storageReference
                .delete()
                .addOnCompleteListener {
                    val r:Outcome

                    if(it.isSuccessful){
                        Timber.e("deleteUserProfileImage: delete successful")

                        r = Outcome.SUCCESS(value = true)

                    }else{
                        Timber.e("deleteUserProfileImage: delete unsuccessful", it.exception )

                        r = Outcome.FAILURE(value = false, reason = it.exception?.message)
                    }

                    emitter.onNextAndComplete(r)
                }
        }

    }

    private fun deleteUserDetailsFromDB():Observable<Outcome>{
        if(currentFirebaseUserResult.user?.uid == null)
            return Observable.just(Outcome.FAILURE(value = false, reason = "no user currently signed in"))

        return Observable.create { emitter ->
            val databaseAddress: String = currentFirebaseUserResult.user!!.uid
            firebaseUtils.getUserDataForUIDRef(databaseAddress)
                .setValue(null)
                .addOnCompleteListener {
                    val r:Outcome

                    if(it.isSuccessful){
                        Timber.e("deleteUserDetailsFromDB: delete successful" )

                        r = Outcome.SUCCESS(value = true)
                    }else{
                        Timber.e("deleteUserDetailsFromDB: delete unsuccessful")

                        r = Outcome.FAILURE(value = false, reason = it.exception?.message)
                    }

                    emitter.onNextAndComplete(r)
                }
        }

    }

    fun verifyDetails(details: VerificationDetails):Observable<Outcome> {
       verificationDetails = details

        if (details == VerificationDetails.EMAIL)
           return verifyBySendingEmail()
        else if(details == VerificationDetails.OTP)
           return verifyViaOTP()

        throw UnsupportedOperationException()
    }

    private fun verifyBySendingEmail(): Observable<Outcome> =
        Observable.create { emitter ->
            currentFirebaseUserResult.user!!.sendEmailVerification()
                .addOnCompleteListener {
                    val r:Outcome

                    if (it.isSuccessful) {
                        //firebaseAuth.signOut()
                        //AppController.notifyObservers(EVENT_USER_REGISTRATION, true)

                        r = Outcome.SUCCESS(value = "user verification email sent successfully")
                    } else {
                        Timber.e("_sendVerificationEmail: sending email verification completed but failed")
                        //AppController.notifyObservers(EVENT_USER_REGISTRATION, false)
                        r = Outcome.FAILURE(value = "user verification email was not sent", reason = it.exception?.message)
                    }

                    emitter.onNextAndComplete(r)
                }
        }

    private fun verifyViaOTP():Observable<Outcome>{
        disposables +=
            internalSubject.subscribe {
                when{
                    it.isSuccess -> verifyOTPFinalStep(it.getTypedValue())
                    it.isFailure -> otpResultSubject.onNext(it)
                }
            }

        disposables +=
            otpSubject.subscribe { smsCode:String ->
                val credential = PhoneAuthProvider.getCredential(verificationID, smsCode)
                verifyOTPFinalStep(credential)
            }

        disposables +=
            resendSubject.subscribe{
                PhoneAuthProvider.verifyPhoneNumber(buildPhoneAuthOptions(resendToken))
            }

        PhoneAuthProvider.verifyPhoneNumber(buildPhoneAuthOptions())
        return otpResultSubject.hide()
    }

    private val phoneAuthCallback:PhoneAuthProvider.OnVerificationStateChangedCallbacks =
        object : PhoneAuthProvider.OnVerificationStateChangedCallbacks(){
            override fun onVerificationCompleted(p0: PhoneAuthCredential) {
                internalSubject.onNext(Outcome.SUCCESS(value = p0))
                otpResultSubject.onNext(Outcome.SUCCESS(value = OTPVerificationStage.PROCESSING))
            }

            override fun onVerificationFailed(p0: FirebaseException) {
                var message = "something went wrong verifying OTP"
                when (p0) {
                    is FirebaseAuthInvalidCredentialsException ->
                        message = "invalid OTP, please check and try again"
                    is FirebaseTooManyRequestsException ->
                        message = "something went wrong on our end. Please try again"
                }
                internalSubject.onNext(Outcome.FAILURE(value = OTPVerificationStage.VERIFICATION_FAILURE, message))
            }

            override fun onCodeSent(p0: String, p1: PhoneAuthProvider.ForceResendingToken) {
                verificationID = p0
                resendToken = p1
                otpResultSubject.onNext(Outcome.SUCCESS(value = OTPVerificationStage.ENTER_OTP))
            }
        }

    private fun buildPhoneAuthOptions(resendToken:PhoneAuthProvider.ForceResendingToken? = null)
            : PhoneAuthOptions =
        PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(verificationDetails.getDetails())
            .setTimeout(60 * 1_000L, TimeUnit.MILLISECONDS)
            .setCallbacks(phoneAuthCallback)
            .apply {
                resendToken?.let { setForceResendingToken(it) }
            }
            .build()

    private fun verifyOTPFinalStep(credential: PhoneAuthCredential){
        firebaseAuth.signInWithCredential(credential)
            .continueWithTask {
                if(it.isSuccessful)
                    firebaseUtils.getUserVerificationStatusRef(currentFirebaseUserResult.user!!.uid)
                        .setValue(true)
                else
                    it
            }
            .continueWith {
                val o:Outcome
                if(it.isSuccessful) {
                    //firebaseAuth.signOut()
                    o = Outcome.SUCCESS(value = OTPVerificationStage.VERIFICATION_SUCCESS)
                }else
                    o = Outcome.FAILURE(value = OTPVerificationStage.VERIFICATION_FAILURE, reason = it.exception?.message)

                otpResultSubject.onNext(o)
            }

    }


}

