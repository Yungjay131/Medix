package com.slyworks.room.di

import android.content.Context
import com.slyworks.di.ActivityScope
import com.slyworks.di.ApplicationScope
import com.slyworks.di.FragmentScope
import com.slyworks.room.AppDatabase
import com.slyworks.room.daos.CallHistoryDao
import com.slyworks.room.daos.FCMTokenDao
import com.slyworks.room.daos.MessageDao
import com.slyworks.room.daos.PersonDao
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
    fun provideAppDatabase(@Named("application_context")
                           context:Context):AppDatabase{
        return AppDatabase.getInstance(context)
    }

    @Provides
    @ApplicationScope
    fun providePersonDao(@Named("application_context")
                         context:Context):PersonDao{
        return AppDatabase.getInstance(context).getPersonDao();
    }

    @Provides
    @ApplicationScope
    fun provideMessageDao(@Named("application_context")
                          context:Context): MessageDao {
        return AppDatabase.getInstance(context).getMessageDao();
    }

    @Provides
    @ApplicationScope
    fun provideCallHistoryDao(@Named("application_context")
                              context:Context):CallHistoryDao{
        return AppDatabase.getInstance(context).getCallHistoryDao();
    }

}