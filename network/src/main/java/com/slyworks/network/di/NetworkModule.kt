package com.slyworks.network.di

import android.content.Context
import com.slyworks.di.ApplicationScope
import com.slyworks.network.NetworkRegister
import dagger.Module
import dagger.Provides
import javax.inject.Named


/**
 *Created by Joshua Sylvanus, 5:00 PM, 23/07/2022.
 */

@Module
object NetworkModule {

    @Provides
    @ApplicationScope
    fun provideNetworkRegister(@Named("application_context")
                               context:Context):NetworkRegister{
        return NetworkRegister(context)
    }
}