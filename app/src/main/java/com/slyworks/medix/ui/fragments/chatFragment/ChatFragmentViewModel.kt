package com.slyworks.medix.ui.fragments.chatFragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.slyworks.communication.MessageManager
import com.slyworks.medix.utils.plusAssign
import com.slyworks.models.models.Outcome
import com.slyworks.models.room_models.FBUserDetails
import com.slyworks.models.room_models.Person
import com.slyworks.network.NetworkRegister
import com.slyworks.userdetails.UserDetailsUtils
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject


/**
 *Created by Joshua Sylvanus, 11:27 AM, 1/18/2022.
 */
class ChatFragmentViewModel
    @Inject
    constructor(private val networkRegister: NetworkRegister,
                private val userDetailsUtils: UserDetailsUtils,
                private val messageManager:MessageManager) : ViewModel() {
    //region Vars
    private val disposables: CompositeDisposable = CompositeDisposable()

    private val _successStateLiveData: MutableLiveData<List<Person>> = MutableLiveData()
    val successStateLiveData: LiveData<List<Person>>
        get() = _successStateLiveData

    private val _introStateLiveData: MutableLiveData<Boolean> = MutableLiveData()
    val intoStateLiveData: LiveData<Boolean>
        get() = _introStateLiveData

    private val _errorStateLiveData: MutableLiveData<Boolean> = MutableLiveData()
    val errorStateLiveData: LiveData<Boolean>
        get() = _errorStateLiveData

    private val _errorDataLiveData: MutableLiveData<String> = MutableLiveData()
    val errorDataLiveData: LiveData<String>
        get() = _errorDataLiveData

    private val _progressStateLiveData: MutableLiveData<Boolean> = MutableLiveData()
    val progressStateLiveData: LiveData<Boolean>
        get() = _progressStateLiveData
    //endregion

    fun getUserDetailsUtils():FBUserDetails = userDetailsUtils.user!!

    fun getChats() {
        disposables +=
            networkRegister.subscribeToNetworkUpdates()
                .flatMap {
                    if (!it)
                        Observable.just(Outcome.ERROR<Unit>(additionalInfo = "no network connection"))
                    else
                        messageManager.observeMessagePersons()
                }
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe {
                    _progressStateLiveData.postValue(false)

                    when {
                        it.isSuccess -> {
                            _introStateLiveData.postValue(false)
                            _errorStateLiveData.postValue(false)
                            _successStateLiveData.postValue(it.getTypedValue())
                        }
                        it.isFailure -> {
                            _errorStateLiveData.postValue(false)
                            _introStateLiveData.postValue(true)
                        }
                        it.isError -> {
                            _introStateLiveData.postValue(false)
                            _errorDataLiveData.postValue(it.getAdditionalInfo())
                            _errorStateLiveData.postValue(true)
                        }
                    }
                }
    }

    fun getNetworkStatus(): Boolean = networkRegister.getNetworkStatus()

    override fun onCleared() {
        super.onCleared()
        disposables.clear()

        messageManager.detachObserveMessagePersonsListener()
        networkRegister.unsubscribeToNetworkUpdates()
    }
}
