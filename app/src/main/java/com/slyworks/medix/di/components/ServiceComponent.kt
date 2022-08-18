package com.slyworks.medix.di.components

import com.slyworks.di.ApplicationScope
import com.slyworks.medix.services.MFirebaseMessagingService
import dagger.Subcomponent


/**
 *Created by Joshua Sylvanus, 7:26 PM, 11/08/2022.
 */
@ApplicationScope
@Subcomponent
interface ServiceComponent {
   fun inject(service: MFirebaseMessagingService)

   @Subcomponent.Builder
   interface Builder{
      fun build(): ServiceComponent
   }
}