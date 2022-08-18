package com.slyworks.medix.ui.activities.video_call_activity

import androidx.lifecycle.ViewModel
import com.slyworks.communication.CallHistoryManager
import com.slyworks.communication.CallManager
import com.slyworks.medix.helpers.VibrationManager
import com.slyworks.models.models.VideoCallRequest
import com.slyworks.models.room_models.CallHistory
import com.slyworks.models.room_models.FBUserDetails
import com.slyworks.userdetails.UserDetailsUtils
import javax.inject.Inject


/**
 *Created by Joshua Sylvanus, 5:30 PM, 1/23/2022.
 */
class VideoCallViewModel
    @Inject
    constructor(private val userDetailsUtils: UserDetailsUtils,
                private val callManager: CallManager,
                private val vibrationManager: VibrationManager,
                private val callHistoryManager: CallHistoryManager) : ViewModel(){


    fun vibrate(type:Int) = vibrationManager.vibrate(type)

    fun getUserDetailsUtils():FBUserDetails = userDetailsUtils.user!!

   fun processVideoCall(type:String, firebaseUID:String, request:VideoCallRequest? = null, status:String? = null){
       callManager.processVideoCall(
           type = type,
           firebaseUID = firebaseUID,
           request = request,
           status = status)
   }

    fun onVideoCallStarted(callHistory: CallHistory) = callHistoryManager.onVideoCallStarted(callHistory)
    fun onVideoCallStopped() = callHistoryManager.onVideoCallStopped()
}