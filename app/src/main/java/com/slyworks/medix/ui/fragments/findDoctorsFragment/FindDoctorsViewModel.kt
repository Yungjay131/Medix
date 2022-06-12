package com.slyworks.medix.ui.fragments.findDoctorsFragment

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slyworks.medix.UsersManager
import com.slyworks.models.room_models.FBUserDetails
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


/**
 *Created by Joshua Sylvanus, 4:21 AM, 1/8/2022.
 */
class FindDoctorsViewModel : ViewModel() {
    //region Vars
    var mDoctorsListLiveData:MutableLiveData<MutableList<FBUserDetails>>? = MutableLiveData()
    //endregion

    val flow = viewModelScope.launch {
        UsersManager.observeDoctors()
            .collectLatest { doctor ->
                mDoctorsListLiveData?.value = doctor
            }
    }

    override fun onCleared() {
        super.onCleared()
        UsersManager.detachGetAllDoctorsListener()
        mDoctorsListLiveData = null
    }
}