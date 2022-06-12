package com.slyworks.medix.utils

import com.slyworks.medix.navigation.ActivityWrapper
import com.slyworks.medix.ui.activities.SplashActivity
import com.slyworks.medix.ui.activities.loginActivity.LoginActivity
import com.slyworks.medix.ui.activities.mainActivity.MainActivity
import com.slyworks.medix.ui.activities.messageActivity.MessageActivity
import com.slyworks.medix.ui.activities.onBoardingActivity.OnBoardingActivity
import com.slyworks.medix.ui.activities.registrationActivity.RegistrationActivity
import com.slyworks.medix.ui.activities.registrationActivity.RegistrationDoctorActivity
import com.slyworks.medix.ui.activities.registrationActivity.RegistrationPatientActivity
import com.slyworks.medix.ui.activities.requestsActivity.RequestsActivity
import com.slyworks.medix.ui.activities.settingsActivity.SettingsActivity
import com.slyworks.medix.ui.activities.videoCallActivity.VideoCallActivity
import com.slyworks.medix.ui.activities.voiceCallActivity.VoiceCallActivity


/**
 *Created by Joshua Sylvanus, 10:08 PM, 18/05/2022.
 */
object ActivityUtils {
    //region Vars
    private var mForegroundStatus:Boolean = true
    private var mCount:Int = 0
    //endregion

    fun from(simpleName:String):Class<*>{
        return when(simpleName){
            MainActivity::class.simpleName -> MainActivity::class.java
            else -> throw IllegalArgumentException("fix the from method")
        }
    }

    fun setForegroundStatus(status:Boolean){
        mForegroundStatus = status
    }

    fun isAppInForeground():Boolean{
        val mActivitiesList:Array<Boolean> = arrayOf(
            RequestsActivity.getForegroundStatus(),
            MessageActivity.getForegroundStatus(),
            VideoCallActivity.getForegroundStatus(),
            VoiceCallActivity.getForegroundStatus(),
            SettingsActivity.getForegroundStatus())

        val otherStatus = ActivityUtils.isThereActivityInForeground()

        //if any is true, means the app in the foreground, return
        return mActivitiesList.any { it == true }
    }

    fun isThereActivityInForeground():Boolean =  mForegroundStatus


    fun setCurrentActivityStatus(simpleName: String, b: Boolean) =
        ActivityWrapper.from(simpleName).setIsRunning(b)

    fun isLastActivity():Boolean = mCount == 1
    fun incrementActivityCount() = mCount++
    fun decrementActivityCount() = mCount--

}

