package app.slyworks.utils_lib._di

import android.content.Context
import app.slyworks.utils_lib.PreferenceHelper
import app.slyworks.utils_lib.TaskManager
import app.slyworks.utils_lib.TimeHelper
import dagger.Module
import dagger.Provides
import javax.inject.Singleton


/**
 *Created by Joshua Sylvanus, 9:54 PM, 23/07/2022.
 */

@Module
object UtilsModule {
    @Provides
    @Singleton
    fun providePreferenceManager(context:Context): PreferenceHelper =
        PreferenceHelper(context)

    @Provides
    fun provideTimeHelper(): TimeHelper = TimeHelper()

    @Provides
    @Singleton
    fun provideTaskManager(): TaskManager = TaskManager
}