package com.slyworks.data.daos

import androidx.room.*
import com.slyworks.models.models.ConsultationRequest
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Maybe

@Dao
interface ConsultationRequestDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addConsultationRequest(vararg consultationRequest: ConsultationRequest):Completable

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateConsultationRequest(vararg consultationRequest: ConsultationRequest):Completable

    @Delete
    fun deleteConsultationRequest(vararg consultationRequest: ConsultationRequest):Completable

    @Query("SELECT * FROM ConsultationRequest")
    fun observeConsultationRequests():Flowable<List<ConsultationRequest>>

    @Query("SELECT * FROM ConsultationRequest")
    fun getConsultationRequests(): Maybe<List<ConsultationRequest>>
}
