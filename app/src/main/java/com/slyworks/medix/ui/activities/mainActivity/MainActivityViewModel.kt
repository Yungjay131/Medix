package com.slyworks.medix.ui.activities.mainActivity

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.slyworks.constants.KEY_UNREAD_MESSAGE_COUNT
import com.slyworks.medix.*
import com.slyworks.medix.utils.*
import com.slyworks.network.NetworkRegister
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers


/**
 *Created by Joshua Sylvanus, 4:34 PM, 1/23/2022.
 */
class MainActivityViewModel: ViewModel() {
    //region Vars
    val mSubscriptions:CompositeDisposable = CompositeDisposable()
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

        l.postValue(mNetworkRegister!!.getNetworkStatus())
        return l
    }


    fun unsubscribeToNetwork(){
        mNetworkRegister!!.unsubscribeToNetworkUpdates()
        mNetworkRegister = null
        mSubscription.dispose()
    }
}