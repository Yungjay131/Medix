package com.slyworks.models.models

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.slyworks.constants.REQUEST_PENDING
import com.slyworks.models.room_models.FBUserDetails
import kotlinx.parcelize.Parcelize


/**
 *Created by Joshua Sylvanus, 6:15 PM, 1/23/2022.
 */
@Parcelize
data class ConsultationRequest(
    var toUID:String = "",
    var details: FBUserDetails = FBUserDetails(),
    var status: String = REQUEST_PENDING ): Parcelable {
    constructor():this(toUID = "", details = FBUserDetails(), status = REQUEST_PENDING)
}




