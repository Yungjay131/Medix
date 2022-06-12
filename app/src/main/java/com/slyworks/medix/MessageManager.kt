package com.slyworks.medix

import android.content.Context
import android.util.Log
import com.google.firebase.database.*
import com.slyworks.constants.*
import com.slyworks.data.AppDatabase
import com.slyworks.data.daos.MessageDao
import com.slyworks.data.daos.MessagePersonDao
import com.slyworks.models.room_models.*
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.asDeferred
import java.util.concurrent.atomic.AtomicReference


/**
 *Created by Joshua Sylvanus, 8:33 PM, 1/8/2022.
 */

class MessageManager(private var context: Context) {
    //region Vars
    private val TAG: String? = MessageManager::class.simpleName

    private var mFirebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
    private var mMessageDao: MessageDao = AppDatabase.getInstance(context).getMessageDao()
    private var mMessagePersonDao: MessagePersonDao = AppDatabase.getInstance(context).getMessagePersonDao()

    private val mListenerMap:MutableMap<String, ChildEventListener> = mutableMapOf()

    private val o:PublishSubject<Boolean> = PublishSubject.create()
    //endregion

    private val mSingleUIDNewMessageChildEventListener:ChildEventListener = object : ChildEventListener{
        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
        override fun onChildRemoved(snapshot: DataSnapshot) {}
        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
        override fun onCancelled(error: DatabaseError) {}
        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
            //new message
            val message: Message = snapshot.getValue(Message::class.java)!!
            if(message.type == OUTGOING_MESSAGE) return

            addMessageToDB(message)
            addMessagePersonToDB(mapMessageToMessagePerson(message, INCOMING_MESSAGE))
        }
    }

    private val mNewMessageChildEventListener:ChildEventListener =  object :ChildEventListener{
        override fun onChildRemoved(snapshot: DataSnapshot) {}
        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
        override fun onCancelled(error: DatabaseError) {}
        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
            val lastMessage: Message = snapshot.children.last().getValue(Message::class.java)!!
            if(lastMessage.type == OUTGOING_MESSAGE) return

            //AppController.notifyObservers(EVENT_NEW_MESSAGE_RECEIVED, null )
            o.onNext(true)
        }

        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
            val lastMessage: Message = snapshot.children.last().getValue(Message::class.java)!!
            if(lastMessage.type == OUTGOING_MESSAGE) return

            // AppController.notifyObservers(EVENT_NEW_MESSAGE_RECEIVED, null )
        }
    }
    fun listenForNewMessages(firebaseUID: String = UserDetailsUtils.user!!.firebaseUID){
        mFirebaseDatabase.reference
            .child("users")
            .child(firebaseUID)
            .child("messages")
            .addChildEventListener(mNewMessageChildEventListener)
    }

    fun listenForNewMessages2(firebaseUID: String = UserDetailsUtils.user!!.firebaseUID): Observable<Boolean>{
        listenForNewMessages(firebaseUID)
        return o.hide()
    }

    fun observeNewMessages(){
        mFirebaseDatabase.reference
            .child("users")
            .child(UserDetailsUtils.user!!.firebaseUID)
            .child("messages")
            .addChildEventListener(object: ChildEventListener{
                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onChildRemoved(snapshot: DataSnapshot) {}
                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onCancelled(error: DatabaseError) {}
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    CoroutineScope(Dispatchers.IO).launch {
                        //person with list of messages
                        val messagePerson: MessagePerson =
                            MessagePerson(firebaseUID = snapshot.key!!)
                        val list:MutableList<Message> = mutableListOf()
                        snapshot.children.forEach{
                            val message: Message = it.getValue(Message::class.java)!!
                            list.add(message)
                        }
                        //would trigger getAllMessagePersonWithMessages()???
                        //maybe if it fro service drop notification
                        mMessagePersonDao.addMessagePerson(messagePerson)
                        mMessageDao.addMessage(*list.toTypedArray())
                    }
                }
            })
    }




    //measureTimeInMillis{}
    fun getAllMessagePersonWithMessages() {
            val mpwmList: MutableList<MessagePersonWithMessages> = mutableListOf()

            var status:Boolean = false
            var message:String? = "an error occurred retrieving messages"
            var triple:Triple<Boolean, String, MutableList<MessagePersonWithMessages>?>

            mFirebaseDatabase.reference
                .child("users")
                .child(UserDetailsUtils.user!!.firebaseUID)
                .child("messages")
                .get()
                .addOnCompleteListener { mpwmL ->
                    CoroutineScope(Dispatchers.IO).launch outer_launch@{
                        if(!mpwmL.isSuccessful){
                            triple = Triple(false, "retrieving messages did not complete", null)
                            AppController.notifyObservers(EVENT_GET_ALL_MESSAGES, triple)
                            return@outer_launch
                        }

                        //means here onwards it was successful
                        if (mpwmL.result?.getValue() == null){
                            triple = Triple(true, "you have no messages at this time", null)
                            AppController.notifyObservers(EVENT_GET_ALL_MESSAGES, triple)
                            return@outer_launch
                        }

                            mpwmL.result!!.children.forEach { it ->
                                val firebaseUID: String = it.key!!
                                val messagePerson: MessagePerson =
                                    MessagePerson(firebaseUID)
                                val details:AtomicReference<FBUserDetails?> = AtomicReference(null)
                                val messageDetails: MessageDetails
                                val messageList: MutableList<Message> = mutableListOf()

                                val result = mFirebaseDatabase.reference
                                            .child("users")
                                            .child(firebaseUID)
                                            .child("details")
                                            .get()
                                            .asDeferred()
                                            .await()

                                message = "error occurred getting a message person's details"

                                details.set(result.getValue(FBUserDetails::class.java))
                                if (details.get() == null){
                                    triple = Triple(false, message!!, null)
                                    AppController.notifyObservers(EVENT_GET_ALL_MESSAGES, triple)
                                    return@outer_launch
                                }

                                it.children.forEach { it3 ->
                                    val _message: Message = it3.getValue(Message::class.java)!!
                                    messageList.add(_message)
                                }

                                val lastMessage: Message = messageList.last()
                                val _details: FBUserDetails = details.get()!!
                                messageDetails = MessageDetails(
                                    userAccountType = _details.accountType,
                                    lastMessageType = lastMessage.type,
                                    lastMessageContent = lastMessage.content,
                                    lastMessageStatus = lastMessage.status,
                                    lastMessageTimeStamp = lastMessage.timeStamp,
                                    senderImageUri = _details.imageUri,
                                    fullName = _details.fullName
                                )

                                val mpwm: MessagePersonWithMessages = MessagePersonWithMessages(
                                    person = messagePerson,
                                    details = messageDetails,
                                    messages = messageList
                                )

                                mpwmList.add(mpwm)

                                mMessageDao.addMessage(*messageList.toTypedArray())
                                mMessagePersonDao.addMessagePerson(messagePerson)
                            }

                            triple = Triple(true, "messages successfully retrieved", mpwmList)
                        AppController.notifyObservers(EVENT_GET_ALL_MESSAGES, triple)
                    }
                }

       }

    fun observeMessagesForUID(firebaseUID:String): Flow<MutableList<Message>> {
        return mMessageDao.observeMessagesForUID(firebaseUID)
    }

    fun addListenerForUID(firebaseUID: String){
        val listener:ChildEventListener = mFirebaseDatabase.reference
            .child("users")
            .child(UserDetailsUtils.user!!.firebaseUID)
            .child("messages")
            .child(firebaseUID)
            .addChildEventListener(mSingleUIDNewMessageChildEventListener)

        mListenerMap.put(firebaseUID, listener)
    }

    fun getMessagesForUID(firebaseUID: String){
        mFirebaseDatabase.reference
            .child("users")
            .child(UserDetailsUtils.user!!.firebaseUID)
            .child("messages")
            .child(firebaseUID)
            .get()
            .addOnCompleteListener {
                if(it.isSuccessful){
                    if (it.result?.getValue() == null) return@addOnCompleteListener

                    val list:MutableList<Message> = mutableListOf()
                    it.result!!.children.forEach { data ->
                        val message: Message = data.getValue(Message::class.java)!!
                        list.add(message)
                    }

                    addMessageToDB(*list.toTypedArray())
                }
            }
    }

    fun sendMessage(message: Message) {
        message.status = DELIVERED

        val message2: Message = message.copy()
        message2.type = INCOMING_MESSAGE

        val senderMessageNodeKey = mFirebaseDatabase.reference
            .child("users")
            .child(UserDetailsUtils.user!!.firebaseUID)
            .child("messages")
            .child(message.toUID)
            .push()
            .key

        val receiverMessageNodeKey = mFirebaseDatabase.reference
            .child("users")
            .child(message.toUID)
            .child("messages")
            .child(message.fromUID)
            .push()
            .key

        if(senderMessageNodeKey == null || receiverMessageNodeKey == null){
            //first messages do manually
         mFirebaseDatabase.reference
                .child("users")
                .child(UserDetailsUtils.user!!.firebaseUID)
                .child("messages")
                .child(message.toUID)
                .setValue(message)
                .continueWith {
                     mFirebaseDatabase.reference
                        .child("users")
                        .child(message.toUID)
                        .child("messages")
                        .child(message.fromUID)
                        .setValue(message2)
                }.addOnCompleteListener {
                 if(it.isSuccessful){
                     addMessageToDB(message)
                     addMessagePersonToDB(mapMessageToMessagePerson(message, OUTGOING_MESSAGE))
                 }else{
                     addMessageToDB(message)
                     addMessagePersonToDB(mapMessageToMessagePerson(message, OUTGOING_MESSAGE))
                 }
             }.addOnFailureListener {
                 Log.e(TAG, "sendMessage: sending message to Firebase DB failed", it )
             }

            return
        }

        val childNode:HashMap<String, Any> = hashMapOf(
            "/users/${message.fromUID}/messages/${message.toUID}/$senderMessageNodeKey" to message,
            "/users/${message.toUID}/messages/${message.fromUID}/$receiverMessageNodeKey" to message2)

        mFirebaseDatabase.reference
            .updateChildren(childNode)
            .addOnCompleteListener {
               if(it.isSuccessful){
                   message.status = DELIVERED
                   addMessageToDB(message)
                   if(message.fromUID != UserDetailsUtils.user!!.firebaseUID)
                      addMessagePersonToDB(mapMessageToMessagePerson(message, OUTGOING_MESSAGE))
               }else{
                   message.status = NOT_SENT
                   addMessageToDB(message)

                   if(message.fromUID != UserDetailsUtils.user!!.firebaseUID)
                      addMessagePersonToDB(mapMessageToMessagePerson(message, OUTGOING_MESSAGE))
               }
            }.addOnFailureListener {
                Log.e(TAG, "sendMessage: sending message to Firebase DB failed", it )
            }
    }
    fun mapMessageToMessagePerson(message: Message, type:String): MessagePerson {
        var messagePerson: MessagePerson? = null
        when(type){
            OUTGOING_MESSAGE ->
                messagePerson = MessagePerson(message.toUID)
            INCOMING_MESSAGE ->
                messagePerson = MessagePerson(message.fromUID)

        }

        return messagePerson!!
    }
    fun detachListenerForUID(firebaseUID: String){
        mListenerMap[firebaseUID] ?: return

        mFirebaseDatabase.reference
            .child("users")
            .child(UserDetailsUtils.user!!.firebaseUID)
            .child("messages")
            .child(firebaseUID)
            .removeEventListener(mListenerMap[firebaseUID]!!)
    }
    fun addMessagePersonToDB(messagePerson: MessagePerson){
        CoroutineScope(Dispatchers.IO).launch {
            mMessagePersonDao.addMessagePerson(messagePerson)
        }
    }

    fun addMessageToDB(vararg message: Message){
        CoroutineScope(Dispatchers.IO).launch {
            mMessageDao.addMessage(*message)
        }
    }


}