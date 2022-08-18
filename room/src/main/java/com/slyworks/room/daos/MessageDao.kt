package com.slyworks.room.daos

import androidx.room.*
import com.slyworks.constants.NOT_SENT
import com.slyworks.models.room_models.Message
import kotlinx.coroutines.flow.Flow


/**
 *Created by Joshua Sylvanus, 8:30 PM, 1/19/2022.
 */
@Dao
interface MessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addMessage(vararg messages: Message)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateMessage(vararg messages: Message):Int

    @Delete
    suspend fun deleteMessage(vararg messages: Message):Int

    @Query("SELECT * FROM Message")
    fun observeMessages(): Flow<MutableList<Message>>

    @Query("SELECT * FROM Message where from_uid == :firebaseUID  OR to_uid == :firebaseUID")
    fun observeMessagesForUID(firebaseUID:String):Flow<MutableList<Message>>

    @Query("SELECT * FROM Message")
    suspend fun getMessages():MutableList<Message>

    @Query("SELECT * FROM Message where message_id == :messageID LIMIT 1")
    suspend fun getMessageByID(messageID:String): Message

    @Query("SELECT * FROM Message where from_uid == :firebaseUID  OR to_uid == :firebaseUID")
    suspend fun getMessagesForUID(firebaseUID:String):MutableList<Message>

    @Query("SELECT COUNT(*) FROM Message")
    suspend fun getMessageCount():Int

    @Query("SELECT COUNT(*) FROM Message where from_uid == :firebaseUID OR to_uid == :firebaseUID")
    suspend fun getMessageCountForUID(firebaseUID:String):Int

    @Query("SELECT * FROM Message where status == $NOT_SENT")
    suspend fun getUnsentMessages():MutableList<Message>

}