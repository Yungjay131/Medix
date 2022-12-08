package app.slyworks.auth_feature.registration

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import app.slyworks.auth_lib.RegistrationManager
import app.slyworks.auth_lib.VerificationDetails
import app.slyworks.base_feature.PermissionManager
import app.slyworks.models_commons_lib.models.TempUserDetails
import app.slyworks.network_lib.NetworkRegister
import app.slyworks.utils_lib.PreferenceManager
import app.slyworks.utils_lib.utils.plusAssign
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.PublishSubject
import timber.log.Timber
import javax.inject.Inject

class RegistrationActivityViewModel
    @Inject
    constructor(private val networkRegister: NetworkRegister,
                private val registrationManager: RegistrationManager,
                private val preferenceManager: PreferenceManager) : ViewModel() {

    //region Vars
    val registrationDetails:TempUserDetails = TempUserDetails()

    private var disposable:Disposable = Disposable.empty()
    private val disposables:CompositeDisposable = CompositeDisposable()

    lateinit var inputOTPSubject:PublishSubject<String>
    lateinit var resendOTPSubject:PublishSubject<Boolean>

    private val networkStatusLiveData: MutableLiveData<Boolean> = MutableLiveData()
    val profileImageUriLiveData:MutableLiveData<Uri> = MutableLiveData()
    val progressLiveData:MutableLiveData<Boolean> = MutableLiveData()
    val messageLiveData:MutableLiveData<String> = MutableLiveData()
    val loginSuccessfulLiveData:MutableLiveData<Boolean> = MutableLiveData()
    val beginOTPVerificationLiveData:MutableLiveData<Boolean> = MutableLiveData()
    val verificationSuccessfulLiveData:MutableLiveData<Boolean> = MutableLiveData()
    //endregion

    fun subscribeToNetwork(): LiveData<Boolean> {
        disposable = networkRegister
            .subscribeToNetworkUpdates()
            .observeOn(Schedulers.io())
            .subscribeOn(Schedulers.io())
            .subscribe {
                networkStatusLiveData.postValue(it)
            }

        return networkStatusLiveData
    }

    fun unsubscribeToNetwork(){
        networkRegister.unsubscribeToNetworkUpdates()
        disposable.dispose()
    }

    fun handleProfileImageUri(o: Observable<Uri>){
        disposables +=
        o.subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe {
                registrationDetails.image_uri_init = it
                it.let{ profileImageUriLiveData.postValue(it) }
            }
    }

    fun registerUser(){
       disposables +=
       registrationManager.register(registrationDetails)
           .subscribeOn(Schedulers.io())
           .observeOn(Schedulers.io())
           .subscribe({
               progressLiveData.postValue(false)

               when{
                   it.isSuccess -> loginSuccessfulLiveData.postValue(true)
                   it.isFailure -> messageLiveData.postValue(it.getAdditionalInfo())
               }
           },{
               Timber.e("error occurred:${it.message}")
               progressLiveData.postValue(false)
               messageLiveData.postValue(it.message)
           })
    }

    fun verifyUser(details: VerificationDetails) {
        if(details == VerificationDetails.OTP) {
            inputOTPSubject = registrationManager.otpSubject
            resendOTPSubject = PublishSubject.create()

            disposables +=
            resendOTPSubject
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe { registrationManager.resendSubject.onNext(details.getDetails()) }
        }

        disposables +=
        registrationManager.verifyDetails(details)
            .subscribeOn(AndroidSchedulers.mainThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                progressLiveData.postValue(false)

                when{
                    it.isSuccess -> verificationSuccessfulLiveData.postValue(true)
                    it.isFailure -> messageLiveData.postValue(it.getAdditionalInfo())
                }
            },{
                Timber.e("error occurred: ${it.message}")
                progressLiveData.postValue(false)
                messageLiveData.postValue(it.message)
            })
    }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }
}
