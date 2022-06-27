package com.slyworks.models.models

import com.slyworks.constants.FCM_VOICE_CALL_REQUEST
import com.slyworks.models.room_models.FBUserDetails


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
    override var type: String = FCM_VOICE_CALL_REQUEST,): Data() {
        companion object{
            fun from(details: FBUserDetails): VoiceCallData {
                return VoiceCallData(
                    accountType = details.accountType,
                    firstName = details.firstName,
                    lastName = details.lastName,
                    fullName = details.fullName,
                    email = details.email,
                    sex = details.sex,
                    age = details.age,
                    firebaseUID = details.firebaseUID,
                    agoraUID = details.agoraUID,
                    fcmRegistrationToken = details.FCMRegistrationToken,
                    imageUri = details.imageUri,
                    type = FCM_VOICE_CALL_REQUEST
                )
            }
        }
    }