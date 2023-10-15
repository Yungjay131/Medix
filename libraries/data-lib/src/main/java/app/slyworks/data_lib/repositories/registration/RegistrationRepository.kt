package app.slyworks.data_lib.repositories.registration

import android.app.Activity
import app.slyworks.data_lib.model.view_entities.OTPVerificationStage
import app.slyworks.data_lib.firebase.FirebaseUtils
import app.slyworks.data_lib.helpers.auth.MAuthStateListener
import app.slyworks.data_lib.model.models.TempUserDetails
import app.slyworks.data_lib.model.view_entities.FBUserDetailsVModel
import app.slyworks.utils_lib.IDHelper
import app.slyworks.utils_lib.ImageProcessor
import app.slyworks.utils_lib.Outcome
import app.slyworks.utils_lib.utils.plusAssign
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.StorageReference
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.PublishSubject
import timber.log.Timber
import java.util.concurrent.TimeUnit


/**
 * Created by Joshua Sylvanus, 7:09 AM, 25-Sep-2023.
 */
internal class RegistrationRepository(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseMessaging: FirebaseMessaging,
    private val firebaseUtils: FirebaseUtils,
    private val imageProcessor: ImageProcessor,
    private val idHelper: IDHelper,
    private val authStateListener: MAuthStateListener
) : IRegistrationRepository {
    private lateinit var phoneNumber:String

    private lateinit var verificationID:String

    private lateinit var resendToken:PhoneAuthProvider.ForceResendingToken

    private lateinit var user: TempUserDetails
    private var currentFirebaseUserResult: AuthResult? = null

    private val otpInputSubject: PublishSubject<String> = PublishSubject.create()
    private val resendOTPSubject: PublishSubject<Boolean> = PublishSubject.create()
    private val otpResultSubject: PublishSubject<Outcome> = PublishSubject.create()
    private val disposables: CompositeDisposable = CompositeDisposable()

    private val phoneAuthCallback: PhoneAuthProvider.OnVerificationStateChangedCallbacks =
        object : PhoneAuthProvider.OnVerificationStateChangedCallbacks(){
            override fun onCodeAutoRetrievalTimeOut(p0: String) {
                super.onCodeAutoRetrievalTimeOut(p0)
            }

            override fun onCodeSent(p0: String, p1: PhoneAuthProvider.ForceResendingToken) {
                /* we would need the verificationID for verification and the resendToken
                * if we need to send another OTP to same number */
                verificationID = p0
                resendToken = p1

                /* notify listener that it can transition to "enter OTP" screen*/
                otpResultSubject.onNext(Outcome.SUCCESS(value = OTPVerificationStage.ENTER_OTP))
            }

            override fun onVerificationCompleted(p0: PhoneAuthCredential) {
                /* perform extra tasks needed to convert phone number signup to
                * email and password signup */
                verifyOTPFinalStep(p0)

                /* notify listener to transition to a "loading" state*/
                otpResultSubject.onNext(Outcome.SUCCESS(OTPVerificationStage.PROCESSING))
            }

            override fun onVerificationFailed(p0: FirebaseException) {
                var message:String = "something went wrong verifying OTP"
                when (p0) {
                    is FirebaseAuthInvalidCredentialsException ->
                        message = "invalid OTP, please check and try again"

                    is FirebaseTooManyRequestsException ->
                        message = "something went wrong on our end. Please try again"
                }

                /* notify listeners that OTP entered was incorrect or there was a BE issue */
                otpResultSubject.onNext(Outcome.SUCCESS(OTPVerificationStage.VERIFICATION_FAILURE, message))
            }
        }

    /* temporarily login or ensure user is logged in before calling this method */
    override fun verifyViaEmail(email: String): Single<Outcome> =
        Single.create { emitter ->
            /* for redirecting back to Medix from email */
            /* TODO: implement with deeplinking and applinks */
            val actionCodeSettings:ActionCodeSettings =
                ActionCodeSettings.newBuilder()
                    .setHandleCodeInApp(true)
                    .setUrl("https://www.medix.com/verify_sign_up?=true")
                    .setAndroidPackageName(
                        "app.slyworks.medix",
                        true,
                        "21"
                    )
                    .build()

            authStateListener.getCurrentUser()!!
                .sendEmailVerification()
                .addOnCompleteListener {
                    val o: Outcome
                    if (it.isSuccessful)
                        o = Outcome.SUCCESS(Unit)
                    else {
                        Timber.e(it.exception)
                        o = Outcome.FAILURE(Unit, it.exception?.message ?: "verification email not sent")
                    }

                    emitter.onSuccess(o)
                }

        }

    override fun resendOTP(){
        resendOTPSubject.onNext(true)
    }

    /* method to be called from the ViewModel to enter the OTP
    * input from the user */
    override fun receiveSMSCodeForOTP(smsCode:String){
        /* subscribed to in verifyViaOTP(), so will trigger the next step
        * of OTP verification */
        otpInputSubject.onNext(smsCode)
    }

    override fun verifyViaOTP(phoneNumber: String,
                              activity: Activity): Observable<Outcome> {
        /* for receiving OTP input from the user, called from
        * receiveSMSCodeForOTP() */
        disposables +=
        otpInputSubject
            .observeOn(Schedulers.io())
            .subscribe {smsCode:String ->
                val credential:PhoneAuthCredential =
                    PhoneAuthProvider.getCredential(verificationID, smsCode)
                verifyOTPFinalStep(credential)
            }

        /* trigger a new OTP request, using Boolean to indicate an event
        * value does not matter*/
        disposables +=
        resendOTPSubject
            .observeOn(Schedulers.io())
            .subscribe{_:Boolean ->
                val options: PhoneAuthOptions =
                    PhoneAuthOptions.newBuilder()
                        .setActivity(activity)
                        .setPhoneNumber(phoneNumber)
                        .setTimeout(90 * 1_000L, TimeUnit.MILLISECONDS)
                        .setCallbacks(phoneAuthCallback)
                        .setForceResendingToken(resendToken)
                        .build()
                PhoneAuthProvider.verifyPhoneNumber(options)
            }

        val options: PhoneAuthOptions =
            PhoneAuthOptions.newBuilder()
                .setActivity(activity)
                .setPhoneNumber(phoneNumber)
                .setTimeout(90 * 1_000L, TimeUnit.MILLISECONDS)
                .setCallbacks(phoneAuthCallback)
                .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
        return otpResultSubject.hide()
    }

    private fun verifyOTPFinalStep(credential: PhoneAuthCredential){
        disposables +=
            loginWithCredentialFromOTP(credential)
                .flatMap {
                    when {
                        it.isSuccess -> deletePhoneNumberUserAddedFromOTP()
                        else -> Single.just(it)
                    }
                }.flatMap {
                    when{
                        it.isSuccess -> loginUserAfterOTP()
                        else -> Single.just(it)
                    }
                }.flatMap {
                    when{
                        it.isSuccess -> updateUserVerificationStatusOnFirebaseDBAfterOTP()
                        else -> Single.just(it)
                    }
                }
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe { result:Outcome ->
                    val o: Outcome
                    if(result.isSuccess) {
                        o = Outcome.SUCCESS(OTPVerificationStage.VERIFICATION_SUCCESS)
                    }else {
                        o = Outcome.SUCCESS(
                            OTPVerificationStage.VERIFICATION_FAILURE,
                            result.getAdditionalInfo() ?: "verification failed" )
                    }

                    otpResultSubject.onNext(o)
                }
    }

    /* complete the process by logining in??? */
    private fun loginWithCredentialFromOTP(credential: PhoneAuthCredential):Single<Outcome> =
        Single.create { emitter ->
            firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener {
                    val o:Outcome
                    if(it.isSuccessful){
                        o = Outcome.SUCCESS(Unit)
                    }else{
                        Timber.e(it.exception)
                        o = Outcome.FAILURE(Unit, it.exception?.message)
                    }

                    emitter.onSuccess(o)
                }
        }

    /* we are not interested in the PhoneNumber user on Firebase so delete */
    private fun deletePhoneNumberUserAddedFromOTP():Single<Outcome> =
        Single.create { emitter ->
            if(firebaseAuth.currentUser?.phoneNumber != phoneNumber){
                emitter.onSuccess(Outcome.SUCCESS(Unit, "no user phone number to delete"))
                return@create
            }

            firebaseAuth.currentUser!!.delete()
                .addOnCompleteListener {
                    val o:Outcome
                    if(it.isSuccessful){
                        o = Outcome.SUCCESS(Unit)
                    }else{
                        Timber.e(it.exception)
                        o = Outcome.FAILURE(Unit,it.exception?.message)
                    }

                    emitter.onSuccess(o)
                }
        }

    /* user has been verified, now log them in using the email and password,
    * they provided during sign up or login */
    private fun loginUserAfterOTP():Single<Outcome> =
        Single.create { emitter ->
            val (email:String?, password:String?) = authStateListener.getEmailAndPassword()
            firebaseAuth.signInWithEmailAndPassword(email!!, password!!)
                .addOnCompleteListener {
                    val o:Outcome
                    if (it.isSuccessful){
                        o = Outcome.SUCCESS(Unit)
                    }else{
                        Timber.e(it.exception)
                        o = Outcome.FAILURE(Unit, it.exception?.message)
                    }

                    emitter.onSuccess(o)
                }
        }

    /* set field we use to monitor verifications to true */
    private fun updateUserVerificationStatusOnFirebaseDBAfterOTP():Single<Outcome> =
        Single.create { emitter ->
            firebaseUtils.getUserVerificationStatusRef(firebaseAuth.currentUser!!.uid)
                .setValue(true)
                .addOnCompleteListener {
                    val o:Outcome
                    if(it.isSuccessful){
                        o = Outcome.SUCCESS(Unit)
                    }else{
                        Timber.e(it.exception)
                        o = Outcome.FAILURE(Unit, it.exception?.message)
                    }

                    emitter.onSuccess(o)
                }
        }


    /* if a task fails, try to rollback cause the whole registration process
            is meant to be all or nothing, e.g upload profile pic after registering on Firebase
            if this fails delete the created user on firebase
            */
        override fun registerUser(userDetails: TempUserDetails): Single<Outcome> {
            user = userDetails
            return createFirebaseUser()
                .concatMap {
                    when {
                        it.isSuccess -> uploadUserProfileImageToFirebaseStorage()
                        else -> deleteUser().concatMap { _ -> Single.just(it) }
                    }
                }.concatMap {
                    when {
                        it.isSuccess -> getUserProfileImageUrl()
                        else -> deleteUser().concatMap { _ -> Single.just(it) }
                    }
                }.concatMap { it ->
                    when {
                        it.isSuccess -> createAgoraUser()
                        else -> deleteUserProfileImageFromFirebaseStorage().concatMap { _ -> Single.just(it) }
                    }
                }.concatMap {
                    when {
                        it.isSuccess -> getFCMTokenFromFirebaseMessaging()
                        else -> Single.just(it)
                    }
                }.concatMap {
                    when {
                        it.isSuccess -> uploadUserDetailsToFirebaseDB()
                        else -> deleteUserDetailsFromFirebaseDB().concatMap { _ -> Single.just(it) }
                    }
                }/*.concatMap {
                when {
                    it.isSuccess -> signOutCreatedUser()
                    else -> Single.just(it)
                }
            }*/

        }

        private fun createFirebaseUser(): Single<Outcome> {
            authStateListener.setEmailAndPassword(user.email!!, user.password!!)

            return Single.create { emitter ->
                firebaseAuth.createUserWithEmailAndPassword(user.email!!, user.password!!)
                    .addOnCompleteListener {
                        val o: Outcome
                        if (it.isSuccessful) {
                            currentFirebaseUserResult = it.result!!
                            user.firebaseUID = it.result!!.user!!.uid

                            o = Outcome.SUCCESS(Unit)
                        } else {
                            Timber.e(it.exception)
                            o = Outcome.FAILURE(Unit, it.exception?.message ?: "user was not created")
                        }

                        emitter.onSuccess(o)
                    }
            }
        }

        private fun uploadUserProfileImageToFirebaseStorage(): Single<Outcome> =
            Single.create { emitter ->
                val o: Outcome = imageProcessor.getByteArrayFromUri(user.image_uri_init!!)
                if (!o.isSuccess)
                    emitter.onSuccess(o)

                val imageByteArray: ByteArray = o.getTypedValue()
                firebaseUtils.getUserProfileImageStorageRef(currentFirebaseUserResult!!.user!!.uid)
                    .putBytes(imageByteArray)
                    .addOnCompleteListener {
                        val o: Outcome
                        if (it.isSuccessful) {
                            o = Outcome.SUCCESS(Unit)
                        } else {
                            Timber.e(it.exception)
                            o = Outcome.FAILURE(Unit, it.exception?.message ?: "profile image was not uploaded")
                        }

                        emitter.onSuccess(o)
                    }
            }

        private fun getUserProfileImageUrl(): Single<Outcome> =
            Single.create { emitter ->
                val storageReference: StorageReference =
                    firebaseUtils.getUserProfileImageStorageRef(currentFirebaseUserResult!!.user!!.uid)

                storageReference.downloadUrl
                    .addOnCompleteListener { it ->
                        val o: Outcome
                        if (it.isSuccessful) {
                            user.imageUri = it.result.toString()
                            o = Outcome.SUCCESS(Unit)
                        } else {
                            Timber.e(it.exception)
                            o = Outcome.FAILURE(Unit, it.exception?.message ?: "profile image url was not downloaded")
                        }

                        emitter.onSuccess(o)
                    }
            }

        private fun uploadUserDetailsToFirebaseDB(): Single<Outcome> =
            Single.create { emitter ->
                val user: FBUserDetailsVModel =
                    FBUserDetailsVModel(
                        accountType = user.accountType.toString(),
                        firstName = user.firstName!!,
                        lastName = user.lastName!!,
                        fullName = "${user.firstName} ${user.lastName}",
                        email = user.email!!,
                        sex = user.gender.toString(),
                        age = user.dob!!,
                        firebaseUID = user.firebaseUID!!,
                        agoraUID = user.agoraUID!!,
                        fcm_registration_token = user.FBRegistrationToken!!,
                        imageUri = user.imageUri!!,
                        history = this.user.history,
                        specialization = this.user.specialization)

                val databaseAddress: String = currentFirebaseUserResult!!.user!!.uid

                firebaseUtils.getUserDataForUIDRef(databaseAddress)
                    .setValue(user)
                    .addOnCompleteListener {
                        val r: Outcome

                        if (it.isSuccessful) {
                            r = Outcome.SUCCESS(value = "user details successfully uploaded")
                        } else {
                            Timber.e("uploadUserDetailsToFirebaseDB2: uploading user details to multiple locations in the DB, completed but failed")
                            r = Outcome.FAILURE(
                                value = "uploading user details to Firebase failed",
                                reason = it.exception?.message ?: "uploading user details failed")
                        }

                        emitter.onSuccess(r)
                    }
            }

        private fun createAgoraUser(): Single<Outcome> =
            Single.fromCallable {
                user.agoraUID = idHelper.generateNewUserID()
                Outcome.SUCCESS(Unit, user.agoraUID)
            }

        private fun getFCMTokenFromFirebaseMessaging(): Single<Outcome> {
            return Single.create { emitter ->
                firebaseMessaging.getToken()
                    .addOnCompleteListener {
                        val o: Outcome
                        if (it.isSuccessful) {
                            user.FBRegistrationToken = it.result

                            o = Outcome.SUCCESS(Unit,it.result)
                        } else {
                            Timber.e(it.exception)

                            o = Outcome.FAILURE(Unit, it.exception?.message ?: "getting FCM token failed")
                        }

                        emitter.onSuccess(o)
                    }
            }
        }


        private fun logoutCreatedUserFromFirebaseAuth(): Single<Outcome> =
            Single.fromCallable{
                firebaseAuth.signOut()
                Outcome.SUCCESS(value = "created user logged out successfully")
            }

        private fun deleteUser(user: TempUserDetails = this.user): Single<Outcome> {
            if(firebaseAuth.currentUser == null)
                return Single.just(Outcome.FAILURE(false, "no user currently signed in"))

            return Single.create{ emitter ->
                firebaseAuth.currentUser!!.delete()
                    .addOnCompleteListener {
                        val o: Outcome
                        if(it.isSuccessful){
                            Timber.e("deleteUser: delete successful")
                            o = Outcome.SUCCESS(Unit)
                        }else{
                            Timber.e("deleteUser: delete unsuccessful", it.exception)

                            o = Outcome.FAILURE(false, it.exception?.message ?: "deleting user failed")
                        }

                        emitter.onSuccess(o)
                    }
            }

        }

        private fun deleteUserProfileImageFromFirebaseStorage(): Single<Outcome> {
            if(currentFirebaseUserResult == null || currentFirebaseUserResult!!.user?.uid == null)
                return Single.just(
                    Outcome.FAILURE(false,"no user currently signed in")
                )

            return Single.create { emitter ->
                val storageReference = firebaseUtils.getUserProfileImageStorageRef(currentFirebaseUserResult!!.user!!.uid)
                storageReference.delete()
                    .addOnCompleteListener {
                        val o: Outcome
                        if(it.isSuccessful){
                            Timber.e("deleteUserProfileImage: delete successful")
                            o = Outcome.SUCCESS(true)
                        }else{
                            Timber.e(it.exception )
                            o = Outcome.FAILURE(false, it.exception?.message ?: "deleting profile image failed")
                        }

                        emitter.onSuccess(o)
                    }
            }

        }

        private fun deleteUserDetailsFromFirebaseDB(): Single<Outcome> {
            if(currentFirebaseUserResult?.user?.uid == null)
                return Single.just(
                    Outcome.FAILURE(false,"no user currently signed in")
                )

            return Single.create { emitter ->
                val databaseAddress: String = currentFirebaseUserResult!!.user!!.uid
                firebaseUtils.getUserDataForUIDRef(databaseAddress)
                    .setValue(null)
                    .addOnCompleteListener {
                        val o: Outcome
                        if(it.isSuccessful){
                            Timber.e("deleteUserDetailsFromDB: delete successful" )
                            o = Outcome.SUCCESS(true)
                        }else{
                            Timber.e(it.exception)
                            o = Outcome.FAILURE(false, it.exception?.message ?: "deleting user details unsuccessful")
                        }

                        emitter.onSuccess(o)
                    }
            }

        }
}