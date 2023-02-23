package app.slyworks.network_lib._di

import android.content.Context
import app.slyworks.network_lib.NetworkRegister
import dagger.Module
import dagger.Provides
import javax.inject.Singleton


/**
 * Created by Joshua Sylvanus, 5:00 PM, 23/07/2022.
 */

@Module
object NetworkModule {

    @Provides
    @Singleton
    fun provideNetworkRegister(context: Context): NetworkRegister =
        NetworkRegister(context)
}