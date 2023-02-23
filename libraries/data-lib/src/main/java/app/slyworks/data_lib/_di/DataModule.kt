package app.slyworks.data_lib._di

import android.content.Context
import app.slyworks.data_lib.*
import app.slyworks.di_base_lib.DataLibScope
import app.slyworks.room_lib._di.RoomModule
import app.slyworks.room_lib.daos.CallHistoryDao
import app.slyworks.room_lib.daos.ConsultationRequestDao
import app.slyworks.room_lib.daos.MessageDao
import app.slyworks.room_lib.daos.PersonDao
import dagger.Module
import dagger.Provides
import javax.inject.Singleton


/**
 * Created by Joshua Sylvanus, 7:59 PM, 27/11/2022.
 */
@Module
object DataModule {

 @Provides
   @Singleton
   fun provideDataManager(md: MessageDao,
                          pd: PersonDao,
                          chd: CallHistoryDao,
                          crd:ConsultationRequestDao,
                          udu: UserDetailsUtils,
                          ch: CryptoHelper): DataManager =
       DataManager(md, pd, chd, crd, udu, ch)

    @Provides
    @Singleton
    fun provideUserDetailsUtils(context: Context): UserDetailsUtils =
        UserDetailsUtils(context)

    @Provides
    @Singleton
    fun provideApiClient(): FCMClientApi =
        ApiClient.getApiInterface()

    @Provides
    @Singleton
    fun provideCryptoHelper(): CryptoHelper = CryptoHelper()


}