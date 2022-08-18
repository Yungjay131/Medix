package com.slyworks.medix.ui.activities.main_activity

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.slyworks.network.NetworkRegister
import com.slyworks.userdetails.UserDetailsUtils
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject


/**
 *Created by Joshua Sylvanus, 4:34 PM, 1/23/2022.
 */
class MainActivityViewModel
    @Inject
    constructor(private val networkRegister: NetworkRegister,
                private val userDetailsUtils: UserDetailsUtils): ViewModel() {
    //region Vars
    val mSubscriptions:CompositeDisposable = CompositeDisposable()
    private var mSubscription:Disposable = Disposable.empty()
    private val l: MutableLiveData<Boolean> = MutableLiveData()
    //endregion

    fun getUserAccountType(): String = userDetailsUtils.user!!.accountType

    fun subscribeToNetwork(): LiveData<Boolean> {
        mSubscription =
          networkRegister.subscribeToNetworkUpdates()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                l.postValue(it)
            }

        return l
    }

    fun unsubscribeToNetwork(){
        networkRegister.unsubscribeToNetworkUpdates()
        mSubscription.dispose()
    }

}