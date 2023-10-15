package app.slyworks.data_lib.model.fcm_models

import app.slyworks.utils_lib.FCM_VOICE_CALL_REQUEST


data class VoiceCallData(
    val accountType:String,
    val firstName:String,
    val lastName:String,
    val fullName:String,
    val email:String,
    val sex:String,
    val age:String,
    val firebaseUID:String,
    val agoraUID:String,
    val fcmRegistrationToken:String,
    val imageUri:String,
    override var type: String = FCM_VOICE_CALL_REQUEST
): Data