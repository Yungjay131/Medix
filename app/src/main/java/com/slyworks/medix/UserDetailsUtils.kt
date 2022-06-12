package com.slyworks.medix

import com.slyworks.models.room_models.FBUserDetails
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged


/**
 *Created by Joshua Sylvanus, 12:34 PM, 1/9/2022.
 */
object UserDetailsUtils {
     //region Vars
      var user:FBUserDetails? = null
      var user2: FBUserDetails? = null

       private val o:PublishSubject<FBUserDetails> = PublishSubject.create()
      //endregion

    init {
       observeUser()
    }


    private fun observeUser(){
        CoroutineScope(Dispatchers.IO).launch {
            UsersManager.getUserFromDataStore()
                .distinctUntilChanged()
                .collectLatest {
                    user = it
                    user2 = it

                    o.onNext(user2!!)
            }
        }

    }

    /*for something like profile picture change,for it to happen immediately*/
    fun observeUserDetails(): Observable<FBUserDetails> {
        return o.hide()
    }

    /*for log out situations*/
    fun clearUserData(){
        user = null
        user2 = null
    }
}