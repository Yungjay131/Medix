package com.slyworks.medix.ui.fragments.homeFragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.slyworks.medix.utils.plusAssign
import com.slyworks.models.room_models.FBUserDetails
import com.slyworks.userdetails.UserDetailsUtils
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject


/**
 *Created by Joshua Sylvanus, 11:28 AM, 1/18/2022.
 */
class HomeFragmentViewModel
    @Inject
    constructor(private val userDetailsUtils: UserDetailsUtils) : ViewModel() {
    //region Vars
    private var disposables: CompositeDisposable = CompositeDisposable()
    //endregion

    fun observeUserProfilePic(): LiveData<String> {
        val l: MutableLiveData<String> = MutableLiveData()
        disposables +=
        userDetailsUtils.observeUserDetails()
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe { l.postValue(it.imageUri) }

        return l
    }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }

    fun getUser(): FBUserDetails = userDetailsUtils.user!!
}