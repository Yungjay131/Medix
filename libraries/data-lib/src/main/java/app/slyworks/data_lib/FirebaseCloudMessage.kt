package app.slyworks.data_lib

import com.google.gson.annotations.SerializedName

data class FirebaseCloudMessage(
    @SerializedName("to")
    var to:String,
    @SerializedName("data")
    var data: app.slyworks.data_lib.model.Data
)