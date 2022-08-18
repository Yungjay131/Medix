package com.slyworks.medix.ui.activities.message_activity

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.slyworks.auth.PersonsManager
import com.slyworks.auth.UsersManager
import com.slyworks.communication.ConnectionStatusManager
import com.slyworks.communication.MessageManager
import com.slyworks.controller.Observer
import com.slyworks.medix.helpers.*
import com.slyworks.medix.utils.plusAssign
import com.slyworks.models.room_models.FBUserDetails
import com.slyworks.models.room_models.Message
import com.slyworks.userdetails.UserDetailsUtils
import com.slyworks.utils.TimeUtils
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject


/**
 *Created by Joshua Sylvanus, 12:27 PM, 1/13/2022.
 */
class MessageActivityViewModel
    @Inject
    constructor(private val usersManager: UsersManager,
                private val messageManager:MessageManager,
                private val userDetailsUtils: UserDetailsUtils,
                private val connectionStatusManager: ConnectionStatusManager,
                private val personsManager: PersonsManager) : ViewModel(), Observer {
    //region Vars
    private val mMessageListLiveData: MutableLiveData<MutableList<Message>> = MutableLiveData(mutableListOf())
    private val mConnectionStatusLiveData:MutableLiveData<String> = MutableLiveData()
    private val mUserDetailsLiveData:MutableLiveData<FBUserDetails> = MutableLiveData()

    private val _startCallDataLiveData:MutableLiveData<FBUserDetails> = MutableLiveData()
    val startCallDataLiveData:LiveData<FBUserDetails>
    get() = _startCallDataLiveData

    private val _startCallStateLiveData:MutableLiveData<Boolean> = MutableLiveData()
    val startCallStateLiveData:LiveData<Boolean>
    get() = _startCallStateLiveData

    val mProgressLiveData:MutableLiveData<Boolean> = MutableLiveData()
    val mStatusLiveData:MutableLiveData<String> = MutableLiveData()

    private var mUID: String? = null
    private val disposables:CompositeDisposable = CompositeDisposable()
    //endregion

    fun getUserDetailsUtils():FBUserDetails = userDetailsUtils.user!!

    fun getUserDetails(firebaseUID:String){
            disposables +=
            usersManager.getUserDataForUID(firebaseUID)
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { mProgressLiveData.postValue(true) }
                .subscribe {
                    mProgressLiveData.postValue(false)

                    when{
                        it.isSuccess ->{
                            _startCallDataLiveData.postValue(it.getTypedValue())
                            _startCallStateLiveData.postValue(true)
                        }
                        it.isFailure ->{
                            mStatusLiveData.postValue(it.getAdditionalInfo())
                        }
                    }
                }
    }

    fun sendMessage(message: Message) {
              disposables +=
              messageManager.sendMessage(message)
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .doOnSubscribe {
                    mMessageListLiveData.value?.add(message)
                    mMessageListLiveData.postValue(mMessageListLiveData.value)

                    updatePersonLastMessageTimeStamp(firebaseUID = message.toUID, message.timeStamp)
                }
                .subscribe { it: Message ->
                    mMessageListLiveData.value?.last()?.status = it.status
                    mMessageListLiveData.postValue(mMessageListLiveData.value)
                }
    }

    private fun updatePersonLastMessageTimeStamp(firebaseUID: String, timeStamp:String) = personsManager.updateLastMessageTimeStamp(firebaseUID, timeStamp)
    fun updatePersonLastMessageInfo(firebaseUID:String) = personsManager.updateLastMessageInfo(firebaseUID)


    fun observeUserDetails(firebaseUID: String):LiveData<FBUserDetails>{
       disposables +=
           usersManager.observeUserDataForUID(firebaseUID)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                if(it == null)
                    return@subscribe

                 usersManager.updateUserInfo(it)
                     .subscribe { _ ->}
                 mUserDetailsLiveData.value = it
            }


        return mUserDetailsLiveData
    }

    fun observeConnectionStatus(firebaseUID:String):LiveData<String>{
        val d:Disposable =
            connectionStatusManager.observeConnectionStatusForUID(firebaseUID.also { mUID = it })
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe{
                    if(it.status)
                       mConnectionStatusLiveData.postValue("online")
                    else{
                       val time = TimeUtils.convertTimeToString(it.timestamp.toString())
                       mConnectionStatusLiveData.postValue(time)
                    }
                }

        disposables.add(d)

       return mConnectionStatusLiveData
    }

    fun observeMessagesForUID(firebaseUID: String) : LiveData<MutableList<Message>>{
        disposables +=
            messageManager.observeMessagesForUID(firebaseUID.also { mUID = it })
            .observeOn(Schedulers.io())
            .subscribeOn(Schedulers.io())
            .subscribe {
                mProgressLiveData.postValue(false)

                when {
                    it.isSuccess -> {
                        val r: MutableList<Message> = it.getValue() as MutableList<Message>
                        mMessageListLiveData.postValue(r)
                    }
                    it.isFailure ->{
                        mStatusLiveData.postValue(it.getAdditionalInfo())
                    }
                }
            }

        return mMessageListLiveData
    }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
        connectionStatusManager.detachObserveConnectionStatusForUIDListener(mUID!!)
        messageManager.detachMessagesForUIDListener(mUID!!)
        //UsersManager.detachUserDataListener(mUID!!)
    }
}