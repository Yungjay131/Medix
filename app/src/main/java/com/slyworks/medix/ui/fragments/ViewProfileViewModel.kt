package com.slyworks.medix.ui.fragments

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.slyworks.medix.managers.CloudMessageManager
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers


/**
 *Created by Joshua Sylvanus, 9:21 AM, 27/06/2022.
 */
class ViewProfileViewModel : ViewModel(){
    //region Vars
    private val _consultationRequestStatusLiveData:MutableLiveData<String> = MutableLiveData()
    val consultationRequestStatusLiveData:LiveData<String>
    get() = _consultationRequestStatusLiveData

    private val mSubscriptions = CompositeDisposable()

    private lateinit var userUID:String
    //endregion

    fun observeConsultationRequestStatus(userUID:String){
        this.userUID = userUID
        val d = CloudMessageManager.observeConsultationRequestStatus(userUID)
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe {
               _consultationRequestStatusLiveData.postValue(it)
            }

        mSubscriptions.add(d)
    }


    override fun onCleared() {
        mSubscriptions.clear()
        CloudMessageManager.detachCheckRequestStatusListener(userUID)
        super.onCleared()
    }

}