package com.slyworks.medix.ui.fragments.callsHistoryFragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.slyworks.communication.CallHistoryManager
import com.slyworks.medix.utils.plusAssign
import com.slyworks.models.room_models.CallHistory
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject


/**
 *Created by Joshua Sylvanus, 1:47 PM, 13/05/2022.
 */
class CallsHistoryViewModel
    @Inject
    constructor(private val callHistoryManager: CallHistoryManager) : ViewModel() {
    //region Vars
    private val _errorState:MutableLiveData<Boolean> = MutableLiveData()
    val errorState:LiveData<Boolean>
    get() = _errorState

    private val _errorData:MutableLiveData<String> = MutableLiveData()
    val errorData:LiveData<String>
    get() = _errorData

    val progressState:MutableLiveData<Boolean> = MutableLiveData(true)
     private val l:MutableLiveData<List<CallHistory>> = MutableLiveData()

    private val disposables:CompositeDisposable = CompositeDisposable()
    //endregion

    fun observeCallsHistory():LiveData<List<CallHistory>>{
        disposables +=
            callHistoryManager.observeCallsHistory()
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .subscribe {
                    progressState.postValue(false)

                    if(it.isNullOrEmpty()){
                        _errorData.postValue("You have no calls. Click the 'call' icon to get started")
                        _errorState.postValue(true)
                    }else {
                        _errorState.postValue(false)
                        l.postValue(it)
                    }
                }

        return l
    }

    fun unbind(){
        callHistoryManager.detachObserveCallsHistoryObserver()
        disposables.clear()
    }

    override fun onCleared() {
        super.onCleared()
        unbind()
    }
}