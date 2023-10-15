package app.slyworks.data_lib

import app.slyworks.data_lib.model.fcm_models.Data
import com.google.gson.annotations.SerializedName

data class FirebaseCloudMessage(
    @SerializedName("to")
    var to:String,
    @SerializedName("data")
    var data: Data
)