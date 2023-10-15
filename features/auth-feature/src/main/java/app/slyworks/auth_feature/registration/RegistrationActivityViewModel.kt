package app.slyworks.auth_feature.registration

import android.app.Activity
import android.net.Uri
import android.os.CountDownTimer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import app.slyworks.data_lib.model.view_entities.OTPVerificationStage
import app.slyworks.base_feature.BaseViewModel
import app.slyworks.base_feature.network_register.INetworkRegister
import app.slyworks.data_lib.model.models.TempUserDetails
import app.slyworks.data_lib.model.models.AccountType
import app.slyworks.data_lib.model.models.Gender
import app.slyworks.data_lib.repositories.registration.IRegistrationRepository
import app.slyworks.utils_lib.NO_NETWORK_CONNECTION_PROMPT
import app.slyworks.utils_lib.utils.plusAssign
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject


sealed class RegistrationUIState {
    /* state to represent do nothing */
    object Default : RegistrationUIState()

    data class LoadingStarted(val loadingPrompt:String) : RegistrationUIState()
    object LoadingStopped : RegistrationUIState()

    data class ProfileImageUri(val uri:Uri) : RegistrationUIState()

    object RegistrationSuccess : RegistrationUIState()

    data class EmailVerificationSuccess(val email:String) : RegistrationUIState()
    data class EmailVerificationFailure(val error:String) : RegistrationUIState()

    /* using a different LiveData object(_uiOTPStateLD) for OTPCountDown for this 2 states */
    data class OTPCountDown(val count: Long) : RegistrationUIState()
    object OTPCountDownFinished : RegistrationUIState()

    object OTPVerificationStarted : RegistrationUIState()
    object OTPVerificationResent : RegistrationUIState()
    object OTPVerificationSuccess : RegistrationUIState()
    data class OTPVerificationFailure(val error:String) : RegistrationUIState()

    data class Message(val message:String) : RegistrationUIState()
}

