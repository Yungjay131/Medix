package app.slyworks.crypto_lib._di

import app.slyworks.data_lib.CryptoHelper
import dagger.Module
import dagger.Provides
import javax.inject.Singleton


/**
 * Created by Joshua Sylvanus, 9:25 PM, 22/11/2022.
 */

@Module
object CryptoModule {
    @Provides
    @Singleton
    fun provideCryptoHelper(): app.slyworks.data_lib.CryptoHelper = app.slyworks.data_lib.CryptoHelper()
}