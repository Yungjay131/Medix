package com.slyworks.utils.di

import android.content.Context
import com.slyworks.di.ApplicationScope
import com.slyworks.utils.PreferenceManager
import dagger.Module
import dagger.Provides
import javax.inject.Named


/**
 *Created by Joshua Sylvanus, 9:54 PM, 23/07/2022.
 */

@Module
class UtilsModule {
    @Provides
    @ApplicationScope
    fun providePreferenceManager(@Named("application_context")
                                 context: Context):PreferenceManager{
        return PreferenceManager(context)
    }
}