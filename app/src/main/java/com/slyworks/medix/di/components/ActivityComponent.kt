package com.slyworks.medix.di.components

import androidx.appcompat.app.AppCompatActivity
import com.slyworks.auth.di.AuthActivityScopedModule
import com.slyworks.di.ActivityScope
import com.slyworks.medix.di.modules.ActivityModule
import com.slyworks.medix.di.modules.ActivityViewModelModule
import com.slyworks.medix.ui.activities.BaseActivity
import com.slyworks.medix.ui.activities.splash_activity.SplashActivity
import com.slyworks.medix.ui.activities.login_activity.LoginActivity
import com.slyworks.medix.ui.activities.main_activity.MainActivity
import com.slyworks.medix.ui.activities.message_activity.MessageActivity
import com.slyworks.medix.ui.activities.onboarding_activity.OnBoardingActivity
import com.slyworks.medix.ui.activities.registration_activity.RegistrationActivity
import com.slyworks.medix.ui.activities.registration_activity.RegistrationDoctorActivity
import com.slyworks.medix.ui.activities.registration_activity.RegistrationPatientActivity
import com.slyworks.medix.ui.activities.settings_activity.SettingsActivity
import com.slyworks.medix.ui.activities.video_call_activity.VideoCallActivity
import com.slyworks.medix.ui.activities.view_request_activity.ViewRequestActivity
import com.slyworks.medix.ui.activities.voice_call_activity.VoiceCallActivity
import dagger.BindsInstance
import dagger.Subcomponent


/**
 *Created by Joshua Sylvanus, 7:30 PM, 24/07/2022.
 */

@ActivityScope
@Subcomponent(modules = [
    ActivityModule::class,
    ActivityViewModelModule::class,
    AuthActivityScopedModule::class
])
interface ActivityComponent{
    fun inject(activity: SplashActivity)
    fun inject(activity: OnBoardingActivity)
    fun inject(activity: LoginActivity)
    fun inject(activity: RegistrationActivity)
    fun inject(activity: RegistrationPatientActivity)
    fun inject(activity: RegistrationDoctorActivity)
    fun inject(activity: MainActivity)
    fun inject(activity: MessageActivity)
    fun inject(activity: VideoCallActivity)
    fun inject(activity: VoiceCallActivity)
    fun inject(activity: ViewRequestActivity)
    fun inject(activity: SettingsActivity)

    fun fragmentComponentBuilder(): FragmentComponent.Builder

    @Subcomponent.Builder
    interface Builder{
        fun setActivity(@BindsInstance activity: AppCompatActivity): Builder
        fun build(): ActivityComponent
    }
}