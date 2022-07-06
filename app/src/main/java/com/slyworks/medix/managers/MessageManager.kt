package com.slyworks.medix.managers

import com.google.firebase.database.*
import com.slyworks.constants.*
import com.slyworks.data.AppDatabase
import com.slyworks.data.daos.MessageDao
import com.slyworks.data.daos.MessagePersonDao
import com.slyworks.data.daos.PersonDao
import com.slyworks.medix.App
import com.slyworks.medix.utils.MChildEventListener
import com.slyworks.medix.utils.MValueEventListener
import com.slyworks.medix.utils.UserDetailsUtils
import com.slyworks.models.models.Outcome
import com.slyworks.models.room_models.*
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged


/**
 *Created by Joshua Sylvanus, 8:33 PM, 1/8/2022.
 */

object MessageManager {
    //region Vars
    private val TAG: String? = MessageManager::class.simpleName

    private var mMessageDao: MessageDao = AppDatabase.getInstance(App.getContext()).getMessageDao()
    private var mPersonDao: PersonDao = AppDatabase.getInstance(App.getContext()).getPersonDao()

    private var mHandleChangedMessagesDisposable:CompositeDisposable = CompositeDisposable()
    private var mHandleNewMessagesDisposable: CompositeDisposable = CompositeDisposable()

    private var observeNewMessagesForUIDJob:Job? = null
    private var observeNewMessagePersonsJob:Job? = null

    private lateinit var mUIDValueEventListener: ValueEventListener
    private lateinit var mUIDChildEventListener:ChildEventListener
   //endregion

    fun detachMessagesForUIDListener(firebaseUID: String){
        observeNewMessagesForUIDJob?.cancel()
        mHandleChangedMessagesDisposable.clear()

        FirebaseDatabase.getInstance()
            .reference
            .child("messages/${UserDetailsUtils.user!!.firebaseUID}")
            .orderByChild("from_uid")
            .equalTo(firebaseUID)
            .removeEventListener(mUIDValueEventListener)
    }

    fun observeMessagesForUID(firebaseUID:String):Observable<Outcome> =
      Observable.create { emitter ->
          mUIDValueEventListener = MValueEventListener(onDataChangeFunc = ::handleChangedMessages)

          FirebaseDatabase.getInstance()
              .reference
              .child("messages/${UserDetailsUtils.user!!.firebaseUID}")
              .orderByChild("from_uid")
              .equalTo(firebaseUID)
              .addValueEventListener(mUIDValueEventListener)

          observeNewMessagesForUIDJob =
              CoroutineScope(Dispatchers.IO).launch {
                  mMessageDao
                      .observeMessagesForUID(firebaseUID)
                      .distinctUntilChanged()
                      .collectLatest {
                          if (it.isNotEmpty()) {
                              it.sort()
                              val r: Outcome = Outcome.SUCCESS<MutableList<Message>>(it)
                              emitter.onNext(r)
                          } else {
                              val r: Outcome = Outcome.FAILURE<Nothing>(reason = "no messages")
                              emitter.onNext(r)
                          }
                      }
              }
      }

    private fun handleChangedMessages(snapshot: DataSnapshot){
        val l: MutableList<Message> = mutableListOf()
        snapshot.children.forEach {
            val m: Message = it.getValue(Message::class.java)!!
            l.add(m)
        }

        if(l.isNullOrEmpty())
            return

        val d = addMessagesToDB(*l.toTypedArray())
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe{ _ -> }

        mHandleChangedMessagesDisposable.add(d)
    }

    fun detachObserveMessagePersonsListener(){
        observeNewMessagePersonsJob?.cancel()

        FirebaseDatabase.getInstance()
            .reference
            .child("messages/${UserDetailsUtils.user!!.firebaseUID}")
            .removeEventListener(mUIDChildEventListener)
    }

    fun observeMessagePersons():Observable<Outcome> =
        Observable.create { emitter ->
             mUIDChildEventListener = MChildEventListener(onChildAddedFunc = ::handleNewMessages)

             FirebaseDatabase.getInstance()
                 .reference
                 .child("messages/${UserDetailsUtils.user!!.firebaseUID}")
                 .addChildEventListener(mUIDChildEventListener)

             observeNewMessagePersonsJob =
                 CoroutineScope(Dispatchers.IO).launch {
                     mPersonDao
                         .observePersons()
                         .distinctUntilChanged()
                         .collectLatest {
                             if (it.isNotEmpty()) {
                                 val r: Outcome = Outcome.SUCCESS<MutableList<Person>>(it)
                                 emitter.onNext(r)
                             } else {
                                 val r: Outcome = Outcome.FAILURE<Nothing>(reason = "you don't seem to have any messages at the moment")
                                 emitter.onNext(r)
                             }
                         }
                 }
        }

