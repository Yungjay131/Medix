package app.slyworks.data_lib.helpers.connection_listener

import app.slyworks.data_lib.firebase.FirebaseUtils
import app.slyworks.data_lib.helpers.storage.IUserDetailsHelper
import app.slyworks.data_lib.model.models.ConnectionStatus
import app.slyworks.firebase_commons_lib.MValueEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import io.reactivex.rxjava3.core.Observable


/**
 * Created by Joshua Sylvanus, 12:40 AM, 08-Oct-2023.
 */
class ConnectionStatusHelper(private val firebaseDB:FirebaseDatabase,
                             private val firebaseUtils:FirebaseUtils,
                             private val userDetailsHelper: IUserDetailsHelper
) : IConnectionStatusHelper {
    //region Vars
    //endregion

    override fun addHandlerForMyConnectionStatus(){
           val func:(DataSnapshot) -> Unit = { snapshot ->
               val uid:String? = userDetailsHelper.getUserID()
               val isConnected:Boolean =
                   snapshot.getValue(Boolean::class.java) ?: false

               /* add a new node representing the device */
               val node:DatabaseReference =
                   firebaseDB.reference
                       .child("connections/$uid")
                       .push()

               /* when this device disconnects remove it */
               node.onDisconnect()
                   .removeValue()

               /* also update the last time my "last-seen" */
               firebaseDB.reference
                   .child("last_online/$uid")
                   .onDisconnect()
                   .updateChildren(
                       mapOf(
                           "/status" to false,
                           "/timestamp" to ServerValue.TIMESTAMP
                       )
                   )

               /* change my current connection status to connected */
               firebaseDB.reference
                   .child("last_online/$uid/status")
                   .setValue(true)

               /* finally add this device to my connections list */
               node.setValue(true)
           }

            /* add the listener */
            firebaseDB.reference
                .child(".info/connected")
                .addValueEventListener(
                    MValueEventListener(
                        onDataChangeFunc = func
                    )
                )
    }

    override fun listenForAnotherUsersConnectionStatus(userUID:String):Observable<ConnectionStatus>{
        return Observable.create { emitter ->
            val func:(DataSnapshot) -> Unit = {snapshot ->
                val status:ConnectionStatus = snapshot.getValue(ConnectionStatus::class.java)!!

                /* update the local storage */

                /* emit */
                emitter.onNext(status)
            }

            firebaseDB.reference
                .child("last_online/$userUID")
                .addValueEventListener(
                    MValueEventListener(
                        onDataChangeFunc = func
                    )
                )
        }
    }

}