package com.slyworks.medix.di.components

import androidx.fragment.app.Fragment
import com.slyworks.di.FragmentScope
import com.slyworks.medix.di.modules.FragmentViewModelModule
import com.slyworks.medix.ui.fragments.ProfileHostFragment
import com.slyworks.medix.ui.fragments.callsHistoryFragment.CallsHistoryFragment
import com.slyworks.medix.ui.fragments.chatFragment.ChatFragment
import com.slyworks.medix.ui.fragments.chatHostFragment.ChatHostFragment
import com.slyworks.medix.ui.fragments.findDoctorsFragment.FindDoctorsFragment
import com.slyworks.medix.ui.fragments.homeFragment.DoctorHomeFragment
import com.slyworks.medix.ui.fragments.homeFragment.PatientHomeFragment
import com.slyworks.medix.ui.fragments.viewProfileFragment.ViewProfileFragment
import dagger.BindsInstance
import dagger.Subcomponent


/**
 *Created by Joshua Sylvanus, 4:00 PM, 07/08/2022.
 */
@FragmentScope
@Subcomponent(modules = [FragmentViewModelModule::class])
interface FragmentComponent {
    fun inject(fragment: PatientHomeFragment)
    fun inject(fragment: DoctorHomeFragment)
    fun inject(fragment: FindDoctorsFragment)
    fun inject(fragment: ProfileHostFragment)
    fun inject(fragment: ViewProfileFragment)
    fun inject(fragment: ChatHostFragment)
    fun inject(fragment: ChatFragment)
    fun inject(fragment: CallsHistoryFragment)

   @Subcomponent.Builder
   interface Builder{
       fun setFragment(@BindsInstance fragment:Fragment): Builder
       fun build(): FragmentComponent
   }

}