    private fun handleNewMessages(snapshot: DataSnapshot){
        val d = getPersonSetObservable()
            .flatMap {
                getMessageListObservable(snapshot)
                    .map(::mapUIDToMessageList)
                    .map(::mapUIDMapToPersonList)
            }
            .flatMap {
                getPersonsObservable(it)
            }
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe { _ -> }

        mHandleNewMessagesDisposable.add(d)
    }

    private fun getMessageListObservable(snapshot: DataSnapshot):Observable<List<Message>> =
        Observable.create<List<Message>> { emitter ->
            val l:MutableList<Message> = mutableListOf()
            for(child in snapshot.children)
                l.add(child.getValue(Message::class.java)!!)

            emitter.onNext(l)
            emitter.onComplete()
        }

    private fun getPersonSetObservable():Observable<MutableSet<Person>> =
        Observable.create<MutableSet<Person>> { emitter ->
            CoroutineScope(Dispatchers.IO).launch {
                val s: MutableSet<Person> =
                   mPersonDao
                    .getPersons()
                    .toMutableSet()

                emitter.onNext(s)
                emitter.onComplete()
            }
        }

    private fun getPersonsObservable(personList:List<Person>):Observable<Boolean> =
        Observable.create<Boolean> { emitter ->
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    mPersonDao
                        .addPerson(*personList.toTypedArray())

                    emitter.onNext(true)
                    emitter.onComplete()
                }catch (e:Exception){
                    emitter.onNext(false)
                    emitter.onComplete()
                }
            }
        }

    private fun mapUIDToMessageList(messageList:List<Message>)
    :MutableMap<String, MutableList<Message>>{
        val sm: MutableMap<String, MutableList<Message>> = mutableMapOf()

        messageList.forEach { m ->
            val key: String =
                if (m.type == OUTGOING_MESSAGE) m.fromUID
                else m.toUID

            if (!sm.containsKey(key))
                sm.put(key, mutableListOf())

            sm.get(key)!!.add(m)
        }

        return sm
    }

    private fun mapUIDMapToPersonList(UIDMap:MutableMap<String, MutableList<Message>>)
    :List<Person> {
        var mList:MutableList<Message>
        var lastMessage:Message
        var name:String
        var unreadMessageCount:Int = 0

        val personList:MutableList<Person> = mutableListOf()
        for(i in UIDMap.keys){
            mList = UIDMap.get(i)!!
            mList.sort()

            lastMessage = mList.last()
            if(lastMessage.type == OUTGOING_MESSAGE)
                name = lastMessage.receiverFullName
            else
                name = lastMessage.senderFullName


            mList.forEach { m:Message ->
                if(m.status != READ)
                    unreadMessageCount++
            }

            personList.add( parsePerson(lastMessage, i, name, unreadMessageCount))
        }

        return personList
    }

    private fun parsePerson(m:Message,
                            UID:String,
                            name:String,
                            unreadMessageCount:Int):Person
            =  Person(
        firebaseUID = UID,
        userAccountType = m.accountType,
        lastMessageType = m.type,
        lastMessageContent = m.content,
        lastMessageStatus = m.status,
        lastMessageTimeStamp = m.timeStamp,
        senderImageUri = m.senderImageUri,
        fullName = name,
        unreadMessageCount = unreadMessageCount,
        FCMRegistrationToken = m.FCMRegistrationToken )



    fun sendMessage(message:Message): Observable<Boolean> {
        /* creating 2 copies of the messages to keep sender and receiver version different */
        //TODO: perform tests to know if this 'doubling' is necessary
        message.status = DELIVERED
        val message2: Message = Message.cloneFrom(message)

        /* if the sender's DB message node has other messages, just add a new empty node */
        val senderMessageNodeKey:String? = FirebaseDatabase.getInstance()
            .reference.child("messages/${message.fromUID}")
            .push().key

        /* if the receiver's DB message node has other messages, just add a new empty node*/
        val receiverMessageNodeKey:String? = FirebaseDatabase.getInstance()
            .reference.child("messages/${message.toUID}")
            .push().key

        getChildNodesObservable(
            message,
            message2,
            senderMessageNodeKey,
            receiverMessageNodeKey)
            .flatMap {
                addMessagesToDB(
                    if (it.isSuccess)
                        message
                    else
                        message.apply { status = NOT_SENT }
                )
            }

        /* complex RX chain that starts with checking if the sender's message node is empty
        * if it is, start by sending the message to the sender's node
        * then if the receiver's message node is empty,
        * send the message to the message node
        * if the receiver's message node is NOT empty
        * send the message to an empty created node
        * and save the sent message, if there was an error, set the Message#status to NOT_SENT
        * and add to DB, if it failed it is re-queued for later
        *
        * if both receiver and sender message nodes are NOT empty, then just perform a
        * simultaneous child node update */
        //TODO && FIXME: use Completable#andThen here
        return Observable.just(senderMessageNodeKey == null)
            .flatMap {
                if(it)
                    getSenderObservable(message)
                        .flatMap { it2 ->
                            if(it2.isSuccess)
                                Observable.just(receiverMessageNodeKey == null)
                                    .flatMap { it3 ->
                                        if(it3)
                                            getReceiverObservable(message2)
                                                .flatMap { it4 ->
                                                    addMessagesToDB(
                                                        if (it4.isSuccess)
                                                            message
                                                        else
                                                            message.apply { status = NOT_SENT }
                                                    )
                                                }
                                        else
                                            getReceiverNodeObservable(message2,receiverMessageNodeKey)
                                                .flatMap { it5 ->
                                                    addMessagesToDB(
                                                        if (it5.isSuccess)
                                                            message
                                                        else
                                                            message.apply { status = NOT_SENT }
                                                    )
                                                }
                                    }

                            else
                                Observable.just(it2)
                        }
                else
                    getChildNodesObservable(message,
                                            message2,
                                            senderMessageNodeKey,
                                            receiverMessageNodeKey)
            }
            .flatMap {
                if(it.isSuccess)
                    Observable.just(true)
                else
                    Observable.just(false)
            }
    }

    private fun getSenderObservable(message:Message):Observable<Outcome> =
        /* Observable for case when sender's message node is empty
       i.e its the very first message for the user */
        Observable.create<Outcome> { emitter ->
            val key:String? = FirebaseDatabase.getInstance()
                .reference
                .child("messages/${message.toUID}")
                .push()
                .key

            if(key == null){
                emitter.onNext(Outcome.FAILURE("created message node for sender is null"))
                emitter.onComplete()
                return@create
            }

            FirebaseDatabase.getInstance()
                .reference
                .child("messages/${UserDetailsUtils.user!!.firebaseUID}/$key")
                .setValue(message)
                .addOnCompleteListener {
                    if(it.isSuccessful)
                        emitter.onNext(Outcome.SUCCESS(null))
                    else
                        emitter.onNext(Outcome.FAILURE(null))

                    emitter.onComplete()
                }
        }

    private fun getReceiverObservable(message:Message):Observable<Outcome> =
        /* Observable for case when receiver's message node is empty
       * i.e its the very first message for the user*/
        Observable.create<Outcome> { emitter ->
            val key:String? = FirebaseDatabase.getInstance()
                .reference
                .child("messages/${message.toUID}")
                .push()
                .key

            if(key == null){
                emitter.onNext(Outcome.FAILURE("created message node for receiver is null"))
                emitter.onComplete()
                return@create
            }

            FirebaseDatabase.getInstance()
                .reference
                .child("messages/${message.toUID}/$key")
                .setValue(message)
                .addOnCompleteListener {
                    if(it.isSuccessful)
                        emitter.onNext(Outcome.SUCCESS(null))
                    else
                        emitter.onNext(Outcome.FAILURE(it.exception?.message))

                    emitter.onComplete()
                }
        }

    private fun getReceiverNodeObservable(message:Message,
                                          receiverMessageNodeKey:String?):Observable<Outcome> =
        /* Observable for case when the receiver's message node is *NOT*
        * empty, hence just add an empty node and set its value to the Message being sent*/
        Observable.create<Outcome> { emitter ->
            FirebaseDatabase.getInstance()
                .reference
                .child("messages/${message.toUID}/$receiverMessageNodeKey")
                .setValue(message)
                .addOnCompleteListener {
                    if(it.isSuccessful)
                        emitter.onNext(Outcome.SUCCESS(null))
                    else
                        emitter.onNext(Outcome.FAILURE(null))

                    emitter.onComplete()
                }
        }


    private fun getChildNodesObservable(message: Message,
                                        message2:Message,
                                        senderMessageNodeKey:String?,
                                        receiverMessageNodeKey: String?):Observable<Outcome> =
        /*
       * Observable for case when both receiver and sender message nodes are NOT
       * empty,hence perform a simultaneous child node update*/
        Observable.create<Outcome>{ emitter ->
            val childNode: HashMap<String, Any> = hashMapOf(
                "/messages/${message.fromUID}/$senderMessageNodeKey" to message,
                "/messages/${message.toUID}/$receiverMessageNodeKey" to message2
            )

            FirebaseDatabase.getInstance()
                .reference
                .updateChildren(childNode)
                .addOnCompleteListener {
                    if(it.isSuccessful)
                        emitter.onNext(Outcome.SUCCESS(null))
                    else
                        emitter.onNext(Outcome.FAILURE(null))

                    emitter.onComplete()
                }
        }

    private fun addMessagesToDB(vararg message: Message):Observable<Outcome> =
        Observable.create { emitter ->
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    mMessageDao
                        .addMessage(*message)

                    emitter.onNext(Outcome.SUCCESS(null))
                    emitter.onComplete()
                }catch (e:Exception){
                    emitter.onNext(Outcome.FAILURE(null, reason = e.message))
                    emitter.onComplete()
                }
            }
        }
}