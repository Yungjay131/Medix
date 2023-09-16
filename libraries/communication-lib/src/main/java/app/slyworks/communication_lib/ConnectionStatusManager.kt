package app.slyworks.communication_lib

import app.slyworks.data_lib.DataManager
import app.slyworks.data_lib.vmodels.PersonVModel
import app.slyworks.firebase_commons_lib.MValueEventListener
import app.slyworks.data_lib.models.ConnectionStatus
import com.google.firebase.database.*
import io.reactivex.rxjava3.core.Observable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


/**
 *Created by Joshua Sylvanus, 8:42 AM, 28/06/2022.
 */
class ConnectionStatusManager(
    private val firebaseDatabase: FirebaseDatabase,
    private val dataManager: DataManager) {

    private lateinit var connectionRefValueEventListener: ValueEventListener
    private lateinit var uidConnectionRefValueEventListener: ValueEventListener


    fun setMyConnectionStatusHandler():Observable<Unit>{
       return Observable.create<Unit> {_ ->

            connectionRefValueEventListener =
                MValueEventListener(onDataChangeFunc = ::onConnectionStatusChanged)

            firebaseDatabase
                .reference
                .child(".info/connected")
                .addValueEventListener(connectionRefValueEventListener)
        }
    }

    private fun onConnectionStatusChanged(snapshot: DataSnapshot){
        val isConnected:Boolean = snapshot.getValue(Boolean::class.java) ?: false
        val firebaseUID:String? = dataManager.getUserDetailsProperty<String>("firebaseUID")

        if(!isConnected || firebaseUID.isNullOrEmpty()) {
            return
        }

            /*add a new node representing a new device*/
            val node: DatabaseReference = firebaseDatabase
                .reference
                .child("connections/${firebaseUID}")
                .push()

            /*when this device disconnects remove it*/
            node.onDisconnect().removeValue()

            /*also update the last time i was seen*/
            firebaseDatabase
                .reference
                .child("last_online/${firebaseUID}")
                .onDisconnect()
                .updateChildren(
                    mapOf(
                        "/status" to false,
                        "/timestamp" to ServerValue.TIMESTAMP ))

            /*change my current connection status to connected*/
            firebaseDatabase
                .reference
                .child("last_online/${firebaseUID}/status")
                .setValue(true)

            /*add this device to my connections list*/
            node.setValue(true)
    }

    fun detachObserveConnectionStatusForUIDListener(firebaseUID: String){
        firebaseDatabase
            .reference
            .child("last_online/$firebaseUID")
            .removeEventListener(uidConnectionRefValueEventListener)
    }

    fun observeConnectionStatusForUID(firebaseUID:String):Observable<ConnectionStatus> =
        Observable.create { emitter ->
            uidConnectionRefValueEventListener =
                MValueEventListener(onDataChangeFunc = {
                    val cs = it.getValue(ConnectionStatus::class.java)!!

                    CoroutineScope(Dispatchers.IO).launch {
                        val person: PersonVModel =
                            dataManager.getPersonByID(firebaseUID) ?: return@launch

                        person.lastMessageTimeStamp = cs.timestamp.toString()

                        dataManager.updatePersons(person)
                    }

                    emitter.onNext(cs)
                })

            firebaseDatabase
                .reference
                .child("last_online/$firebaseUID")
                .addValueEventListener(uidConnectionRefValueEventListener)
        }
}