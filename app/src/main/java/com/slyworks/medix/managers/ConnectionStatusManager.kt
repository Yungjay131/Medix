package com.slyworks.medix.managers

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.slyworks.data.AppDatabase
import com.slyworks.data.daos.PersonDao
import com.slyworks.medix.App
import com.slyworks.medix.utils.MValueEventListener
import com.slyworks.medix.utils.UserDetailsUtils
import com.slyworks.models.models.ConnectionStatus
import com.slyworks.models.room_models.Person
import io.reactivex.rxjava3.core.Observable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


/**
 *Created by Joshua Sylvanus, 8:42 AM, 28/06/2022.
 */
object ConnectionStatusManager {
    //region Vars
    private val mPersonDao:PersonDao = AppDatabase.getInstance(App.getContext()).getPersonDao()

    private lateinit var mConnectionRefValueEventListener: ValueEventListener
    private lateinit var mUIDConnectionRefValueEventListener: ValueEventListener
    //endregion

    fun setMyConnectionStatusHandler():Observable<Unit>{
       return Observable.create<Unit> {_ ->
            mConnectionRefValueEventListener =
                MValueEventListener(onDataChangeFunc = ::handleConnectionStatusChange)

            FirebaseDatabase.getInstance()
                .reference
                .child(".info/connected")
                .addValueEventListener(mConnectionRefValueEventListener)
        }
    }

    private fun handleConnectionStatusChange(snapshot: DataSnapshot){
        val isConnected:Boolean = snapshot.getValue(Boolean::class.java) ?: false
        if(isConnected){
            /*add a new node representing a new device*/
            val node = FirebaseDatabase.getInstance()
                .reference
                .child("connections/${UserDetailsUtils.user!!.firebaseUID}")
                .push()

            /*when this device disconnects remove it*/
            node.onDisconnect().removeValue()

            /*also update the last time i was seen*/
            FirebaseDatabase.getInstance()
                .reference
                .child("last_online/${UserDetailsUtils.user!!.firebaseUID}")
                .onDisconnect()
                .updateChildren(
                    mapOf(
                        "/status" to false,
                        "/timestamp" to ServerValue.TIMESTAMP
                    )
                )

            /*change my current connection status to connected*/
            FirebaseDatabase.getInstance()
                .reference
                .child("last_online/${UserDetailsUtils.user!!.firebaseUID}/status")
                .setValue(true)

            /*add this device to my connections list*/
            node.setValue(true)
        }
    }

    fun detachObserveConnectionStatusForUIDListener(firebaseUID: String){
        FirebaseDatabase.getInstance()
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
                        val person: Person? = mPersonDao
                            .getPersonByID(firebaseUID)

                        if(person == null)
                            return@launch

                        person.lastMessageTimeStamp = cs.timestamp.toString()

                        mPersonDao.updatePerson(person)
                    }

                    emitter.onNext(cs)
            })

            FirebaseDatabase.getInstance()
                .reference
                .child("last_online/$firebaseUID")
                .addValueEventListener(mUIDConnectionRefValueEventListener)
        }
}