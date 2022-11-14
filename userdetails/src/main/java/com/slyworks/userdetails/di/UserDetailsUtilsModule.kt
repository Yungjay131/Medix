package com.slyworks.userdetails.di

import android.content.Context
import com.slyworks.di.ApplicationScope
import com.slyworks.userdetails.UserDetailsUtils
import dagger.Module
import dagger.Provides
import javax.inject.Named


/**
 *Created by Joshua Sylvanus, 8:43 PM, 23/07/2022.
 */

@Module
object UserDetailsUtilsModule {
    @Provides
    @ApplicationScope
    fun provideUserDetailsUtils(context: Context):UserDetailsUtils{
        return UserDetailsUtils(context)
    }
}