package com.slyworks.medix.navigation

import androidx.appcompat.app.AppCompatActivity
import androidx.collection.SimpleArrayMap
import com.slyworks.medix.ui.activities.login_activity.LoginActivity
import com.slyworks.medix.ui.activities.main_activity.MainActivity
import com.slyworks.medix.ui.activities.message_activity.MessageActivity
import com.slyworks.medix.ui.activities.onboarding_activity.OnBoardingActivity
import com.slyworks.medix.ui.activities.registration_activity.RegistrationActivity
import com.slyworks.medix.ui.activities.registration_activity.RegistrationDoctorActivity
import com.slyworks.medix.ui.activities.registration_activity.RegistrationPatientActivity
import com.slyworks.medix.ui.activities.video_call_activity.VideoCallActivity
import com.slyworks.medix.ui.activities.view_requests_activity.ViewRequestActivity
import com.slyworks.medix.ui.activities.settings_activity.SettingsActivity
import com.slyworks.medix.ui.activities.splash_activity.SplashActivity
import com.slyworks.medix.ui.activities.voice_call_activity.VoiceCallActivity


/**
 *Created by Joshua Sylvanus, 5:14 AM, 2/6/2022.
 */

sealed class ActivityWrapper{
    companion object {
        private var mMap: SimpleArrayMap<String, ActivityWrapper> = SimpleArrayMap<String, ActivityWrapper>().apply {
            put(SplashActivity::class.simpleName, SPLASH)
            put(OnBoardingActivity::class.simpleName, ONBOARDING)
            put(LoginActivity::class.simpleName, LOGIN)
            put(RegistrationActivity::class.simpleName, REG)
            put(RegistrationPatientActivity::class.simpleName, REG_PATIENT)
            put(RegistrationDoctorActivity::class.simpleName, REG_DOCTOR)
            put(MainActivity::class.simpleName, MAIN)
            put(MessageActivity::class.simpleName, MESSAGE)
            put(VideoCallActivity::class.simpleName, VIDEO_CALL)
            put(VoiceCallActivity::class.simpleName, SPLASH)
            put(ViewRequestActivity::class.simpleName, REQUESTS)
            put(SettingsActivity::class.simpleName, SETTINGS)
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
        override fun getInstance(): Class<out AppCompatActivity> = ViewRequestActivity::class.java
    }

    object SETTINGS : ActivityWrapper(){
        override fun getInstance(): Class<out AppCompatActivity> = SettingsActivity::class.java
    }
}