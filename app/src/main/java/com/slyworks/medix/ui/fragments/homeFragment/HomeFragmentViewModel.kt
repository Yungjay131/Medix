package com.slyworks.medix.ui.fragments.homeFragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.slyworks.medix.UserDetailsUtils
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers


/**
 *Created by Joshua Sylvanus, 11:28 AM, 1/18/2022.
 */
class HomeFragmentViewModel : ViewModel(){
    //region Vars
    private var mSubscriptions:CompositeDisposable = CompositeDisposable()
    //endregion

    fun observeUserProfilePic():LiveData<String>{
        val l:MutableLiveData<String> = MutableLiveData()
        val d1 = UserDetailsUtils.observeUserDetails()
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe{ l.postValue(it.imageUri) }

        mSubscriptions.add(d1)
        return l
    }

    override fun onCleared() {
        super.onCleared()
        mSubscriptions.clear()
    }
}