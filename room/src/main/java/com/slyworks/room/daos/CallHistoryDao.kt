package com.slyworks.room.daos

import androidx.room.*
import com.slyworks.models.room_models.CallHistory
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Single


/**
 *Created by Joshua Sylvanus, 6:04 PM, 12/05/2022.
 */
@Dao
interface CallHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addCallHistory(vararg callHistories: CallHistory): Completable

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateCallHistory(callHistory: CallHistory):Completable

    @Delete
    fun deleteCallHistory(vararg callHistories: CallHistory): Completable

    @Query("SELECT * FROM CallHistory")
    fun observeCallHistory(): Flowable<List<CallHistory>>

    @Query("SELECT * FROM CallHistory")
     fun getCallHistory(): Maybe<List<CallHistory>>

     @Query("SELECT COUNT(*) FROM CallHistory")
     fun getCallHistoryCount(): Single<Int>
}