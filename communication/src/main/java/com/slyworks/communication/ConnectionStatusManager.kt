package com.slyworks.communication

import android.annotation.SuppressLint
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.slyworks.firebase_commons.MValueEventListener
import com.slyworks.room.daos.PersonDao
import com.slyworks.models.models.ConnectionStatus
import com.slyworks.models.room_models.Person
import com.slyworks.userdetails.UserDetailsUtils
import io.reactivex.rxjava3.core.Observable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject


/**
 *Created by Joshua Sylvanus, 8:42 AM, 28/06/2022.
 */
class ConnectionStatusManager(
    private val personDao: PersonDao,
    private val firebaseDatabase: FirebaseDatabase,
    private val userDetailsUtils: UserDetailsUtils) {

    //region Vars
    private lateinit var mConnectionRefValueEventListener: ValueEventListener
    private lateinit var mUIDConnectionRefValueEventListener: ValueEventListener
    //endregion

    fun setMyConnectionStatusHandler():Observable<Unit>{
       return Observable.create<Unit> {_ ->
            mConnectionRefValueEventListener =
                MValueEventListener(onDataChangeFunc = ::handleConnectionStatusChange)

            firebaseDatabase
                .reference
                .child(".info/connected")
                .addValueEventListener(mConnectionRefValueEventListener)
        }
    }

    private fun handleConnectionStatusChange(snapshot: DataSnapshot){
        val isConnected:Boolean = snapshot.getValue(Boolean::class.java) ?: false
        if(isConnected){
            /*add a new node representing a new device*/
            val node = firebaseDatabase
                .reference
                .child("connections/${userDetailsUtils.user!!.firebaseUID}")
                .push()

            /*when this device disconnects remove it*/
            node.onDisconnect().removeValue()

            /*also update the last time i was seen*/
            firebaseDatabase
                .reference
                .child("last_online/${userDetailsUtils.user!!.firebaseUID}")
                .onDisconnect()
                .updateChildren(
                    mapOf(
                        "/status" to false,
                        "/timestamp" to ServerValue.TIMESTAMP ))

            /*change my current connection status to connected*/
            firebaseDatabase
                .reference
                .child("last_online/${userDetailsUtils.user!!.firebaseUID}/status")
                .setValue(true)

            /*add this device to my connections list*/
            node.setValue(true)
        }
    }

    fun detachObserveConnectionStatusForUIDListener(firebaseUID: String){
        firebaseDatabase
            .reference
            .child("last_online/$firebaseUID")
            .removeEventListener(mUIDConnectionRefValueEventListener)
    }

    fun observeConnectionStatusForUID(firebaseUID:String):Observable<ConnectionStatus> =
        Observable.create { emitter ->
            mUIDConnectionRefValueEventListener =
               MValueEventListener(onDataChangeFunc = {
                    val cs = it.getValue(ConnectionStatus::class.java)!!

                    CoroutineScope(Dispatchers.IO).launch {
                        val person: Person =
                            personDao.getPersonByID(firebaseUID) ?: return@launch

                        person.lastMessageTimeStamp = cs.timestamp.toString()

                        personDao.updatePerson(person)
                    }

                    emitter.onNext(cs)
                })

            firebaseDatabase
                .reference
                .child("last_online/$firebaseUID")
                .addValueEventListener(mUIDConnectionRefValueEventListener)
        }
}