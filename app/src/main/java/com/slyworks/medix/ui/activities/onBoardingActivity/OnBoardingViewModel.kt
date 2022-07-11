package com.slyworks.medix.ui.activities.onBoardingActivity

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.slyworks.medix.App
import com.slyworks.network.NetworkRegister
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers


/**
 *Created by Joshua Sylvanus, 5:54 PM, 18/05/2022.
 */
class OnBoardingViewModel : ViewModel() {
    //region Vars
    private val mSubscriptions:CompositeDisposable = CompositeDisposable()
    private var mNetworkRegister: NetworkRegister? = null
    //endregion

    fun subscribeToNetwork(): LiveData<Boolean>{
        val l:MutableLiveData<Boolean> = MutableLiveData()

        mNetworkRegister = NetworkRegister(App.getContext())
        val d1 = mNetworkRegister!!
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
        mNetworkRegister!!.unsubscribeToNetworkUpdates()
        mNetworkRegister = null
        mSubscriptions.clear()
    }
}