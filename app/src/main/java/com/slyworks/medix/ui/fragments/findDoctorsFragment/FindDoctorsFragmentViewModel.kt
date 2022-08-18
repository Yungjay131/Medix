package com.slyworks.medix.ui.fragments.findDoctorsFragment

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.slyworks.auth.UsersManager
import com.slyworks.medix.App
import com.slyworks.medix.utils.plusAssign
import com.slyworks.models.room_models.FBUserDetails
import com.slyworks.network.NetworkRegister
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject


/**
 *Created by Joshua Sylvanus, 4:21 AM, 1/8/2022.
 */
class FindDoctorsFragmentViewModel
    @Inject
    constructor(private val networkRegister: NetworkRegister,
                private val usersManager: UsersManager) : ViewModel() {
    //region Vars
    val mDoctorsListLiveData:MutableLiveData<MutableList<FBUserDetails>> = MutableLiveData()
    private val disposables:CompositeDisposable = CompositeDisposable()
    //endregion

    fun getNetworkStatus():Boolean = networkRegister.getNetworkStatus()
    fun getAllDoctors() = usersManager.getAllDoctors()
    private fun observeDoctors(){
        disposables +=
        usersManager.observeDoctors()
            .observeOn(Schedulers.io())
            .subscribeOn(Schedulers.io())
            .subscribe {
                if(it.isSuccess)
                   mDoctorsListLiveData.postValue(it.getTypedValue())
            }
    }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }
}