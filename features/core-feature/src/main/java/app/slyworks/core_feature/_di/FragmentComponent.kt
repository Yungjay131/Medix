package app.slyworks.core_feature._di

import app.slyworks.core_feature.profile.ProfileHostFragment
import app.slyworks.core_feature.chat.ChatHostFragment
import app.slyworks.core_feature.home.HomeDoctorFragment
import app.slyworks.core_feature.home.HomePatienrFragment
import app.slyworks.di_base_lib.FragmentScope
import dagger.Subcomponent


/**
 * Created by Joshua Sylvanus, 6:24 PM, 05-Dec-2022.
 */

@FragmentScope
@Subcomponent(modules = [FragmentViewModelModule::class])
interface FragmentComponent {
   fun inject(fragment:ChatHostFragment)
   fun inject(fragment:HomeDoctorFragment)
   fun inject(fragment:HomePatienrFragment)
   fun inject(fragment: ProfileHostFragment)

   @Subcomponent.Builder
   interface Builder{
       fun build(): FragmentComponent
   }
}