package com.slyworks.models.models

import android.os.Parcelable
import com.slyworks.constants.REQUEST_PENDING
import com.slyworks.models.room_models.FBUserDetails
import kotlinx.parcelize.Parcelize

@Parcelize
data class VoiceCallRequest(
    var details: FBUserDetails = FBUserDetails(),
    var status: String = REQUEST_PENDING
) : Parcelable {
    constructor() : this(
        details = FBUserDetails(),
        status = "")
}
