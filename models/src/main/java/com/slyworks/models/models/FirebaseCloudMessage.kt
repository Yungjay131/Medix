package com.slyworks.models.models

import com.google.gson.annotations.SerializedName

data class FirebaseCloudMessage(
    @SerializedName("to")
    var to:String,
    @SerializedName("data")
    var data: Data)