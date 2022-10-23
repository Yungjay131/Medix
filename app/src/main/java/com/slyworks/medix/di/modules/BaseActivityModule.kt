package com.slyworks.medix.di.modules

import com.slyworks.communication.CallManager
import com.slyworks.communication.CloudMessageManager
import com.slyworks.communication.ConnectionStatusManager
import com.slyworks.di.ApplicationScope
import com.slyworks.di.BaseActivityScope
import com.slyworks.medix.helpers.ListenerManager
import com.slyworks.medix.helpers.NotificationHelper
import dagger.Module
import dagger.Provides


/**
 *Created by Joshua Sylvanus, 8:16 PM, 15/10/2022.
 */

@BaseActivityScope
@Module
object BaseActivityModule {
    @BaseActivityScope
    @Provides
    fun provideListenerManager(callManager: CallManager,
                               cloudMessageManager: CloudMessageManager,
                               notificationHelper: NotificationHelper,
                               connectionStatusManager: ConnectionStatusManager): ListenerManager =
        ListenerManager(callManager, cloudMessageManager, notificationHelper, connectionStatusManager)
}