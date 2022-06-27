package com.slyworks.medix.ui.fragments.findDoctorsFragment

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.slyworks.medix.App
import com.slyworks.medix.managers.UsersManager
import com.slyworks.models.room_models.FBUserDetails
import com.slyworks.network.NetworkRegister
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers


/**
 *Created by Joshua Sylvanus, 4:21 AM, 1/8/2022.
 */
class FindDoctorsViewModel : ViewModel() {
    //region Vars
    var mDoctorsListLiveData:MutableLiveData<MutableList<FBUserDetails>> = MutableLiveData()
    private val mSubscriptions:CompositeDisposable = CompositeDisposable()
    private var mNetworkRegister:NetworkRegister? = null
    //endregion

    init {
        mNetworkRegister = NetworkRegister(App.getContext())
    }

    fun getNetworkStatus():Boolean = mNetworkRegister!!.getNetworkStatus()
    fun getAllDoctors() = UsersManager.getAllDoctors()
    private fun observeDoctors(){
        val d = UsersManager.observeDoctors()
            .observeOn(Schedulers.io())
            .subscribeOn(Schedulers.io())
            .subscribe {
                if(it.isSuccess)
                   mDoctorsListLiveData.postValue(it.getTypedValue())
            }
    }


    override fun onCleared() {
        super.onCleared()
        //mSubscriptions.clear()
        //UsersManager.detachGetAllDoctorsListener()
    }
}