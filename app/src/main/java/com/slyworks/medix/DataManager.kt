package com.slyworks.medix

import android.util.Log
import androidx.collection.SimpleArrayMap
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.slyworks.constants.*
import com.slyworks.medix.utils.TimeUtils
import com.slyworks.data.AppDatabase
import com.slyworks.models.models.Outcome
import com.slyworks.models.room_models.Message
import com.slyworks.models.room_models.Person
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest


class DataManager{
    //region Vars
    private val TAG: String? = DataManager::class.simpleName

    private val mListenersMap:SimpleArrayMap<String, ValueEventListener> = SimpleArrayMap()
    private val mObserversMap:SimpleArrayMap<String, Observable<String>> = SimpleArrayMap()

    private var handleNewMessagesJob:Job? = null
    private var observeNewMessagePersonsJob:Job? = null

    /*fixme:watch this for memory leaks*/
    private var mUIDValueEventListener:ValueEventListener =
        MValueEventListener(onDataChangeFunc = ::handleChangedMessages)

    private var mUIDChildEventListener:ChildEventListener =
        MChildEventListener(onChildAddedFunc = ::handleNewMessages)

    private var mConnectionStatusValueEventListener:ValueEventListener =
        MValueEventListener(onDataChangeFunc = ::handleConnectionStatusChanged)
    //endregion

    private fun handleChangedMessages(snapshot: DataSnapshot) {
            val l: MutableList<Message> = mutableListOf()
            snapshot.children.forEach {
                val m: Message = it.getValue(Message::class.java)!!
                l.add(m)
            }

            addMessagesToDB(*l.toTypedArray())
    }


    private fun handleNewMessages(snapshot: DataSnapshot){
    handleNewMessagesJob =
        CoroutineScope(Dispatchers.IO).launch {
      val l:MutableList<Message> = mutableListOf()
        for (child in snapshot.children) {
            val m: Message = child.getValue(Message::class.java)!!
            l.add(m)
        }

        addMessagesToDB(*l.toTypedArray())

        /*getting persons from the list of messages*/
        /*using HashSet to ensure nothing is entered twice*/
        val l2:HashSet<Person> =
            AppDatabase.getInstance(App.getContext())
                .getPersonDao()
                .getPersons()
                .toHashSet()

        val l3:HashMap<String, MutableList<Message>> = HashMap()
        l.forEach {
                val key:String = if(it.type == OUTGOING_MESSAGE) it.fromUID else it.toUID
                if(!l3.containsKey(key)){
                    l3.put(key, mutableListOf())
                }

                l3.get(key)!!.add(it)
            }

            with(l3){
                keys.forEach{
                    val pList:MutableList<Message> = get(it)!!
                    /*sort by timestamp???*/
                    pList.sort()
                    val m: Message = pList.last()
                    val uid:String
                    val name:String
                    var unreadMessageCount:Int = 0
                    if(m.type == OUTGOING_MESSAGE){
                        uid = m.fromUID
                        name = m.receiverFullName
                    } else{
                        uid = m.toUID
                        name = m.senderFullName
                    }

                    pList.forEach{ it2 ->
                        if(it2.status != READ)
                            unreadMessageCount++
                    }

                    val p: Person =
                        Person(
                            firebaseUID = it,
                            userAccountType = m.accountType,
                            lastMessageType = m.type,
                            lastMessageContent = m.content,
                            lastMessageStatus = m.status,
                            lastMessageTimeStamp = m.timeStamp,
                            senderImageUri = m.senderImageUri,
                            fullName = name,
                            unreadMessageCount = unreadMessageCount,
                            FCMRegistrationToken = m.FCMRegistrationToken
                        )

                    /*add to set*/
                    l2.add(p)
                }
            }


            AppDatabase.getInstance(App.getContext())
                .getPersonDao()
                .addPerson(*l2.toTypedArray())
        }

    }

    private fun handleConnectionStatusChanged(snapshot: DataSnapshot){
        /*  Since users can connect from multiple devices, i store each connection instance separately
                 ny time that connectionsRef's value is null (i.e. has no children) i am offline*/
        val userConnectionsRef = FirebaseDatabase.getInstance()
            .getReference("connections/${UserDetailsUtils.user!!.firebaseUID}/connections")

        /*stores the timestamp of users last disconnect,(last time user was online)*/
        val lastOnlineRef = Firebase.database
            .getReference("connections/${UserDetailsUtils.user!!.firebaseUID}/last_online_timestamp")


        val connectionStatus:Boolean = snapshot.getValue(Boolean::class.java) ?: false

        if(connectionStatus){
            val newConnectionKey:DatabaseReference = userConnectionsRef.push()

            /*add this device to my connections list*/
            newConnectionKey.setValue(ServerValue.TIMESTAMP)

            /*when this device disconnects remove it*/
            newConnectionKey.onDisconnect().removeValue()

            lastOnlineRef.setValue("online")

            /*when this device disconnects update the last time i was seen online*/
            lastOnlineRef.onDisconnect()
                .setValue(ServerValue.TIMESTAMP)
        }
    }

