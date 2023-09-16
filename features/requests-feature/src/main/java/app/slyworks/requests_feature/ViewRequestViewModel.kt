package app.slyworks.requests_feature

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import app.slyworks.auth_lib.LoginManager
import app.slyworks.auth_lib.UsersManager
import app.slyworks.communication_lib.ConsultationRequestsManager
import app.slyworks.constants_lib.NO_INTERNET_CONNECTION_MESSAGE
import app.slyworks.data_lib.DataManager
import app.slyworks.data_lib.vmodels.FBUserDetailsVModel
import app.slyworks.data_lib.models.ConsultationResponse
import app.slyworks.network_lib.NetworkRegister
import app.slyworks.utils_lib.utils.plusAssign
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject

/**
 * Created by Joshua Sylvanus, 8:15 PM, 06/07/2022.
 */

sealed class ViewRequestUIState {
    object LoadingStarted : ViewRequestUIState()
    object LoadingStopped : ViewRequestUIState()
    data class UserDetailsRetrieved(val details:FBUserDetailsVModel) : ViewRequestUIState()
    data class UserDetailsNotRetrieved(val error: String) : ViewRequestUIState()
    object SendResponseSuccess : ViewRequestUIState()
    data class SendResponseFailure(val error: String) : ViewRequestUIState()
    data class Message(val message: String) : ViewRequestUIState()
}

class ViewRequestViewModel
    @Inject
    constructor(private val loginManager: LoginManager,
                private val usersManager: UsersManager,
                private val consultationRequestsManager: ConsultationRequestsManager,
                private val networkRegister: NetworkRegister,
                private val dataManager: DataManager) : ViewModel() {

    private val _uiStateLD:MutableLiveData<ViewRequestUIState> = MutableLiveData()
    val uiStateLD:LiveData<ViewRequestUIState> = _uiStateLD

    private val disposables:CompositeDisposable = CompositeDisposable()

    fun getUserDetailsUtils():FBUserDetailsVModel =
        dataManager.getUserDetailsProperty<FBUserDetailsVModel>()!!

    fun getLoginStatus():Boolean = loginManager.getLoginStatus()

    fun getUserDetails(userUID:String){
        if(!networkRegister.getNetworkStatus()){
            _uiStateLD.postValue(ViewRequestUIState.Message(NO_INTERNET_CONNECTION_MESSAGE))
            return
        }

       disposables +=
       usersManager.getUserDataForUID(userUID)
                  .doOnSubscribe { _uiStateLD.postValue(ViewRequestUIState.LoadingStarted) }
                  .subscribeOn(Schedulers.io())
                  .observeOn(Schedulers.io())
                  .subscribe {
                      _uiStateLD.postValue(ViewRequestUIState.LoadingStopped)

                      when{
                          it.isSuccess ->
                              _uiStateLD.postValue(ViewRequestUIState.UserDetailsRetrieved(it.getTypedValue()))
                          it.isFailure ->
                              _uiStateLD.postValue(ViewRequestUIState.UserDetailsNotRetrieved(it.getAdditionalInfo()!!))
                      }
                  }
    }

    fun respondToRequest(response: ConsultationResponse){
        if(!networkRegister.getNetworkStatus()){
            _uiStateLD.postValue(ViewRequestUIState.Message(NO_INTERNET_CONNECTION_MESSAGE))
            return
        }

        disposables +=
        consultationRequestsManager.sendConsultationRequestResponse(response)
            .doOnSubscribe { _uiStateLD.postValue(ViewRequestUIState.LoadingStarted) }
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe {
                _uiStateLD.postValue(ViewRequestUIState.LoadingStopped)

                when {
                    it.isSuccess ->
                        _uiStateLD.postValue(ViewRequestUIState.SendResponseSuccess)
                    it.isFailure ->
                        _uiStateLD.postValue(ViewRequestUIState.SendResponseFailure(it.getAdditionalInfo()!!))
                }
            }

    }

    override fun onCleared() {
        super.onCleared()
        onStop()
    }

    private fun onStop(){
        disposables.clear()
    }
}