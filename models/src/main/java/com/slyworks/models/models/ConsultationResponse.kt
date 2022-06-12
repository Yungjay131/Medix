package com.slyworks.models.models

import android.os.Parcelable
import com.slyworks.constants.REQUEST_PENDING
import kotlinx.parcelize.Parcelize

@Parcelize
data class ConsultationResponse(
    var toUID:String = "",
    var fromUID: String = "",
    var status:String = REQUEST_PENDING,
    var fullName:String = ""
): Parcelable {
        constructor():this(toUID = "", fromUID = "", status = REQUEST_PENDING, fullName = "")
    }