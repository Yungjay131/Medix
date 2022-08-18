package com.slyworks.medix.di.modules

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.slyworks.constants.DI_FRAGMENT_VIEWMODEL_KEY
import com.slyworks.di.FragmentScope
import com.slyworks.medix.MViewModelFactory
import com.slyworks.medix.di.ViewModelKey
import com.slyworks.medix.ui.fragments.callsHistoryFragment.CallsHistoryViewModel
import com.slyworks.medix.ui.fragments.chatFragment.ChatFragmentViewModel
import com.slyworks.medix.ui.fragments.chatHostFragment.ChatHostFragmentViewModel
import com.slyworks.medix.ui.fragments.findDoctorsFragment.FindDoctorsFragmentViewModel
import com.slyworks.medix.ui.fragments.homeFragment.HomeFragmentViewModel
import com.slyworks.medix.ui.fragments.viewProfileFragment.ViewProfileFragmentViewModel
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import javax.inject.Named
import javax.inject.Provider

/**
 *Created by Joshua Sylvanus, 7:31 PM, 24/07/2022.
 */
@Module
object FragmentViewModelModule{
    @FragmentScope
    @Provides
    @Named(DI_FRAGMENT_VIEWMODEL_KEY)
    fun provideViewModelFactory(map: Map<Class<out ViewModel>, @JvmSuppressWildcards Provider<ViewModel>>): MViewModelFactory
            = MViewModelFactory(map)

    @FragmentScope
    @Provides
    @IntoMap
    @ViewModelKey(HomeFragmentViewModel::class)
    fun provideHomeFragmentViewModel(@Named(DI_FRAGMENT_VIEWMODEL_KEY)
                                     viewModelFactory: MViewModelFactory,
                                     fragment: Fragment): HomeFragmentViewModel {
        return ViewModelProvider(
            fragment.viewModelStore,
            viewModelFactory
        )
            .get(HomeFragmentViewModel::class.java)
    }

    @FragmentScope
    @Provides
    @IntoMap
    @ViewModelKey(FindDoctorsFragmentViewModel::class)
    fun provideFindDoctorsFragmentViewModel(@Named(DI_FRAGMENT_VIEWMODEL_KEY)
                                            viewModelFactory: MViewModelFactory,
                                            fragment: Fragment): FindDoctorsFragmentViewModel {
        return ViewModelProvider(
            fragment.viewModelStore,
            viewModelFactory)
            .get(FindDoctorsFragmentViewModel::class.java)
    }

    @FragmentScope
    @Provides
    @IntoMap
    @ViewModelKey(ChatHostFragmentViewModel::class)
    fun provideChatHostFragmentViewModel(@Named(DI_FRAGMENT_VIEWMODEL_KEY)
                                         viewModelFactory: MViewModelFactory,
                                         fragment: Fragment):ChatHostFragmentViewModel{
        return ViewModelProvider(
            fragment.viewModelStore,
            viewModelFactory)
            .get(ChatHostFragmentViewModel::class.java)
    }

    @FragmentScope
    @Provides
    @IntoMap
    @ViewModelKey(ChatFragmentViewModel::class)
    fun provideChatFragmentViewModel(@Named(DI_FRAGMENT_VIEWMODEL_KEY)
                                     viewModelFactory: MViewModelFactory,
                                     fragment: Fragment): ChatFragmentViewModel {
        return ViewModelProvider(
            fragment.viewModelStore,
            viewModelFactory)
            .get(ChatFragmentViewModel::class.java)
    }

    @FragmentScope
    @Provides
    @IntoMap
    @ViewModelKey(CallsHistoryViewModel::class)
    fun provideCallHistoryViewModel(@Named(DI_FRAGMENT_VIEWMODEL_KEY)
                                    viewModelFactory: MViewModelFactory,
                                    fragment: Fragment): CallsHistoryViewModel {
        return ViewModelProvider(
            fragment.viewModelStore,
            viewModelFactory)
            .get(CallsHistoryViewModel::class.java)
    }

    @FragmentScope
    @Provides
    @IntoMap
    @ViewModelKey(ViewProfileFragmentViewModel::class)
    fun provideViewProfileFragmentViewModel(@Named(DI_FRAGMENT_VIEWMODEL_KEY)
                                            viewModelFactory: MViewModelFactory,
                                            fragment: Fragment): ViewProfileFragmentViewModel {
        return ViewModelProvider(
            fragment.viewModelStore,
            viewModelFactory)
            .get(ViewProfileFragmentViewModel::class.java)
    }

}