    private fun connectionStatusListener(firebaseUID:String,
                                         o:PublishSubject<String>):ValueEventListener{

        val l = MValueEventListener(
            onDataChangeFunc =  { snapshot ->
                var connectionStatus:String = snapshot.getValue(String::class.java) ?: ""
                if(connectionStatus != "" || connectionStatus != "online"){
                    /*its in millis since epoch, convert to readable time*/
                    connectionStatus = TimeUtils.convertTimeToString(connectionStatus)
                }

                o.onNext(connectionStatus)
            })

        mListenersMap.put(firebaseUID,l)
        mObserversMap.put(firebaseUID,o)

        return l
    }
    fun handlePresence(){
        FirebaseDatabase.getInstance()
                        .getReference(".info/connected")
                        .addValueEventListener(mConnectionStatusValueEventListener);
    }

    fun detachPresenceListener(){
        FirebaseDatabase.getInstance()
            .getReference(".info/connected")
            .removeEventListener(mConnectionStatusValueEventListener)
    }

    fun observeUserConnectionStatus(firebaseUID: String): Observable<String> {
       val o:PublishSubject<String>? = PublishSubject.create()

       FirebaseDatabase.getInstance()
           .getReference("connections/${firebaseUID}/last_online_timestamp")
           .addValueEventListener(connectionStatusListener(firebaseUID, o!!))

        return o.hide()
    }

    fun detachUserConnectionStatusListener(firebaseUID: String){
        val l:ValueEventListener = mListenersMap[firebaseUID] ?: return

        FirebaseDatabase.getInstance()
            .getReference("connections/${firebaseUID}/last_online_timestamp")
            .removeEventListener(l)

        var o:Observable<String>? = mObserversMap[firebaseUID] ?: return
        o = null
    }

