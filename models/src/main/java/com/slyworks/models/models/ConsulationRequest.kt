package com.slyworks.models.models

import android.os.Parcelable
import androidx.room.*
import com.slyworks.constants.REQUEST_PENDING
import com.slyworks.models.room_models.FBUserDetails
import kotlinx.parcelize.Parcelize


/**
 *Created by Joshua Sylvanus, 6:15 PM, 1/23/2022.
 */
@Parcelize
@Entity
data class ConsultationRequest(
    @PrimaryKey
    @ColumnInfo(name = "to_uid") val toUID:String = "",
    @ColumnInfo(name = "timestamp") val timeStamp:String = "",
    @Embedded val details: FBUserDetails = FBUserDetails(),
    @ColumnInfo(name = "status") var status: String = REQUEST_PENDING ): Parcelable, Comparable<ConsultationRequest> {

    constructor():this(toUID = "", timeStamp = "", details = FBUserDetails(), status = REQUEST_PENDING)

    override fun compareTo(other: ConsultationRequest): Int {
        val thisTimeStamp:Long = this.timeStamp.toLong()
        val otherTimeStamp:Long = other.timeStamp.toLong()

        return when{
            thisTimeStamp > otherTimeStamp -> 1
            thisTimeStamp < otherTimeStamp -> -1
            thisTimeStamp == otherTimeStamp -> 0
            else -> throw UnsupportedOperationException("cannot sort order of unknown value")
        }
    }
}




