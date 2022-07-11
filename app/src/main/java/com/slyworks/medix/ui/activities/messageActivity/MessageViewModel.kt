package com.slyworks.medix.ui.activities.messageActivity

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.slyworks.medix.managers.*
import com.slyworks.models.models.Observer
import com.slyworks.models.room_models.Message
import com.slyworks.models.room_models.FBUserDetails
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers


/**
 *Created by Joshua Sylvanus, 12:27 PM, 1/13/2022.
 */
class MessageViewModel : ViewModel(), Observer {
    //region Vars
    private var mUID: String? = null
    private val mSubscriptions:CompositeDisposable = CompositeDisposable()

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
    //endregion

    fun getUserDetails(firebaseUID:String){
        val d =
            UsersManager.getUserDataForUID(firebaseUID)
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .doOnSubscribe {
                    mProgressLiveData.postValue(true)
                }
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
          val d =
              MessageManager.sendMessage(message)
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .doOnSubscribe {
                    mMessageListLiveData.value?.add(message)
                    mMessageListLiveData.postValue(mMessageListLiveData.value)

                    updatePersonLastMessageTimeStamp(firebaseUID = message.toUID, message.timeStamp)
                }
                .subscribe { it:Message ->
                    mMessageListLiveData.value?.last()?.status = it.status
                    mMessageListLiveData.postValue(mMessageListLiveData.value)
                }

        mSubscriptions.add(d)
    }

    fun updatePersonLastMessageTimeStamp(firebaseUID: String, timeStamp:String) = PersonsManager.updateLastMessageTimeStamp(firebaseUID, timeStamp)
    fun updatePersonLastMessageInfo(firebaseUID:String) = PersonsManager.updateLastMessageInfo(firebaseUID)


    fun observeUserDetails(firebaseUID: String):LiveData<FBUserDetails>{
       val d =
           UsersManager.observeUserDataForUID(firebaseUID)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                if(it == null)
                    return@subscribe

                 UsersManager.updateUserInfo(it)
                     .subscribe { _ ->}
                 mUserDetailsLiveData.value = it
            }

        mSubscriptions.add(d)

        return mUserDetailsLiveData
    }

    fun observeConnectionStatus(firebaseUID:String):LiveData<String>{
        val d:Disposable =
            ConnectionStatusManager.observeConnectionStatusForUID(firebaseUID.also { mUID = it })
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

        mSubscriptions.add(d)

       return mConnectionStatusLiveData
    }

    fun observeMessagesForUID(firebaseUID: String) : LiveData<MutableList<Message>>{
        val d: Disposable =
            MessageManager.observeMessagesForUID(firebaseUID.also { mUID = it })
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

        mSubscriptions.add(d)

        return mMessageListLiveData
    }

    override fun onCleared() {
        super.onCleared()
        mSubscriptions.clear()
        ConnectionStatusManager.detachObserveConnectionStatusForUIDListener(mUID!!)
        MessageManager.detachMessagesForUIDListener(mUID!!)
        //UsersManager.detachUserDataListener(mUID!!)
    }
}