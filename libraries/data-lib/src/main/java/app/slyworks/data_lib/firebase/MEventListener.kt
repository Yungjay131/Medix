package app.slyworks.data_lib.firebase

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestoreException
import timber.log.Timber


/**
 * Created by Joshua Sylvanus, 8:08 AM, 12-Oct-2023.
 */
class MEventListener(private val onEventFunc:(DocumentSnapshot) -> Unit) : EventListener<DocumentSnapshot> {
    override fun onEvent(value: DocumentSnapshot?, error: FirebaseFirestoreException?) {
        if(error != null){
            Timber.e(error)
            return
        }

        onEventFunc.invoke(value!!)
    }
}