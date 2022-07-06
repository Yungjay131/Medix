package com.slyworks.models.room_models

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.slyworks.constants.NOT_SET
import kotlinx.parcelize.Parcelize


/**
 *Created by Joshua Sylvanus, 5:58 PM, 12/05/2022.
 */

@Parcelize
@Entity
data class CallHistory(
    @ColumnInfo(name = "type")var type:Int = NOT_SET,
    @ColumnInfo(name = "status")var status:Int = NOT_SET,
    @ColumnInfo(name = "caller_uid")var callerUID:String = "",
    @ColumnInfo(name = "name")var callerName:String = "",
    @ColumnInfo(name = "sender_image_uri")var senderImageUri:String = "",
    @PrimaryKey
    @ColumnInfo(name = "time_stamp") var timeStamp:String = "",
    @ColumnInfo(name = "duration") var duration:String = ""):Parcelable, Comparable<CallHistory>{

    constructor():this(
        type = NOT_SET,
        status = NOT_SET,
        callerUID = "",
        callerName = "",
        senderImageUri = "",
        timeStamp = "",
        duration = "")

    override fun compareTo(other: CallHistory): Int {
        val otherTimeStamp: Long = other.timeStamp.toLong()
        if (this.timeStamp.toLong() > otherTimeStamp)
            return 1
        else if (this.timeStamp.toLong() < otherTimeStamp)
            return -1
        else if (this.timeStamp.toLong() == otherTimeStamp)
            return 0
        else
            throw UnsupportedOperationException("cannot sort order of unknown value")
    }
}