    /*fixme:move to a background thread*/
    fun observeMessagePersonsFromFB():Observable<Outcome>{
        FirebaseDatabase.getInstance()
            .reference
            .child("messages")
            .child(UserDetailsUtils.user!!.firebaseUID)
            .addChildEventListener(mUIDChildEventListener)

        val o:Observable<Outcome> = Observable.create { emitter ->
            observeNewMessagePersonsJob =
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        AppDatabase.getInstance(App.getContext())
                            .getPersonDao()
                            .observePersons()
                            .collectLatest {
                                if (it.isNotEmpty()) {
                                    val r: Outcome = Outcome.SUCCESS<MutableList<Person>>(it)
                                    emitter.onNext(r)
                                } else {
                                    val r: Outcome = Outcome.FAILURE<Nothing>(reason = "you don't seem to have any messages at the moment")
                                    emitter.onNext(r)
                                }
                            }
                    } catch (e: Exception) {
                        val r:Outcome = Outcome.ERROR<Nothing>(error = e)
                        emitter.onNext(r)
                    }
                }
        }

        return o
    }

    fun detachMessagesPersonsFromFBListener(){
        FirebaseDatabase.getInstance()
            .reference
            .child("messages")
            .child(UserDetailsUtils.user!!.firebaseUID)
            .removeEventListener(mUIDChildEventListener)

        observeNewMessagePersonsJob!!.cancel()
    }


    fun observeMessagesFromUIDFromFB(firebaseUID: String){
        FirebaseDatabase.getInstance()
            .getReference("messages/${UserDetailsUtils.user!!.firebaseUID}")
            .orderByChild("from_uid")
            .equalTo(firebaseUID)
            .addChildEventListener(mUIDChildEventListener)
    }

    fun detachMessagesFromUIDFromDBListener(firebaseUID: String){
        FirebaseDatabase.getInstance()
            .getReference("messages/${UserDetailsUtils.user!!.firebaseUID}")
            .orderByChild("from_uid")
            .equalTo(firebaseUID)
            .removeEventListener(mUIDChildEventListener)
    }

    fun observeMessagesForUIDFromFirebase(){
            try {
                    getUserMessagesRef()
                    .addValueEventListener(mUIDValueEventListener)
            }catch (e:Exception){
                //TODO:figure out what to do when an exception is thrown
                Log.e(TAG, "observeMessagesForUIDFromFirebase: error occurred",e )
            }
    }

    fun detachListenersForUID(){
        getUserMessagesRef()
            .removeEventListener(mUIDValueEventListener)
    }

    fun getMessagesForUIDFromDB(firebaseUID:String): Observable<Outcome> {
        val o:Observable<Outcome> = Observable.create { emitter ->
            CoroutineScope(Dispatchers.IO).launch {
                AppDatabase.getInstance(App.getContext())
                    .getMessageDao()
                    .observeMessagesForUID(firebaseUID)
                    .collectLatest {
                        val r:Outcome = Outcome.SUCCESS(it)
                        emitter.onNext(r)
                    }
            }
        }

        return o
    }

    suspend fun sendMessage(message: Message):Boolean{
        var status:Boolean = false
        val job:Deferred<Boolean> = CoroutineScope(Dispatchers.IO).async {
            val childJob:Deferred<Unit> = CoroutineScope(Dispatchers.IO).async inner_async@{


                message.status = DELIVERED
                val message2: Message =
                    Message(
                        type = INCOMING_MESSAGE,
                        fromUID = message.fromUID,
                        toUID = message.toUID,
                        senderFullName = message.senderFullName,
                        receiverFullName = message.receiverFullName,
                        content = message.content,
                        timeStamp = message.timeStamp,
                        messageID = message.messageID,
                        status = message.status,
                        senderImageUri = message.senderImageUri,
                        accountType = message.accountType
                    )


                val senderMessageNodeKey:String? = FirebaseDatabase.getInstance()
                    .reference
                    .child("messages")
                    .child(message.fromUID)
                    .push()
                    .key

                val receiverMessageNodeKey:String? = FirebaseDatabase.getInstance()
                    .reference
                    .child("messages")
                    .child(message.toUID)
                    .push()
                    .key

                if(senderMessageNodeKey == null){
                    /*first ever messages, add 'manually'*/
                    FirebaseDatabase.getInstance()
                        .reference
                        .child("messages")
                        .child(UserDetailsUtils.user!!.firebaseUID)
                        .setValue(message)
                        .addOnCompleteListener {
                            if(it.isSuccessful){
                                if(receiverMessageNodeKey == null){
                                        FirebaseDatabase.getInstance()
                                            .reference
                                            .child("messages")
                                            .child(message.toUID)
                                            .setValue(message2)
                                            .addOnCompleteListener{ it2 ->
                                                if(it2.isSuccessful){
                                                    addMessagesToDB(message)
                                                    status = true
                                                }else{
                                                    message.status = NOT_SENT
                                                    addMessagesToDB(message)
                                                }
                                            }
                                }else{
                                    FirebaseDatabase.getInstance()
                                        .reference
                                        .child("messages")
                                        .child(message.toUID)
                                        .child(receiverMessageNodeKey)
                                        .setValue(message2)
                                        .addOnCompleteListener { it3->
                                            if(it3.isSuccessful){
                                                addMessagesToDB(message)
                                                status = true
                                            }else{
                                                message.status = NOT_SENT
                                                addMessagesToDB(message)
                                            }
                                        }
                                }
                            }else{
                                message.status = NOT_SENT
                                addMessagesToDB(message)
                            }

                            this@inner_async.cancel()
                        }
                }else {

                    val childNode: HashMap<String, Any> = hashMapOf(
                        "/messages/${message.fromUID}/$senderMessageNodeKey" to message,
                        "/messages/${message.toUID}/$receiverMessageNodeKey" to message2
                    )

                    FirebaseDatabase.getInstance()
                        .reference
                        .updateChildren(childNode)
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                addMessagesToDB(message)
                                status = true
                            } else {
                                message.status = NOT_SENT
                                addMessagesToDB(message)
                            }

                            this@async.cancel()
                        }
                }
            }

            /*to prevent premature cancellation of the coroutine when the completeListener is not done*/
            childJob.await()

            /*closing the outer launch coroutine, assuming at this point that everything has been executed*/
            return@async status
        }


       return job.await()
    }

    private fun addMessagesToDB(vararg message: Message){
        CoroutineScope(Dispatchers.IO).launch {
            AppDatabase.getInstance(App.getContext())
                .getMessageDao()
                .addMessage(*message)
        }
    }


    fun cleanup(firebaseUID: String? = null){}
}