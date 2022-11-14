package com.slyworks.medix.ui.fragments.viewProfileFragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.slyworks.communication.CloudMessageManager
import com.slyworks.medix.utils.plusAssign
import com.slyworks.models.models.ConsultationRequest
import com.slyworks.models.models.MessageMode
import com.slyworks.models.room_models.FBUserDetails
import com.slyworks.userdetails.UserDetailsUtils
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject


/**
 *Created by Joshua Sylvanus, 9:21 AM, 27/06/2022.
 */
class ViewProfileFragmentViewModel
    @Inject
    constructor(private val cloudMessageManager: CloudMessageManager,
                private val userDetailsUtils: UserDetailsUtils) : ViewModel(){
    //region Vars
    private val _consultationRequestStatusLiveData:MutableLiveData<String> = MutableLiveData()
    val consultationRequestStatusLiveData:LiveData<String>
    get() = _consultationRequestStatusLiveData

    private val disposables = CompositeDisposable()

    private lateinit var userUID:String
    //endregion

    fun getUserDetailsUser():FBUserDetails = userDetailsUtils.user!!

    fun observeConsultationRequestStatus(userUID:String){
        this.userUID = userUID
        disposables +=
        cloudMessageManager.observeConsultationRequestStatus(userUID)
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe {
               _consultationRequestStatusLiveData.postValue(it)
            }
    }

    fun sendConsultationRequest(request:ConsultationRequest, mode:MessageMode = MessageMode.DB_MESSAGE)
        = cloudMessageManager.sendConsultationRequest(request, mode)

    override fun onCleared() {
        disposables.clear()
        cloudMessageManager.detachCheckRequestStatusListener(userUID)
        super.onCleared()
    }

}