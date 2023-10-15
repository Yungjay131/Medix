package app.slyworks.data_lib.model.models

import android.os.Parcelable
import app.slyworks.data_lib.model.view_entities.FBUserDetailsVModel
import app.slyworks.utils_lib.REQUEST_PENDING
import kotlinx.parcelize.Parcelize


/**
 *Created by Joshua Sylvanus, 12:22 PM, 1/23/2022.
 */
@Parcelize
data class VideoCallRequest(
    var details: FBUserDetailsVModel = FBUserDetailsVModel(),
    var status: String = REQUEST_PENDING
): Parcelable {
    constructor():this(
        details = FBUserDetailsVModel(),
        status = "")
}
