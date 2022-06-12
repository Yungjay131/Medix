package com.slyworks.medix.ui.activities.messageActivity

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slyworks.constants.EVENT_INCOMING_VIDEO_CALL
import com.slyworks.constants.EVENT_LISTEN_FOR_CONSULTATION_REQUESTS
import com.slyworks.constants.EVENT_NEW_MESSAGE_RECEIVED
import com.slyworks.medix.*
import com.slyworks.medix.AppController.clearAndRemove
import com.slyworks.models.models.ConsultationRequest
import com.slyworks.models.models.Observer
import com.slyworks.models.room_models.Message
import com.slyworks.models.room_models.FBUserDetails
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


/**
 *Created by Joshua Sylvanus, 12:27 PM, 1/13/2022.
 */
class MessageViewModel : ViewModel(), Observer {
    //region Vars
    private var mUID: String? = null
    private val mSubscriptionList: MutableList<Subscription> = mutableListOf()
    private val mSubscriptions:CompositeDisposable = CompositeDisposable()

    private val mMessageListLiveData: MutableLiveData<MutableList<Message>> = MutableLiveData(mutableListOf())
    private val mIncomingVideoCallUpdate: MutableLiveData<FBUserDetails> = MutableLiveData()
    private val mNewConsultationRequest: MutableLiveData<FBUserDetails> = MutableLiveData()
    private val mNewMessageUpdate: MutableLiveData<Boolean> = MutableLiveData(false)
    private val mConnectionStatusLiveData:MutableLiveData<String> = MutableLiveData()
    private val mUserDetailsLiveData:MutableLiveData<FBUserDetails> = MutableLiveData()

    private var mDataManager: DataManager? = null
    private var mMessageManager: MessageManager? = null
    //endregion


    init {
        mDataManager = DataManager()
    }

    private fun subscribeToUpdates() {
        AppController.addEvent(EVENT_INCOMING_VIDEO_CALL)
        val subscription_1: Subscription = AppController.subscribeTo(EVENT_INCOMING_VIDEO_CALL, this)

        AppController.addEvent(EVENT_NEW_MESSAGE_RECEIVED)
        val subscription_2: Subscription = AppController.subscribeTo(EVENT_NEW_MESSAGE_RECEIVED, this)

        AppController.addEvent(EVENT_LISTEN_FOR_CONSULTATION_REQUESTS)
        val subscription_3: Subscription = AppController.subscribeTo(EVENT_LISTEN_FOR_CONSULTATION_REQUESTS, this)

        mSubscriptionList.add(subscription_1)
        mSubscriptionList.add(subscription_2)
        mSubscriptionList.add(subscription_3)
    }

    fun sendMessage(message: Message) {
        viewModelScope.launch(Dispatchers.IO) {
            mDataManager!!.sendMessage(message)
            mMessageListLiveData.value?.add(message)
            mMessageListLiveData.setValue(mMessageListLiveData.value)
        }
    }

    fun observeUserDetails(firebaseUID: String):LiveData<FBUserDetails>{
        UsersManager.getUserDataForUID(firebaseUID)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                 UsersManager.updateUserInfo(it)
                 mUserDetailsLiveData.value = it
            }

        return mUserDetailsLiveData
    }

    fun observeConnectionStatus(firebaseUID:String):LiveData<String>{
        val d:Disposable =
            mDataManager!!.observeUserConnectionStatus(firebaseUID)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe{
                    mConnectionStatusLiveData.postValue(it)
                }

       return mConnectionStatusLiveData
    }

    fun observeMessagesForUID(firebaseUID: String) : LiveData<MutableList<Message>>{
        mUID = firebaseUID
        mDataManager?.observeMessagesFromUIDFromFB(firebaseUID)

        val d1: Disposable =
            mDataManager!!.getMessagesForUIDFromDB(firebaseUID)
            .observeOn(Schedulers.io())
            .subscribeOn(Schedulers.io())
            .subscribe {
                when {
                    it.isSuccess -> {
                        val r: MutableList<Message> = it.getValue() as MutableList<Message>
                        mMessageListLiveData.postValue(r)
                    }
                }
            }

        mSubscriptions.add(d1)

        return mMessageListLiveData
    }

    fun getMessagesForUID(context: Context,
                          firebaseUID: String) {
        mMessageManager = MessageManager(context)
        mMessageManager!!.getMessagesForUID(firebaseUID)
        mMessageManager!!.addListenerForUID(firebaseUID)
        mUID = firebaseUID

        viewModelScope.launch {
            mMessageManager!!.observeMessagesForUID(firebaseUID)
                .collectLatest {
                    mMessageListLiveData.value = it
                }
        }

    }

    override fun <T> notify(event: String, data: T?) {
        when (event) {
            EVENT_INCOMING_VIDEO_CALL -> {
                mIncomingVideoCallUpdate.postValue(data as FBUserDetails)
            }
            EVENT_NEW_MESSAGE_RECEIVED -> {
                mNewMessageUpdate.postValue(true)
            }
            EVENT_LISTEN_FOR_CONSULTATION_REQUESTS -> {
                val result: FBUserDetails = (data as ConsultationRequest).details
                mNewConsultationRequest.postValue(result)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        mSubscriptionList.forEach { it.clearAndRemove() }
        mSubscriptions.clear()
        mDataManager = null

        /*mDataManager?.detachUserConnectionStatusListener(mUID!!)
        mDataManager?.detachMessagesFromUIDFromDBListener(mUID!!)*/

    }
}