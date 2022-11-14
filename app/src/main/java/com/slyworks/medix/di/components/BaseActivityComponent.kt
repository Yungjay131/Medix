package com.slyworks.medix.di.components

import com.slyworks.di.ApplicationScope
import com.slyworks.medix.ui.activities.BaseActivity
import dagger.Component
import dagger.Subcomponent
import javax.inject.Singleton


/**
 * Created by Joshua Sylvanus, 1:24 AM, 26/10/2022.
 */

@Subcomponent
interface BaseActivityComponent {
  fun inject(ba:BaseActivity)

  @Subcomponent.Builder
  interface Builder{
      fun build():BaseActivityComponent
  }
}