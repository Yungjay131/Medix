package app.slyworks.auth_lib

import android.app.Activity
import app.slyworks.data_lib.CryptoHelper
import app.slyworks.firebase_commons_lib.FirebaseUtils
import app.slyworks.utils_lib.Outcome
import app.slyworks.utils_lib.utils.plusAssign
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleEmitter
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.PublishSubject
import timber.log.Timber
import java.util.concurrent.TimeUnit


/**
 * Created by Joshua Sylvanus, 6:07 AM, 19-Dec-2022.
 */
class VerificationHelper(
    private val authStateListener: MAuthStateListener,
    private val firebaseAuth: FirebaseAuth,
    private val firebaseUtils:FirebaseUtils,
    private val cryptoHelper: CryptoHelper
) {

    //region Vars
    private lateinit var phoneNumber:String

    private lateinit var verificationID:String
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken


    private val internalSubject: PublishSubject<Outcome> = PublishSubject.create()
    private val otpResultSubject: PublishSubject<Outcome> = PublishSubject.create()
    val otpSubject: PublishSubject<String> = PublishSubject.create()
    val resendSubject: PublishSubject<String> = PublishSubject.create()

    private var otpActivity:Activity? = null

    private val disposables:CompositeDisposable = CompositeDisposable()
    //endregion

     private fun signInTemporarily(email:String, password: String): Single<Outcome> =
        Single.create { emitter ->
            firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    if(it.isSuccessful)
                        emitter.onSuccess(Outcome.SUCCESS(Unit))
                    else
                        emitter.onSuccess(Outcome.FAILURE(Unit, it.exception?.message))
                }
        }


    /* temporarily sign in before calling this method */
    fun verifyBySendingEmail():Single<Outcome> =
        Single.create{ emitter:SingleEmitter<Outcome> ->
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


     fun verifyViaOTP(phoneNumber:String, a:Activity):Observable<Outcome>{
         this.phoneNumber = phoneNumber
         otpActivity = a

         disposables +=
         internalSubject.subscribe {
             when{
                 it.isSuccess -> verifyOTPFinalStep(it.getTypedValue())
                    //it.isFailure -> otpResultSubject.onNext(it)
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
                var message:String = "something went wrong verifying OTP"
                when (p0) {
                    is FirebaseAuthInvalidCredentialsException ->
                        message = "invalid OTP, please check and try again"
                    is FirebaseTooManyRequestsException ->
                        message = "something went wrong on our end. Please try again"
                }
                otpResultSubject.onNext(Outcome.SUCCESS(value = OTPVerificationStage.VERIFICATION_FAILURE, message))
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
            .setActivity(otpActivity!!)
            .setPhoneNumber(phoneNumber)
            .setTimeout(90 * 1_000L, TimeUnit.MILLISECONDS)
            .setCallbacks(phoneAuthCallback)
            .apply {
                resendToken?.let { setForceResendingToken(it) }
            }
            .build()

    private fun verifyOTPFinalStep(credential: PhoneAuthCredential){
        disposables +=
        signInWithCredentialFromOTP(credential)
           .concatMap {
               when {
                   it.isSuccess -> deleteUserAddedWithPhoneNumberFromOTP()
                   else -> Single.just(it)
               }
           }.concatMap {
               when{
                   it.isSuccess -> signInUserAfterOTP()
                   else -> Single.just(it)
               }
           }.concatMap {
               when{
                   it.isSuccess -> updateUserVerificationStatusAfterOTP()
                   else -> Single.just(it)
               }
           }
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe { result:Outcome ->
                val o: Outcome
                if(result.isSuccess) {
                    o = Outcome.SUCCESS(value = OTPVerificationStage.VERIFICATION_SUCCESS)
                }else {
                    o = Outcome.SUCCESS(
                        value = OTPVerificationStage.VERIFICATION_FAILURE,
                        additionalInfo = result.getAdditionalInfo() ?: "verification failed" )
                }

                otpResultSubject.onNext(o)
            }
    }

    private fun signInWithCredentialFromOTP(credential: PhoneAuthCredential):Single<Outcome> =
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

    private fun deleteUserAddedWithPhoneNumberFromOTP():Single<Outcome> =
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

    private fun signInUserAfterOTP():Single<Outcome> =
        Single.create { emitter ->
            val (email:String?,password:String?) = authStateListener.getEmailAndPassword()
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

    private fun updateUserVerificationStatusAfterOTP():Single<Outcome> =
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


}