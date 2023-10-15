package app.slyworks.auth_feature.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import app.slyworks.base_feature.BaseViewModel
import app.slyworks.base_feature.VibrationManager
import app.slyworks.base_feature.network_register.INetworkRegister
import app.slyworks.utils_lib.Outcome
import app.slyworks.data_lib.repositories.login.ILoginRepository
import app.slyworks.utils_lib.ACCOUNT_NOT_VERIFIED_PROMPT
import app.slyworks.utils_lib.NO_NETWORK_CONNECTION_PROMPT
import app.slyworks.utils_lib.utils.plusAssign
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject


/**
 * Created by Joshua Sylvanus, 6:39 AM, 07/06/2022.
 */

sealed class LoginUIState {
    object LoadingStarted : LoginUIState()
    object LoadingStopped : LoginUIState()

    object LoadingForgotPasswordStarted : LoginUIState()
    object LoadingForgotPasswordStopped : LoginUIState()

    object ResetPasswordEmailSuccess : LoginUIState()
    data class ResetPasswordEmailFailure(val reason:String) : LoginUIState()

    object LoginSuccess : LoginUIState()

    data class AccountNotVerified(val message:String) : LoginUIState()

    data class Message(val message:String) :LoginUIState()
}

class LoginActivityViewModel
    @Inject
    constructor(override val networkRegister: INetworkRegister,
                private val vibrationManager: VibrationManager,
                private val repository: ILoginRepository) : BaseViewModel(){
    //region Vars
    private val _uiStateLD: MutableLiveData<LoginUIState> = MutableLiveData()
    val uiStateLD: LiveData<LoginUIState> =_uiStateLD

    override val disposables:CompositeDisposable = CompositeDisposable()
    //endregion

    fun vibrate(type:Int) = vibrationManager.vibrate(type)

    fun login(email:String, password:String){
        if(!networkRegister.getNetworkStatus()){
            _uiStateLD.setValue(LoginUIState.Message(NO_NETWORK_CONNECTION_PROMPT))
            return
        }

        disposables +=
        repository.login(email, password)
            .subscribeOn(Schedulers.io())
            .doOnSubscribe {
                _uiStateLD.setValue(LoginUIState.LoadingStarted)
            }
            .subscribeOn(AndroidSchedulers.mainThread())
            .observeOn(Schedulers.io())
            .subscribe({ it: Outcome ->
                _uiStateLD.postValue(LoginUIState.LoadingStopped)

                when{
                    it.isSuccess ->
                        _uiStateLD.postValue(LoginUIState.LoginSuccess)
                    it.isFailure ->
                        _uiStateLD.postValue(LoginUIState.Message(it.getAdditionalInfo()!!))
                    it.isError ->
                        _uiStateLD.postValue(LoginUIState.AccountNotVerified(ACCOUNT_NOT_VERIFIED_PROMPT))
                }
            },{
                Timber.e("error occurred:", it)
                _uiStateLD.postValue(LoginUIState.LoadingStopped)
                _uiStateLD.postValue(LoginUIState.Message("an error occurred"))
            })
    }

    fun handleForgotPassword(email: String){
        if(!networkRegister.getNetworkStatus()){
            _uiStateLD.postValue(LoginUIState.Message(NO_NETWORK_CONNECTION_PROMPT))
            return
        }

        disposables +=
        repository.handleForgotPassword(email)
            .doOnSubscribe {
                _uiStateLD.postValue(LoginUIState.LoadingForgotPasswordStarted)
            }
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe({ it:Outcome ->
                _uiStateLD.postValue(LoginUIState.LoadingForgotPasswordStopped)

                when{
                    it.isSuccess ->
                    _uiStateLD.postValue(LoginUIState.ResetPasswordEmailSuccess)
                    it.isFailure || it.isError ->
                    _uiStateLD.postValue(LoginUIState.ResetPasswordEmailFailure(it.getAdditionalInfo()!!))
                }
            },
            {
                    Timber.e("error occurred:", it)
                    _uiStateLD.postValue(LoginUIState.LoadingStopped)
                    _uiStateLD.postValue(LoginUIState.Message("an error occurred"))
            })
    }

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }
}