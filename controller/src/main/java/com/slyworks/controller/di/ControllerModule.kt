package com.slyworks.controller.di

import com.slyworks.controller.AppController
import com.slyworks.di.ApplicationScope
import dagger.Module
import dagger.Provides


/**
 *Created by Joshua Sylvanus, 9:21 PM, 23/07/2022.
 */
@Module
object ControllerModule {
    @Provides
    @ApplicationScope
    fun provideAppController():AppController{
        return AppController()
    }
}