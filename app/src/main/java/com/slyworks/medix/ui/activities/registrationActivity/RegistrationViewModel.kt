package com.slyworks.medix.ui.activities.registrationActivity

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.slyworks.medix.App
import com.slyworks.network.NetworkRegister
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers

class RegistrationViewModel : ViewModel() {
    //region Vars
    private var mSubscription:Disposable = Disposable.empty()

    private var mNetworkRegister: NetworkRegister? = null
    //endregion


    fun subscribeToNetwork(): LiveData<Boolean> {
        val l: MutableLiveData<Boolean> = MutableLiveData()

        mNetworkRegister = NetworkRegister(App.getContext())
        mSubscription = mNetworkRegister!!
            .subscribeToNetworkUpdates()
            .observeOn(Schedulers.io())
            .subscribeOn(Schedulers.io())
            .subscribe {
                l.postValue(it)
            }

        return l
    }


    fun unsubscribeToNetwork(){
        mNetworkRegister!!.unsubscribeToNetworkUpdates()
        mNetworkRegister = null
        mSubscription.dispose()
    }
}
