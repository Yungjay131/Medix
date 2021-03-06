package com.slyworks.models.models

import android.os.Parcelable
import com.slyworks.constants.REQUEST_PENDING
import kotlinx.parcelize.Parcelize

@Parcelize
data class ConsultationResponse(
    var toUID:String = "",
    var fromUID: String = "",
    var toFCMRegistrationToken:String = "",
    var status:String = REQUEST_PENDING,
    var fullName:String = ""
): Parcelable {
        constructor():this(toUID = "", fromUID = "", toFCMRegistrationToken = "", status = REQUEST_PENDING, fullName = "")
    }