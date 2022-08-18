package com.slyworks.medix.ui.activities.voice_call_activity

import androidx.lifecycle.ViewModel
import com.slyworks.communication.CallManager
import com.slyworks.medix.helpers.VibrationManager
import com.slyworks.models.models.VoiceCallRequest
import com.slyworks.models.room_models.FBUserDetails
import com.slyworks.userdetails.UserDetailsUtils
import javax.inject.Inject


/**
 *Created by Joshua Sylvanus, 8:50 PM, 11/08/2022.
 */
class VoiceCallViewModel
    @Inject
    constructor(private val userDetailsUtils: UserDetailsUtils,
                private val callManager: CallManager,
                private val vibrationManager: VibrationManager) : ViewModel() {
    //region Vars
    //endregion

    fun vibrate(type:Int) = vibrationManager.vibrate(type)

    fun stopVibration() = vibrationManager.stopVibration()

    fun getUserDetailsUtils():FBUserDetails = userDetailsUtils.user!!

    fun sendVoiceCallRequestViaFCM(userDetails: FBUserDetails)
         = callManager.sendVoiceCallRequestViaFCM(userDetails)

    fun processVoiceCall(type:String,
                         firebaseUID:String,
                         status:String? = null,
                         request:VoiceCallRequest? = null)
         = callManager.processVoiceCall(
                  type =  type,
                   firebaseUID = firebaseUID,
                   status = status,
                    request = request)
}