package com.slyworks.medix.ui.activities.loginActivity

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.slyworks.medix.managers.CloudMessageManager
import com.slyworks.medix.managers.UsersManager
import com.slyworks.models.models.ConsultationResponse
import com.slyworks.models.room_models.FBUserDetails
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers


/**
 *Created by Joshua Sylvanus, 8:15 PM, 06/07/2022.
 */
class ViewRequestViewModel : ViewModel() {
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

    private val mSubscriptions:CompositeDisposable = CompositeDisposable()
    //endregion

    fun getUserDetails(userUID:String){
        val d =
            UsersManager.getUserDataForUID(userUID)
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

        mSubscriptions.add(d)
    }

    fun respondToRequest(response:ConsultationResponse){
        val d =
            CloudMessageManager.sendConsultationRequestResponse(response)
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

        mSubscriptions.add(d)
    }

    private fun onStop(){
        mSubscriptions.clear()
    }

    override fun onCleared() {
        super.onCleared()
        onStop()
    }
}