package com.slyworks.auth

import android.content.Context
import com.slyworks.constants.READ
import com.slyworks.models.room_models.Person
import com.slyworks.room.daos.PersonDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named


/**
 *Created by Joshua Sylvanus, 9:17 PM, 27/06/2022.
 */
class PersonsManager(private val personDao: PersonDao) {

    fun updateLastMessageInfo(firebaseUID:String){
        CoroutineScope(Dispatchers.IO).launch {
            val person: Person =
               personDao.getPersonByID(firebaseUID) ?: return@launch

               personDao
                .updatePerson(person.apply {
                    lastMessageStatus = READ
                    unreadMessageCount = 0
                })
        }

    }

    fun updateLastMessageTimeStamp(firebaseUID: String, timeStamp:String) {
        CoroutineScope(Dispatchers.IO).launch{
           val person: Person =
             personDao.getPersonByID(firebaseUID) ?: return@launch

             personDao
                 .updatePerson(person.apply {
                     lastMessageTimeStamp = timeStamp
                 })
        }
    }
}