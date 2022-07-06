package com.slyworks.medix.ui.fragments.chatFragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.slyworks.medix.App
import com.slyworks.medix.managers.MessageManager
import com.slyworks.models.models.Outcome
import com.slyworks.models.room_models.Person
import com.slyworks.network.NetworkRegister
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers


/**
 *Created by Joshua Sylvanus, 11:27 AM, 1/18/2022.
 */
class ChatFragmentViewModel : ViewModel(){
    //region Vars
    private val mSubscriptions:CompositeDisposable = CompositeDisposable()

    private val _successStateLiveData:MutableLiveData<List<Person>> = MutableLiveData()
    val successStateLiveData:LiveData<List<Person>>
    get() = _successStateLiveData

    private val _introStateLiveData:MutableLiveData<Boolean> = MutableLiveData()
    val intoStateLiveData:LiveData<Boolean>
    get() = _introStateLiveData

    private val _errorStateLiveData:MutableLiveData<Boolean> = MutableLiveData()
    val errorStateLiveData:LiveData<Boolean>
    get() = _errorStateLiveData

    private val _errorDataLiveData:MutableLiveData<String> = MutableLiveData()
    val errorDataLiveData:LiveData<String>
    get() = _errorDataLiveData

    private val _progressStateLiveData:MutableLiveData<Boolean> = MutableLiveData()
    val progressStateLiveData:LiveData<Boolean>
    get() = _progressStateLiveData


    private var mNetworkRegister:NetworkRegister? = NetworkRegister(App.getContext())

    private var isMessageManagerInitialized:Boolean = false
    //endregion

    fun getChats(){
       val d = Observable.merge(
           Observable.just(mNetworkRegister!!.getNetworkStatus()),
           mNetworkRegister!!.subscribeToNetworkUpdates()
       )
           .flatMap {
               if(!it)
                   Observable.just(Outcome.ERROR<Nothing>(additionalInfo = "no network connection"))
               else {
                   isMessageManagerInitialized = true
                   MessageManager.observeMessagePersons()
               }
           }
           .subscribeOn(Schedulers.io())
           .observeOn(Schedulers.io())
           .subscribe {
               _progressStateLiveData.postValue(false)

               when{
                   it.isSuccess ->{
                       _introStateLiveData.postValue(false)
                       _errorStateLiveData.postValue(false)
                       _successStateLiveData.postValue(it.getTypedValue())
                   }
                   it.isFailure ->{
                       _errorStateLiveData.postValue(false)
                       _introStateLiveData.postValue(true)
                   }
                   it.isError ->{
                       _introStateLiveData.postValue(false)
                       _errorDataLiveData.postValue(it.getAdditionalInfo())
                       _errorStateLiveData.postValue(true)
                   }
               }
           }

        mSubscriptions.add(d)
    }

   fun getNetworkStatus():Boolean = mNetworkRegister!!.getNetworkStatus()

    override fun onCleared() {
        super.onCleared()
        mSubscriptions.clear()

        if(isMessageManagerInitialized)
            MessageManager.detachObserveMessagePersonsListener()

        mNetworkRegister!!.unsubscribeToNetworkUpdates()
        mNetworkRegister = null

    }
}