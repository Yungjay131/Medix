package com.slyworks.room.daos

import androidx.room.*
import com.slyworks.models.models.ConsultationRequest
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable

@Dao
interface ConsultationRequestDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addConsultationRequest(vararg consultationRequest: ConsultationRequest):Completable

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateConsultationRequest(vararg consultationRequest: ConsultationRequest):Completable

    @Delete
    fun deleteConsultationRequest(vararg consultationRequest: ConsultationRequest):Completable

    @Query("SELECT * FROM ConsultationRequest ORDER BY timestamp ASC")
    fun observeConsultationRequests():Flowable<List<ConsultationRequest>>

    @Query("SELECT * FROM ConsultationRequest ORDER BY timestamp ASC")
    fun getConsultationRequestsAsync(): Observable<List<ConsultationRequest>>

    @Query("SELECT * FROM ConsultationRequest ORDER BY timestamp ASC")
    fun getConsultationRequests():List<ConsultationRequest>
}
