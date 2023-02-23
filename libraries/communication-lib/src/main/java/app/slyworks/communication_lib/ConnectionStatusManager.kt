package app.slyworks.communication_lib

import app.slyworks.data_lib.DataManager
import app.slyworks.data_lib.vmodels.PersonVModel
import app.slyworks.firebase_commons_lib.MValueEventListener
import app.slyworks.data_lib.models.ConnectionStatus
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
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

    //region Vars
    private lateinit var connectionRefValueEventListener: ValueEventListener
    private lateinit var uidConnectionRefValueEventListener: ValueEventListener
    //endregion

    fun test(){

    }
    fun setMyConnectionStatusHandler():Observable<Unit>{
       return Observable.create<Unit> {_ ->
           val func:(DataSnapshot) -> Unit = {
               val isConnected:Boolean = it.getValue(Boolean::class.java) ?: false
               if(isConnected){
                   /*add a new node representing a new device*/
                   val node = firebaseDatabase
                       .reference
                       .child("connections/${dataManager.getUserDetailsParam<String>("firebaseUID")}")
                       .push()

                   /*when this device disconnects remove it*/
                   node.onDisconnect().removeValue()

                   /*also update the last time i was seen*/
                   firebaseDatabase
                       .reference
                       .child("last_online/${dataManager.getUserDetailsParam<String>("firebaseUID")}")
                       .onDisconnect()
                       .updateChildren(
                           mapOf(
                               "/status" to false,
                               "/timestamp" to ServerValue.TIMESTAMP ))

                   /*change my current connection status to connected*/
                   firebaseDatabase
                       .reference
                       .child("last_online")
                       .child("${dataManager.getUserDetailsParam<String>("firebaseUID")}")
                       .child("status")
                       .setValue(true)

                   /*add this device to my connections list*/
                   node.setValue(true)
               }
           }

            connectionRefValueEventListener =
                MValueEventListener(onDataChangeFunc = func)

            firebaseDatabase
                .reference
                .child(".info/connected")
                .addValueEventListener(connectionRefValueEventListener)
        }
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

                    CoroutineScope(Dispatchers.Default).launch {
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