class RegistrationActivityViewModel
    @Inject
    constructor(override val networkRegister: INetworkRegister,
                private val repository: IRegistrationRepository) :  BaseViewModel() {
    //region Vars
    private val registrationDetails: TempUserDetails = TempUserDetails()

    private var currentTimer:CountDownTimer? = null

    private val _uiOTPStateLD: MutableLiveData<RegistrationUIState> = MutableLiveData()
    val uiOTPStateLD: LiveData<RegistrationUIState> = _uiOTPStateLD

    private val _uiStateLD:MutableLiveData<RegistrationUIState> = MutableLiveData()
    val uiStateLD: LiveData<RegistrationUIState> = _uiStateLD

    override val disposables:CompositeDisposable = CompositeDisposable()
    //endregion

    /* for doctor users*/
    fun setSpecialization(list:List<String>){
        registrationDetails.specialization = list
    }

    /* for patient users */
    fun setHealthHistory(list:List<String>){
        registrationDetails.history = list
    }

    fun setNameDOBAndSex(firstName:String,
                         lastName:String,
                         dob:String,
                         sex: Gender
    ){
        registrationDetails.firstName = firstName
        registrationDetails.lastName = lastName
        registrationDetails.dob = dob
        registrationDetails.gender = sex
    }

    fun setEmailAndPassword(email: String, password: String) {
        registrationDetails.email = email
        registrationDetails.password = password
    }

    fun setAccountType(accountType: AccountType){
        registrationDetails.accountType  = accountType
    }

    fun getAccountType(): AccountType = registrationDetails.accountType!!

    fun registerUser(){
       if(!networkRegister.getNetworkStatus()){
           _uiStateLD.setValue(RegistrationUIState.Message(NO_NETWORK_CONNECTION_PROMPT))
           return
       }

       disposables +=
       repository.registerUser(registrationDetails)
           .doOnSubscribe {
               _uiStateLD.postValue(
                   RegistrationUIState.LoadingStarted("signing you up...")
               )
           }
           .subscribeOn(Schedulers.io())
           .observeOn(Schedulers.io())
           .subscribe({
               _uiStateLD.postValue(RegistrationUIState.LoadingStopped)

               when{
                   it.isSuccess ->
                       _uiStateLD.postValue(RegistrationUIState.RegistrationSuccess)
                   it.isFailure ->
                       _uiStateLD.postValue(RegistrationUIState.Message(it.getAdditionalInfo()!!))
               }
           },
            {
               Timber.e("error occurred:", it)
               _uiStateLD.postValue(RegistrationUIState.LoadingStopped)
               _uiStateLD.postValue(RegistrationUIState.Message("an error occurred"))
           })
    }

    fun verifyByEmail(){
        if(!networkRegister.getNetworkStatus()){
            _uiStateLD.setValue(RegistrationUIState.Message(NO_NETWORK_CONNECTION_PROMPT))
            return
        }

        disposables +=
        repository.verifyViaEmail(registrationDetails.email!!)
            .doOnSubscribe {
                _uiStateLD.postValue(
                    RegistrationUIState.LoadingStarted("sending verification email...")
                )
            }
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe({
                _uiStateLD.postValue(RegistrationUIState.LoadingStopped)

                when {
                    it.isSuccess ->
                        _uiStateLD.postValue(
                            RegistrationUIState.EmailVerificationSuccess(registrationDetails.email!!)
                        )
                    it.isFailure ->
                        _uiStateLD.postValue(
                            RegistrationUIState.EmailVerificationFailure(it.getAdditionalInfo()!!)
                        )
                }
            },
                {
                    Timber.e("error occurred:", it)
                    _uiStateLD.postValue(RegistrationUIState.LoadingStopped)
                    _uiStateLD.postValue(RegistrationUIState.Message("an error occurred"))
             })

    }

    fun resendOTP():Unit = repository.resendOTP()

    /* send the entered OTP to the repo */
    fun receiveSMSCodeForOTP(smsCode:String):Unit = repository.receiveSMSCodeForOTP(smsCode)

    /* start the OTP verification process */
    fun verifyViaOTP(phoneNumber:String, a:Activity){
        if(!networkRegister.getNetworkStatus()){
            _uiStateLD.setValue(RegistrationUIState.Message(NO_NETWORK_CONNECTION_PROMPT))
            return
        }

        disposables +=
        repository.verifyViaOTP(phoneNumber, a)
            .doOnSubscribe {
                _uiStateLD.postValue(
                    RegistrationUIState.LoadingStarted("processing OTP...")
                )
            }
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe({
                _uiStateLD.postValue(RegistrationUIState.LoadingStopped)

                when{
                    it.isSuccess -> {
                        when (it.getTypedValue<OTPVerificationStage>()) {
                            OTPVerificationStage.ENTER_OTP ->
                                _uiStateLD.postValue(RegistrationUIState.OTPVerificationStarted)

                            OTPVerificationStage.PROCESSING -> {}

                            OTPVerificationStage.OTP_RESENT ->
                                _uiStateLD.postValue(RegistrationUIState.OTPVerificationResent)

                            OTPVerificationStage.VERIFICATION_SUCCESS ->
                                _uiStateLD.postValue(RegistrationUIState.OTPVerificationSuccess)

                            OTPVerificationStage.VERIFICATION_FAILURE ->
                                _uiStateLD.postValue(RegistrationUIState.OTPVerificationFailure(it.getAdditionalInfo()!!))
                        }
                    }

                    }
                },{
                    Timber.e("error occurred:", it)
                    _uiStateLD.postValue(RegistrationUIState.LoadingStopped)
                    _uiStateLD.postValue(RegistrationUIState.Message("an error occurred"))
                })
    }

    fun initOTPTimeoutCountdown(){
        currentTimer = OTPTimeoutCountDownTimer().start()
    }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
        currentTimer?.cancel()
        currentTimer = null
    }

    inner class OTPTimeoutCountDownTimer : CountDownTimer(90_000, 1_000){
        private var currentCountDownValue:Long = 90

        override fun onTick(p0: Long) {
            _uiOTPStateLD.postValue(
                RegistrationUIState.OTPCountDown(--currentCountDownValue)
            )
        }

        override fun onFinish() {
           _uiOTPStateLD.postValue(RegistrationUIState.OTPCountDownFinished)
        }
    }
}
