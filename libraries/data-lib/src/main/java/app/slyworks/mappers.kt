package app.slyworks

import app.slyworks.data_lib.model.fcm_models.VideoCallData
import app.slyworks.data_lib.model.view_entities.FBUserDetailsVModel
import app.slyworks.data_lib.model.fcm_models.VoiceCallData
import app.slyworks.data_lib.model.models.VideoCallRequest
import app.slyworks.data_lib.model.models.VoiceCallRequest
import app.slyworks.utils_lib.FCM_VIDEO_CALL_REQUEST
import app.slyworks.utils_lib.FCM_VOICE_CALL_REQUEST


/**
 * Created by Joshua Sylvanus, 2:05 PM, 07-Oct-2023.
 */
fun FBUserDetailsVModel.toVoiceCallData():VoiceCallData{
    return VoiceCallData(
        accountType = accountType,
        firstName = firstName,
        lastName = lastName,
        fullName = fullName,
        email = email,
        sex = sex,
        age = age,
        firebaseUID = firebaseUID,
        agoraUID = agoraUID,
        fcmRegistrationToken = fcm_registration_token,
        imageUri = imageUri,
        type = FCM_VOICE_CALL_REQUEST
    )
}

fun FBUserDetailsVModel.toVideoCallData():VideoCallData{
    return VideoCallData(
        accountType = accountType,
        firstName = firstName,
        lastName = lastName,
        fullName = fullName,
        email = email,
        sex = sex,
        age = age,
        firebaseUID = firebaseUID,
        agoraUID = agoraUID,
        fcmRegistrationToken = fcm_registration_token,
        imageUri = imageUri,
        type = FCM_VIDEO_CALL_REQUEST
    )
}
fun VoiceCallRequest.toVoiceCallData(): VoiceCallData {
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
        fcmRegistrationToken = details.fcm_registration_token,
        imageUri = details.imageUri,
        type = FCM_VOICE_CALL_REQUEST
    )
}

fun VideoCallRequest.toVideoCallData(): VideoCallData {
    return VideoCallData(
        accountType = details.accountType,
        firstName = details.firstName,
        lastName = details.lastName,
        fullName = details.fullName,
        email = details.email,
        sex = details.sex,
        age = details.age,
        firebaseUID = details.firebaseUID,
        agoraUID = details.agoraUID,
        fcmRegistrationToken = details.fcm_registration_token,
        imageUri = details.imageUri,
        type = FCM_VIDEO_CALL_REQUEST
    )
}