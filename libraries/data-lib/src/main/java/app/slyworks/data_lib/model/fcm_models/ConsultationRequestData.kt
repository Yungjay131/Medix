package app.slyworks.data_lib.model.fcm_models

data class ConsultationRequestData(
    var message:String,
    var fromUID:String,
    var fullName:String,
    var toFCMRegistrationToken:String,
    override var type:String): Data