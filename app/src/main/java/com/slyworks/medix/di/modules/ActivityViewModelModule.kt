package com.slyworks.medix.di.modules

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.slyworks.constants.DI_ACTIVITY_VIEWMODEL_KEY
import com.slyworks.di.ActivityScope
import com.slyworks.medix.MViewModelFactory
import com.slyworks.medix.di.ViewModelKey
import com.slyworks.medix.ui.activities.login_activity.LoginActivityViewModel
import com.slyworks.medix.ui.activities.main_activity.MainActivityViewModel
import com.slyworks.medix.ui.activities.message_activity.MessageActivityViewModel
import com.slyworks.medix.ui.activities.onboarding_activity.OnBoardingViewModel
import com.slyworks.medix.ui.activities.registration_activity.RegistrationDoctorActivityViewModel
import com.slyworks.medix.ui.activities.registration_activity.RegistrationPatientActivityViewModel
import com.slyworks.medix.ui.activities.registration_activity.RegistrationActivityViewModel
import com.slyworks.medix.ui.activities.video_call_activity.VideoCallViewModel
import com.slyworks.medix.ui.activities.view_requests_activity.ViewRequestViewModel
import com.slyworks.medix.ui.activities.voice_call_activity.VoiceCallViewModel
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import javax.inject.Named
import javax.inject.Provider


@Module
object ActivityViewModelModule {
    @ActivityScope
    @Provides
    @Named(DI_ACTIVITY_VIEWMODEL_KEY)
    fun provideViewModelFactory(map: Map<Class<out ViewModel>, @JvmSuppressWildcards Provider<ViewModel>>):MViewModelFactory
       = MViewModelFactory(map)


    @ActivityScope
    @Provides
    @IntoMap
    @ViewModelKey(OnBoardingViewModel::class)
    fun provideOnBoardingActivityViewModel(viewModelFactory: MViewModelFactory,
                                           activity: AppCompatActivity):OnBoardingViewModel{
        return ViewModelProvider(activity.viewModelStore,
                                 viewModelFactory)
            .get(OnBoardingViewModel::class.java)
    }

    @ActivityScope
    @Provides
    @IntoMap
    @ViewModelKey(LoginActivityViewModel::class)
    fun provideLoginActivityViewModel(@Named(DI_ACTIVITY_VIEWMODEL_KEY)
                                      viewModelFactory:MViewModelFactory,
                                      activity: AppCompatActivity): LoginActivityViewModel {
        return ViewModelProvider(activity.viewModelStore,
                                 viewModelFactory)
            .get(LoginActivityViewModel::class.java)
    }

    @ActivityScope
    @Provides
    @IntoMap
    @ViewModelKey(RegistrationActivityViewModel::class)
    fun provideRegistrationActivityViewModel(@Named(DI_ACTIVITY_VIEWMODEL_KEY)
                                             viewModelFactory: MViewModelFactory,
                                             activity: AppCompatActivity):RegistrationActivityViewModel {
        return ViewModelProvider(activity.viewModelStore,
                                 viewModelFactory)
            .get(RegistrationActivityViewModel::class.java)
    }
    
    @ActivityScope
    @Provides
    @IntoMap
    @ViewModelKey(RegistrationPatientActivityViewModel::class)
    fun provideRegistrationPatientActivityViewModel(@Named(DI_ACTIVITY_VIEWMODEL_KEY)
                                                    viewModelFactory: MViewModelFactory,
                                                    activity: AppCompatActivity):RegistrationPatientActivityViewModel{
        return ViewModelProvider(activity.viewModelStore,
                                 viewModelFactory)
            .get(RegistrationPatientActivityViewModel::class.java)
    }

    @ActivityScope
    @Provides
    @IntoMap
    @ViewModelKey(RegistrationDoctorActivityViewModel::class)
    fun provideRegistrationDoctorActivityViewModel(@Named(DI_ACTIVITY_VIEWMODEL_KEY)
                                                   viewModelFactory: MViewModelFactory,
                                                   activity: AppCompatActivity): RegistrationDoctorActivityViewModel {
        return ViewModelProvider(activity.viewModelStore,
                                 viewModelFactory)
            .get(RegistrationDoctorActivityViewModel::class.java)
    }

    @ActivityScope
    @Provides
    @IntoMap
    @ViewModelKey(MainActivityViewModel::class)
    fun provideMainActivityViewModel(@Named(DI_ACTIVITY_VIEWMODEL_KEY)
                                     viewModelFactory: MViewModelFactory,
                                     activity: AppCompatActivity):MainActivityViewModel{
        return ViewModelProvider(activity.viewModelStore,
                                 viewModelFactory)
            .get(MainActivityViewModel::class.java)
    }

    @ActivityScope
    @Provides
    @IntoMap
    @ViewModelKey(MessageActivityViewModel::class)
    fun provideMessageActivityViewModel(@Named(DI_ACTIVITY_VIEWMODEL_KEY)
                                         viewModelFactory: MViewModelFactory,
                                         activity: AppCompatActivity): MessageActivityViewModel {
        return ViewModelProvider(activity.viewModelStore,
                                 viewModelFactory)
            .get(MessageActivityViewModel::class.java);
    }

    @ActivityScope
    @Provides
    @IntoMap
    @ViewModelKey(VideoCallViewModel::class)
    fun provideVideoCallActivityViewModel(@Named(DI_ACTIVITY_VIEWMODEL_KEY)
                                          viewModelFactory: MViewModelFactory,
                                          activity: AppCompatActivity):VideoCallViewModel{
        return ViewModelProvider(activity.viewModelStore,
                                 viewModelFactory)
            .get(VideoCallViewModel::class.java)
    }

    @ActivityScope
    @Provides
    @IntoMap
    @ViewModelKey(VoiceCallViewModel::class)
    fun provideVoiceCallActivityViewModel(@Named(DI_ACTIVITY_VIEWMODEL_KEY)
                                          viewModelFactory: MViewModelFactory,
                                          activity: AppCompatActivity):VoiceCallViewModel{
        return ViewModelProvider(activity.viewModelStore,
                                 viewModelFactory)
            .get(VoiceCallViewModel::class.java)
    }

    @ActivityScope
    @Provides
    @IntoMap
    @ViewModelKey(ViewRequestViewModel::class)
    fun provideViewRequestsViewModel(@Named(DI_ACTIVITY_VIEWMODEL_KEY)
                                     viewModelFactory: MViewModelFactory,
                                     activity: AppCompatActivity):ViewRequestViewModel{
        return ViewModelProvider(activity.viewModelStore,
                                 viewModelFactory)
            .get(ViewRequestViewModel::class.java);
    }
}