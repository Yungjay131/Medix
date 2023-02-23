package app.slyworks.room_lib._di

import android.content.Context
import app.slyworks.di_base_lib.RoomLibScope
import app.slyworks.room_lib.daos.CallHistoryDao
import app.slyworks.room_lib.daos.ConsultationRequestDao
import app.slyworks.room_lib.daos.MessageDao
import app.slyworks.room_lib.AppDatabase
import app.slyworks.room_lib.daos.PersonDao
import dagger.Module
import dagger.Provides
import javax.inject.Singleton


/**
 * Created by Joshua Sylvanus, 4:54 PM, 23/07/2022.
 */

@Module
object RoomModule {
    @Provides
    @Singleton
    fun provideAppDatabase(context: Context): AppDatabase =
        AppDatabase.getInstance(context)

    @Provides
    @Singleton
    fun providePersonDao(appDatabase: AppDatabase): PersonDao =
        appDatabase.getPersonDao()

    @Provides
    @Singleton
    fun provideMessageDao(appDatabase: AppDatabase): MessageDao =
        appDatabase.getMessageDao()

    @Provides
    @Singleton
    fun provideCallHistoryDao(appDatabase: AppDatabase): CallHistoryDao =
        appDatabase.getCallHistoryDao()

    @Provides
    @Singleton
    fun provideConsultationRequestDao(appDatabase: AppDatabase): ConsultationRequestDao =
        appDatabase.getConsultationRequestDao()
}