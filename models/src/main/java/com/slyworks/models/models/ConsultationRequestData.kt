package com.slyworks.models.models

data class ConsultationRequestData(
    var message:String,
    var fromUID:String,
    override var type:String): Data