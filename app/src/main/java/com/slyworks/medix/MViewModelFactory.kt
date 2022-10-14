package com.slyworks.medix

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.slyworks.medix.ui.activities.login_activity.LoginActivityViewModel
import com.slyworks.medix.ui.activities.main_activity.MainActivityViewModel
import com.slyworks.medix.ui.activities.message_activity.MessageActivityViewModel
import com.slyworks.medix.ui.activities.onboarding_activity.OnBoardingViewModel
import com.slyworks.medix.ui.activities.registration_activity.RegistrationDoctorActivityViewModel
import com.slyworks.medix.ui.activities.registration_activity.RegistrationPatientActivityViewModel
import com.slyworks.medix.ui.activities.registration_activity.RegistrationActivityViewModel
import com.slyworks.medix.ui.activities.splash_activity.SplashActivityViewModel
import com.slyworks.medix.ui.activities.video_call_activity.VideoCallViewModel
import com.slyworks.medix.ui.activities.view_request_activity.ViewRequestViewModel
import com.slyworks.medix.ui.activities.voice_call_activity.VoiceCallViewModel
import com.slyworks.medix.ui.fragments.callsHistoryFragment.CallsHistoryViewModel
import com.slyworks.medix.ui.fragments.chatFragment.ChatFragmentViewModel
import com.slyworks.medix.ui.fragments.chatHostFragment.ChatHostFragmentViewModel
import com.slyworks.medix.ui.fragments.findDoctorsFragment.FindDoctorsFragmentViewModel
import com.slyworks.medix.ui.fragments.homeFragment.HomeFragmentViewModel
import com.slyworks.medix.ui.fragments.viewProfileFragment.ViewProfileFragmentViewModel
import javax.inject.Provider


/**
 *Created by Joshua Sylvanus, 7:33 PM, 24/07/2022.
 */
class MViewModelFactory (
    private val viewModels:
        Map<Class<out ViewModel>, @JvmSuppressWildcards Provider<ViewModel>>
    ): ViewModelProvider.Factory{

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(SplashActivityViewModel::class.java) ->
                viewModels[SplashActivityViewModel::class.java] as T

            modelClass.isAssignableFrom(OnBoardingViewModel::class.java) ->
                viewModels[OnBoardingViewModel::class.java] as T

            modelClass.isAssignableFrom(LoginActivityViewModel::class.java) ->
                viewModels[LoginActivityViewModel::class.java] as T

            modelClass.isAssignableFrom(RegistrationActivityViewModel::class.java) ->
                viewModels[RegistrationActivityViewModel::class.java] as T

            modelClass.isAssignableFrom(RegistrationPatientActivityViewModel::class.java) ->
                viewModels[RegistrationPatientActivityViewModel::class.java] as T

            modelClass.isAssignableFrom(RegistrationDoctorActivityViewModel::class.java) ->
                viewModels[RegistrationDoctorActivityViewModel::class.java] as T

            modelClass.isAssignableFrom(MainActivityViewModel::class.java) ->
                viewModels[MainActivityViewModel::class.java] as T

            modelClass.isAssignableFrom(MessageActivityViewModel::class.java) ->
                viewModels[MessageActivityViewModel::class.java] as T

             modelClass.isAssignableFrom(VideoCallViewModel::class.java) ->
                viewModels[VideoCallViewModel::class.java] as T

            modelClass.isAssignableFrom(VoiceCallViewModel::class.java) ->
                viewModels[VoiceCallViewModel::class.java] as T

            modelClass.isAssignableFrom(ViewRequestViewModel::class.java) ->
                viewModels[ViewRequestViewModel::class.java] as T

            modelClass.isAssignableFrom(HomeFragmentViewModel::class.java) ->
                viewModels[HomeFragmentViewModel::class.java] as T

            modelClass.isAssignableFrom(ChatHostFragmentViewModel::class.java) ->
                viewModels[ChatHostFragmentViewModel::class.java] as T

            modelClass.isAssignableFrom(FindDoctorsFragmentViewModel::class.java) ->
                viewModels[FindDoctorsFragmentViewModel::class.java] as T

            modelClass.isAssignableFrom(ChatFragmentViewModel::class.java) ->
                viewModels[ChatFragmentViewModel::class.java] as T

            modelClass.isAssignableFrom(CallsHistoryViewModel::class.java) ->
                viewModels[CallsHistoryViewModel::class.java] as T

            modelClass.isAssignableFrom(ViewProfileFragmentViewModel::class.java) ->
                viewModels[ViewProfileFragmentViewModel::class.java] as T

            else -> throw UnsupportedOperationException("please add class to MVIewModelFactory before trying to instantiate it")
        }
    }

}