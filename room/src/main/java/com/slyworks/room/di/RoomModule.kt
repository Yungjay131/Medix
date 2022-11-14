package com.slyworks.room.di

import android.content.Context
import com.slyworks.di.ActivityScope
import com.slyworks.di.ApplicationScope
import com.slyworks.di.FragmentScope
import com.slyworks.room.AppDatabase
import com.slyworks.room.daos.*
import dagger.Module
import dagger.Provides
import javax.inject.Named


/**
 *Created by Joshua Sylvanus, 4:54 PM, 23/07/2022.
 */

@Module
object RoomModule {
    @Provides
    @ApplicationScope
    fun provideAppDatabase(context: Context):AppDatabase{
        return AppDatabase.getInstance(context)
    }

    @Provides
    @ApplicationScope
    fun providePersonDao(appDatabase: AppDatabase):PersonDao{
        return appDatabase.getPersonDao();
    }

    @Provides
    @ApplicationScope
    fun provideMessageDao(appDatabase: AppDatabase): MessageDao {
        return appDatabase.getMessageDao();
    }

    @Provides
    @ApplicationScope
    fun provideCallHistoryDao(appDatabase: AppDatabase):CallHistoryDao{
        return appDatabase.getCallHistoryDao();
    }

    @Provides
    @ApplicationScope
    fun provideConsultationRequestDao(appDatabase: AppDatabase):ConsultationRequestDao{
        return appDatabase.getConsultationRequestDao()
    }

}