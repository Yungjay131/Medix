package com.slyworks.medix.ui.fragments.chatFragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.slyworks.constants.EVENT_GET_ALL_MESSAGES
import com.slyworks.medix.App
import com.slyworks.medix.AppController
import com.slyworks.medix.AppController.clearAndRemove
import com.slyworks.medix.managers.DataManager
import com.slyworks.medix.Subscription
import com.slyworks.models.models.Observer
import com.slyworks.models.models.Outcome
import com.slyworks.network.NetworkRegister
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers


/**
 *Created by Joshua Sylvanus, 11:27 AM, 1/18/2022.
 */
class ChatFragmentViewModel : ViewModel(), Observer {
    //region Vars
    private val mSubscriptionList:MutableList<Subscription> = mutableListOf()
    private val mSubscriptions:CompositeDisposable = CompositeDisposable()

    private val _mPersonListLiveData:MutableLiveData<Outcome> = MutableLiveData()

    private var mDataManager: DataManager? = null
    private var mNetworkRegister:NetworkRegister? = null
    //endregion

    init {
        AppController.addEvent(EVENT_GET_ALL_MESSAGES)
        val subscription = AppController.subscribeTo(EVENT_GET_ALL_MESSAGES, this)
         mSubscriptionList.add(subscription)

        mDataManager = DataManager()
        mNetworkRegister = NetworkRegister(App.getContext())
    }

    fun getPersonsListLiveData():LiveData<Outcome> = _mPersonListLiveData

    fun getChats(){
       val d = Observable.merge(
           Observable.just(mNetworkRegister!!.getNetworkStatus()),
           mNetworkRegister!!.subscribeToNetworkUpdates()
       )
           .flatMap {
               if(!it)
                   Observable.just(Outcome.ERROR(value = "no network connection detected"))
               else
                   mDataManager!!.observeMessagePersonsFromFB()
           }
           .subscribeOn(Schedulers.io())
           .observeOn(Schedulers.io())
           .subscribe {
               _mPersonListLiveData.postValue(it)
           }

        mSubscriptions.add(d)
    }

    fun cleanup(){
        mSubscriptions.clear()
        mDataManager!!.detachMessagesPersonsFromFBListener()
    }

    override fun <T> notify(event: String, data: T?) {
        when(event){
            EVENT_GET_ALL_MESSAGES -> {}
        }
    }

    override fun onCleared() {
        super.onCleared()
        mSubscriptionList.forEach { it.clearAndRemove() }
        mSubscriptions.clear()

        //mDataManager!!.detachMessagesPersonsFromFBListener()
        mDataManager = null
        mNetworkRegister!!.unsubscribeToNetworkUpdates()
        mNetworkRegister = null

    }
}