package com.slyworks.medix.ui.activities.splash_activity

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.slyworks.auth.UsersManager
import com.slyworks.constants.KEY_LAST_SIGN_IN_TIME
import com.slyworks.models.room_models.FBUserDetails
import com.slyworks.userdetails.UserDetailsUtils
import com.slyworks.utils.PreferenceManager
import com.slyworks.utils.TimeUtils
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import java.util.concurrent.TimeUnit
import javax.inject.Inject


/**
 *Created by Joshua Sylvanus, 9:32 PM, 16/08/2022.
 */
class SplashActivityViewModel
    @Inject
    constructor(private val usersManager:UsersManager,
                private val userDetailsUtils: UserDetailsUtils,
                private val preferenceManager: PreferenceManager,
                private val firebaseAuth:FirebaseAuth): ViewModel() {

        //region Vars
         private val _isSessionValid:MutableLiveData<Boolean> = MutableLiveData()
         val isSessionValid:LiveData<Boolean>
         get() = _isSessionValid

         private val disposables:CompositeDisposable = CompositeDisposable()
        //endregion

        private fun checkUsersDetailsAvailability(): Single<Boolean>
         = Single.create{ emitter ->
             viewModelScope.launch {
                 usersManager.getUserFromDataStore()
                     .collectLatest { it: FBUserDetails ->
                         if(it.firebaseUID.isEmpty()){
                             emitter.onSuccess(false)

                             this.coroutineContext.cancel()
                             return@collectLatest
                         }

                         userDetailsUtils.user = it
                         emitter.onSuccess(false)
                         this.coroutineContext.cancel()
                     }
               }
           }

        private fun checkIfSessionIsExpired():Single<Boolean>
         = Single.fromCallable {
            val lastSignInTime: Long = preferenceManager.get(KEY_LAST_SIGN_IN_TIME, System.currentTimeMillis())
            return@fromCallable TimeUtils.isWithinTimePeriod(lastSignInTime, 3, TimeUnit.DAYS)
         }


         fun verify(){
             firebaseAuth.addAuthStateListener {

             }
         }
         fun checkLoginSession(){
             checkUsersDetailsAvailability()
                 .observeOn(Schedulers.io())
                 .flatMap {
                     if(it)
                         checkIfSessionIsExpired()
                     else
                         Single.just(false)
                 }
                 .subscribeOn(Schedulers.io())
                 .observeOn(Schedulers.io())
                 .subscribe(_isSessionValid::postValue)
         }
}