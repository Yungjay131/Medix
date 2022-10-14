package com.slyworks.medix.di.modules

import com.slyworks.communication.CallManager
import com.slyworks.communication.CloudMessageManager
import com.slyworks.di.ActivityScope
import com.slyworks.di.ApplicationScope
import com.slyworks.medix.helpers.ListenerManager
import com.slyworks.medix.helpers.NotificationHelper
import com.slyworks.medix.helpers.PermissionManager
import dagger.Module
import dagger.Provides

@Module
object ActivityModule {
    @ActivityScope
    @Provides
    fun providePermissionManager():PermissionManager = PermissionManager()

    @ActivityScope
    @Provides
    fun provideListenerManager(callManager: CallManager,
                               cloudMessageManager: CloudMessageManager,
                               notificationHelper: NotificationHelper): ListenerManager =
        ListenerManager(callManager, cloudMessageManager, notificationHelper)
}
