package com.slyworks.data.daos

import androidx.room.*
import com.slyworks.models.room_models.Person
import kotlinx.coroutines.flow.Flow


/**
 *Created by Joshua Sylvanus, 8:12 AM, 27/04/2022.
 */
@Dao
interface PersonDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addPerson(vararg persons: Person)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updatePerson(person: Person)

    @Delete
    suspend fun deletePerson(vararg persons: Person)

    @Query("SELECT * FROM Person")
    suspend fun getPersons():MutableList<Person>

    @Query("SELECT * FROM Person")
    fun observePersons(): Flow<MutableList<Person>>

    @Query("SELECT * FROM Person where firebase_uid == :firebaseUID LIMIT 1")
    suspend fun getPersonByID(firebaseUID:String): Person

    @Query("SELECT COUNT(*) FROM Message")
    suspend fun getPersonCount():Int
}