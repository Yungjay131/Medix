package com.slyworks.medix.ui.activities.registration_activity

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.slyworks.models.models.TempUserDetails
import com.slyworks.network.NetworkRegister
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject

class RegistrationActivityViewModel
    @Inject
    constructor(private val networkRegister: NetworkRegister) : ViewModel() {
    //region Vars
    private var registrationDetails:TempUserDetails = TempUserDetails()

    private var mSubscription:Disposable = Disposable.empty()
    private val l: MutableLiveData<Boolean> = MutableLiveData()
    //endregion

    fun subscribeToNetwork(): LiveData<Boolean> {
        mSubscription = networkRegister
            .subscribeToNetworkUpdates()
            .observeOn(Schedulers.io())
            .subscribeOn(Schedulers.io())
            .subscribe {
                l.postValue(it)
            }

        return l
    }

    fun unsubscribeToNetwork(){
        networkRegister.unsubscribeToNetworkUpdates()
        mSubscription.dispose()
    }

    fun getUserDetails():TempUserDetails = registrationDetails
    fun setUserDetails(details: TempUserDetails){
        registrationDetails = details
    }
}
