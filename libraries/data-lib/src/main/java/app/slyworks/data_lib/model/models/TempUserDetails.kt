package app.slyworks.data_lib.model.models

import android.net.Uri

data class TempUserDetails
@JvmOverloads
constructor(
    var accountType: AccountType? = null,
    var firstName:String? = null,
    var lastName:String? = null,
    var email:String? = null,
    var gender: Gender? = null,
    var dob:String? = null,
    var password:String? = null,
    var firebaseUID: String? = null,
    var agoraUID:String? = null,
    var FBRegistrationToken:String? = null,
    var image_uri_init: Uri? = null,
    var imageUri:String? = null,
    var history: List<String>? = null,
    var specialization: List<String>? = null
)