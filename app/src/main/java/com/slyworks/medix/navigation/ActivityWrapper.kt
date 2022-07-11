package com.slyworks.medix.navigation

import androidx.appcompat.app.AppCompatActivity
import androidx.collection.SimpleArrayMap
import com.slyworks.medix.ui.activities.*
import com.slyworks.medix.ui.activities.loginActivity.LoginActivity
import com.slyworks.medix.ui.activities.mainActivity.MainActivity
import com.slyworks.medix.ui.activities.messageActivity.MessageActivity
import com.slyworks.medix.ui.activities.onBoardingActivity.OnBoardingActivity
import com.slyworks.medix.ui.activities.registrationActivity.RegistrationActivity
import com.slyworks.medix.ui.activities.registrationActivity.RegistrationDoctorActivity
import com.slyworks.medix.ui.activities.registrationActivity.RegistrationPatientActivity
import com.slyworks.medix.ui.activities.videoCallActivity.VideoCallActivity
import com.slyworks.medix.ui.activities.requestsActivity.ViewRequest
import com.slyworks.medix.ui.activities.settingsActivity.SettingsActivity
import com.slyworks.medix.ui.activities.voiceCallActivity.VoiceCallActivity


/**
 *Created by Joshua Sylvanus, 5:14 AM, 2/6/2022.
 */

sealed class ActivityWrapper{
    companion object {
        private var mMap: SimpleArrayMap<String, ActivityWrapper> = SimpleArrayMap<String, ActivityWrapper>().apply {
            put(SplashActivity::class.simpleName, ActivityWrapper.SPLASH)
            put(OnBoardingActivity::class.simpleName, ActivityWrapper.ONBOARDING)
            put(LoginActivity::class.simpleName, ActivityWrapper.LOGIN)
            put(RegistrationActivity::class.simpleName, ActivityWrapper.REG)
            put(RegistrationPatientActivity::class.simpleName, ActivityWrapper.REG_PATIENT)
            put(RegistrationDoctorActivity::class.simpleName, ActivityWrapper.REG_DOCTOR)
            put(MainActivity::class.simpleName, ActivityWrapper.MAIN)
            put(MessageActivity::class.simpleName, ActivityWrapper.MESSAGE)
            put(VideoCallActivity::class.simpleName, ActivityWrapper.VIDEO_CALL)
            put(VoiceCallActivity::class.simpleName, ActivityWrapper.SPLASH)
            put(ViewRequest::class.simpleName, ActivityWrapper.REQUESTS)
            put(SettingsActivity::class.simpleName, ActivityWrapper.SETTINGS)
        }

        fun from(simpleName: String): ActivityWrapper {
            return mMap[simpleName] ?: throw IllegalArgumentException("ActivityWrapper with that name does not exist")
        }
    }

    abstract fun getInstance(): Class<out AppCompatActivity>

    object SPLASH : ActivityWrapper() {
        override fun getInstance(): Class<out AppCompatActivity> = SplashActivity::class.java
    }

    object ONBOARDING : ActivityWrapper() {
        override fun getInstance(): Class<out AppCompatActivity> = OnBoardingActivity::class.java

    }

    object LOGIN : ActivityWrapper() {
        override fun getInstance(): Class<out AppCompatActivity> = LoginActivity::class.java
    }

    object REG : ActivityWrapper() {
        override fun getInstance(): Class<out AppCompatActivity> = RegistrationActivity::class.java
    }

    object REG_PATIENT : ActivityWrapper() {
        override fun getInstance(): Class<out AppCompatActivity> = RegistrationPatientActivity::class.java
    }

    object REG_DOCTOR : ActivityWrapper() {
        override fun getInstance(): Class<out AppCompatActivity> = RegistrationDoctorActivity::class.java
    }

    object MAIN : ActivityWrapper(){
        override fun getInstance(): Class<out AppCompatActivity> = MainActivity::class.java
    }

    object MESSAGE : ActivityWrapper(){
        override fun getInstance(): Class<out AppCompatActivity> = MessageActivity::class.java
    }

    object VIDEO_CALL : ActivityWrapper(){
        override fun getInstance(): Class<out AppCompatActivity> = VideoCallActivity::class.java
    }

    object VOICE_CALL : ActivityWrapper(){
        override fun getInstance(): Class<out AppCompatActivity> = VoiceCallActivity::class.java
    }

    object REQUESTS : ActivityWrapper(){
        override fun getInstance(): Class<out AppCompatActivity> = ViewRequest::class.java
    }

    object SETTINGS : ActivityWrapper(){
        override fun getInstance(): Class<out AppCompatActivity> = SettingsActivity::class.java
    }
}