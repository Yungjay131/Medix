package app.slyworks.core_feature._di

import androidx.fragment.app.Fragment
import app.slyworks.core_feature.ProfileHostFragment
import app.slyworks.core_feature.chat.ChatHostFragment
import app.slyworks.core_feature.home.DoctorHomeFragment
import app.slyworks.core_feature.home.PatientHomeFragment
import app.slyworks.di_base_lib.FragmentScope
import dagger.BindsInstance
import dagger.Subcomponent


/**
 * Created by Joshua Sylvanus, 6:24 PM, 05-Dec-2022.
 */

@FragmentScope
@Subcomponent(modules = [FragmentViewModelModule::class])
interface FragmentComponent {
   fun inject(fragment:ChatHostFragment)
   fun inject(fragment:DoctorHomeFragment)
   fun inject(fragment:PatientHomeFragment)
   fun inject(fragment:ProfileHostFragment)

   @Subcomponent.Builder
   interface Builder{
       fun build(): FragmentComponent
   }
}