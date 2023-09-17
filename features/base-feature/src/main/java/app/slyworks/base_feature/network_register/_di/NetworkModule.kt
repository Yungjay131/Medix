package app.slyworks.base_feature.network_register._di

import android.content.Context
import app.slyworks.base_feature.network_register.NetworkRegister
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