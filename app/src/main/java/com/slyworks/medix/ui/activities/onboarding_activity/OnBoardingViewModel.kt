package com.slyworks.medix.ui.activities.onboarding_activity

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.slyworks.network.NetworkRegister
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject


/**
 *Created by Joshua Sylvanus, 5:54 PM, 18/05/2022.
 */
class OnBoardingViewModel
    @Inject
    constructor(private var networkRegister: NetworkRegister?) : ViewModel() {
    //region Vars
    private val l:MutableLiveData<Boolean> = MutableLiveData()
    private val mSubscriptions:CompositeDisposable = CompositeDisposable()
    //endregion

    fun subscribeToNetwork(): LiveData<Boolean>{
        val d1 = networkRegister!!
            .subscribeToNetworkUpdates()
            .observeOn(Schedulers.io())
            .subscribeOn(Schedulers.io())
            .subscribe {
                l.postValue(it)
            }

        mSubscriptions.add(d1)
        return l
    }

    fun unsubscribeToNetwork(){
        networkRegister!!.unsubscribeToNetworkUpdates()
        mSubscriptions.clear()
    }

    override fun onCleared() {
        super.onCleared()
        unsubscribeToNetwork()
        networkRegister = null
    }
}