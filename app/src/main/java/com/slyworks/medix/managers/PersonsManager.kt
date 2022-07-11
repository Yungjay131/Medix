package com.slyworks.medix.managers

import androidx.room.CoroutinesRoom
import com.google.firebase.crashlytics.internal.common.AppData
import com.slyworks.constants.READ
import com.slyworks.data.AppDatabase
import com.slyworks.data.daos.PersonDao
import com.slyworks.medix.App
import com.slyworks.models.room_models.Person
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch


/**
 *Created by Joshua Sylvanus, 9:17 PM, 27/06/2022.
 */
object PersonsManager {
    //region Vars
    private val mPersonDao:PersonDao = AppDatabase.getInstance(App.getContext()).getPersonDao()
    //endregion

    fun updateLastMessageInfo(firebaseUID:String){
        CoroutineScope(Dispatchers.IO).launch {
            val person: Person? =
               mPersonDao
                     .getPersonByID(firebaseUID) ?: return@launch

               mPersonDao
                .updatePerson(person!!.apply {
                    lastMessageStatus = READ
                    unreadMessageCount = 0
                })
        }

    }

    fun updateLastMessageTimeStamp(firebaseUID: String, timeStamp:String) {
        CoroutineScope(Dispatchers.IO).launch{
           val person:Person? =
             mPersonDao
                   .getPersonByID(firebaseUID) ?: return@launch

             mPersonDao
                 .updatePerson(person!!.apply {
                     lastMessageTimeStamp = timeStamp
                 })
        }
    }
}