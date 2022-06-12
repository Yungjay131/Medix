package com.slyworks.data.daos

import androidx.room.*
import com.slyworks.models.room_models.CallHistory
import kotlinx.coroutines.flow.Flow


/**
 *Created by Joshua Sylvanus, 6:04 PM, 12/05/2022.
 */
@Dao
interface CallHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addCallHistory(vararg callHistories:CallHistory)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateCallHistory(callHistory:CallHistory):Int

    @Delete
    suspend fun deleteCallHistory(vararg callHistories: CallHistory):Int

    @Query("SELECT * FROM CallHistory")
    fun observeCallHistory(): Flow<MutableList<CallHistory>>

    @Query("SELECT * FROM CallHistory")
    suspend fun getCallHistory():MutableList<CallHistory>
}