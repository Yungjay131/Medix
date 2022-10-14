package com.slyworks.communication.di

import com.google.firebase.database.FirebaseDatabase
import com.slyworks.communication.*
import com.slyworks.controller.di.ControllerModule
import com.slyworks.di.ApplicationScope
import com.slyworks.fcm_api.FCMClientApi
import com.slyworks.fcm_api.di.FCM_APIModule
import com.slyworks.firebase_commons.FirebaseUtils
import com.slyworks.firebase_commons.di.FirebaseCommonsModule
import com.slyworks.room.daos.CallHistoryDao
import com.slyworks.room.daos.ConsultationRequestDao
import com.slyworks.room.daos.MessageDao
import com.slyworks.room.daos.PersonDao
import com.slyworks.room.di.RoomModule
import com.slyworks.userdetails.UserDetailsUtils
import com.slyworks.userdetails.di.UserDetailsUtilsModule
import dagger.Module
import dagger.Provides


/**
 *Created by Joshua Sylvanus, 5:57 AM, 09/08/2022.
 */
@Module(includes = [
    ControllerModule::class,
    UserDetailsUtilsModule::class,
    FirebaseCommonsModule::class,
    RoomModule::class,
    FCM_APIModule::class])
object CommunicationModule {
    @Provides
    @ApplicationScope
    fun provideCallManager(firebaseDatabase: FirebaseDatabase,
                           fcmClientApi: FCMClientApi,
                           userDetailsUtils: UserDetailsUtils,
                           firebaseUtils: FirebaseUtils): CallManager {
        return CallManager(
            firebaseDatabase,
            fcmClientApi,
            userDetailsUtils,
            firebaseUtils)
    }

    @Provides
    @ApplicationScope
    fun provideMessageManager(firebaseDatabase: FirebaseDatabase,
                              messageDao: MessageDao,
                              personDao: PersonDao,
                              userDetailsUtils: UserDetailsUtils): MessageManager {
        return MessageManager(
            firebaseDatabase,
            messageDao,
            personDao,
            userDetailsUtils)
    }

    @Provides
    @ApplicationScope
    fun provideCallHistoryManager(callHistoryDao: CallHistoryDao,
                                  firebaseDatabase: FirebaseDatabase,
                                  userDetailsUtils: UserDetailsUtils): CallHistoryManager {
        return CallHistoryManager(callHistoryDao,
            firebaseDatabase,
            userDetailsUtils)
    }


    @Provides
    @ApplicationScope
    fun provideCloudMessageManager(firebaseDatabase: FirebaseDatabase,
                                   fcmClientApi: FCMClientApi,
                                   userDetailsUtils: UserDetailsUtils,
                                   firebaseUtils: FirebaseUtils,
                                   consultationRequestDao: ConsultationRequestDao): CloudMessageManager {
        return CloudMessageManager(firebaseDatabase,
            fcmClientApi,
            userDetailsUtils,
            firebaseUtils,
            consultationRequestDao)
    }

    @Provides
    @ApplicationScope
    fun provideConnectionStatusManager(personDao:PersonDao,
                                       firebaseDatabase: FirebaseDatabase,
                                       userDetailsUtils: UserDetailsUtils): ConnectionStatusManager {
        return ConnectionStatusManager(personDao,
            firebaseDatabase,
            userDetailsUtils)
    }
}