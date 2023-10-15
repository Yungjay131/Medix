package app.slyworks.core_feature.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import app.slyworks.base_feature.BaseViewModel
import app.slyworks.base_feature.network_register.INetworkRegister
import app.slyworks.data_lib.model.view_entities.CallHistoryVModel
import app.slyworks.data_lib.repositories.home.IHomeRepository
import app.slyworks.utils_lib.utils.plusAssign
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject


/**
 * Created by Joshua Sylvanus, 11:28 AM, 18/1/2022.
 */
sealed class CoreUIState {
    data class LoadingStarted(val prompt:String) : CoreUIState()
    object LoadingStopped : CoreUIState()

    data class UserProfilePic(val uri:String): CoreUIState()

    data class CallsHistory(val callsHistory:List<CallHistoryVModel>) : CoreUIState()

    object NoNetworkConnection : CoreUIState()
    data class Message(val message:String) : CoreUIState()
}

class CoreViewModel
    @Inject
    constructor(override val networkRegister: INetworkRegister,
                private val repository: IHomeRepository) : BaseViewModel() {
    //region Vars
    private val _uiStateLD:MutableLiveData<CoreUIState> = MutableLiveData()
    val uiStateLD:LiveData<CoreUIState> = _uiStateLD

    override val disposables: CompositeDisposable = CompositeDisposable()
    //endregion

    fun getUserFullName():String = repository.getUserFullName()

    fun getUserProfilePic():String = repository.getUserProfilePicUri()

    fun observeUserProfilePic(){
        disposables +=
        repository.observeUserDetails()
            .map{ it.imageUri }
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe({
               _uiStateLD.postValue(CoreUIState.UserProfilePic(it))
            },
            {
               Timber.e(it)
            })
    }

    fun observeCallsHistory(){
        disposables +=
        repository.observeCallsHistory()
            .observeOn(Schedulers.io())
            .subscribeOn(Schedulers.io())
            .subscribe({
                _uiStateLD.postValue(CoreUIState.CallsHistory(it))
            },
            {
                Timber.e(it)
            })
    }

    fun logout(){
        if(!networkRegister.getNetworkStatus()){
            _uiStateLD.setValue(CoreUIState.NoNetworkConnection)
            return
        }

        disposables +=
        repository.logout()
            .doOnSubscribe{
                _uiStateLD.postValue(CoreUIState.LoadingStarted("logging out..."))
            }
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe({
                 _uiStateLD.postValue(CoreUIState.LoadingStopped)

                when{
                  it.isSuccess -> {}
                  it.isFailure || it.isError -> {}
                }
            },
            {
                Timber.e(it)

                _uiStateLD.postValue(CoreUIState.LoadingStopped)
                _uiStateLD.postValue(CoreUIState.Message("an error occurred logging you out"))
            })
    }

    override fun onCleared() {
        super.onCleared()

        disposables.clear()
    }
}
