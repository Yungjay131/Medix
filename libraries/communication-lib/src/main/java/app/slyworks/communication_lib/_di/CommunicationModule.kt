package app.slyworks.communication_lib._di

import app.slyworks.communication_lib.*
import app.slyworks.data_lib.DataManager
import app.slyworks.data_lib.FCMClientApi
import app.slyworks.firebase_commons_lib.FirebaseUtils
import com.google.firebase.database.FirebaseDatabase
import dagger.Module
import dagger.Provides
import javax.inject.Singleton


/**
 * Created by Joshua Sylvanus, 5:57 AM, 09/08/2022.
 */
@Module
object CommunicationModule {
    @Provides
    @Singleton
    fun provideCallManager(fdb: FirebaseDatabase,
                           fcma: FCMClientApi,
                           fu:FirebaseUtils,
                           dm: DataManager)
    : CallManager = CallManager(fdb,fcma, fu, dm)

    @Provides
    @Singleton
    fun provideMessageManager(fb:FirebaseDatabase,
                              fcma: FCMClientApi,
                              fu: FirebaseUtils,
                              dm: DataManager)
    : MessageManager = MessageManager(fb, fcma, fu, dm)

    @Provides
    @Singleton
    fun provideCallHistoryManager(fb: FirebaseDatabase,
                                  dm: DataManager)
    : CallHistoryManager = CallHistoryManager( fb, dm)


    @Provides
    @Singleton
    fun provideConsultationRequestsManager(fb:FirebaseDatabase,
                                           fcma: FCMClientApi,
                                           fu: FirebaseUtils,
                                           dm: DataManager)
    : ConsultationRequestsManager = ConsultationRequestsManager(fb, fcma, fu, dm)

    @Provides
    @Singleton
    fun provideConnectionStatusManager(fb:FirebaseDatabase,
                                       dm: DataManager)
    : ConnectionStatusManager = ConnectionStatusManager(fb, dm)
}