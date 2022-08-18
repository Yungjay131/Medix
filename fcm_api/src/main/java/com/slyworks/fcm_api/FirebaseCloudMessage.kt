package com.slyworks.fcm_api

import com.google.gson.annotations.SerializedName
import com.slyworks.models.models.Data

data class FirebaseCloudMessage(
    @SerializedName("to")
    var to:String,
    @SerializedName("data")
    var data: Data
)