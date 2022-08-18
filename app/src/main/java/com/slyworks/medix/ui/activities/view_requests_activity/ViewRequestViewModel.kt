package com.slyworks.medix.ui.activities.view_requests_activity

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.slyworks.auth.LoginManager
import com.slyworks.auth.UsersManager
import com.slyworks.communication.CloudMessageManager
import com.slyworks.medix.utils.plusAssign
import com.slyworks.models.models.ConsultationResponse
import com.slyworks.models.room_models.FBUserDetails
import com.slyworks.userdetails.UserDetailsUtils
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject


/**
 *Created by Joshua Sylvanus, 8:15 PM, 06/07/2022.
 */
class ViewRequestViewModel
    @Inject
    constructor(private val loginManager: LoginManager,
                private val usersManager: UsersManager,
                private val cloudMessageManager: CloudMessageManager,
                private val userDetailsUtils: UserDetailsUtils) : ViewModel() {
    //region Vars
    private val _successData:MutableLiveData<FBUserDetails> = MutableLiveData()
    val successData:LiveData<FBUserDetails>
    get() = _successData

    private val _successState:MutableLiveData<Boolean> = MutableLiveData()
    val successState:LiveData<Boolean>
    get() = _successState

    private val _errorData:MutableLiveData<String> = MutableLiveData()
    val errorData:LiveData<String>
    get() = _errorData

    private val _errorState:MutableLiveData<Boolean> = MutableLiveData()
    val errorState:LiveData<Boolean>
    get() = _errorState

    val progressState:MutableLiveData<Boolean> = MutableLiveData()

    private val disposables:CompositeDisposable = CompositeDisposable()
    //endregion
    fun getUserDetailsUtils():FBUserDetails = userDetailsUtils.user!!

    fun getLoginStatus():Boolean = loginManager.getLoginStatus()

    fun getUserDetails(userUID:String){
       disposables +=
            usersManager.getUserDataForUID(userUID)
                       .subscribeOn(Schedulers.io())
                       .observeOn(Schedulers.io())
                       .subscribe {
                           progressState.postValue(false)

                           when{
                               it.isSuccess ->{
                                   _errorState.postValue(false)
                                   _successData.postValue(it.getTypedValue())
                                   _successState.postValue(true)
                               }
                               it.isFailure ->{
                                   _errorData.postValue(it.getAdditionalInfo())
                                   _errorState.postValue(true)
                               }
                           }
                       }
    }

    fun respondToRequest(response:ConsultationResponse){
       disposables +=
            cloudMessageManager.sendConsultationRequestResponse(response)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe {
                    progressState.postValue(false)

                    when {
                        it.isSuccess -> {
                            _errorState.postValue(false)
                        }
                        it.isFailure -> {
                            _errorData.postValue(it.getAdditionalInfo())
                            _errorState.postValue(true)
                        }
                    }
                }

    }

    private fun onStop(){
        disposables.clear()
    }

    override fun onCleared() {
        super.onCleared()
        onStop()
    }
}