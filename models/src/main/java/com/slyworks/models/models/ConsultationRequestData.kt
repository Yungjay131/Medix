package com.slyworks.models.models

data class ConsultationRequestData(
    var message:String,
    var fromUID:String,
    var fullName:String,
    var toFCMRegistrationToken:String,
    override var type:String):Data