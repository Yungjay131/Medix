package com.slyworks.models.models

import android.net.Uri

data class TempUserDetails
@JvmOverloads
constructor(
    var accountType: AccountType? = null,
    var firstName:String? = null,
    var lastName:String? = null,
    var email:String,
    var sex: Gender?,
    var age:String,
    var password:String,
    var firebaseUID: String? = null,
    var agoraUID:String? = null,
    var FBRegistrationToken:String? = null,
    var image_uri_init: Uri? = null,
    var imageUri:String? = null,
    var history: MutableList<String>? = null,
    var specialization: MutableList<String>? = null
)