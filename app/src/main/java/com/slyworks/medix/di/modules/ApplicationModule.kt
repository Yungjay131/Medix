package com.slyworks.medix.di.modules

import android.content.Context
import com.slyworks.auth.di.AuthApplicationScopedModule
import com.slyworks.communication.CallManager
import com.slyworks.communication.CloudMessageManager
import com.slyworks.communication.ConnectionStatusManager
import com.slyworks.communication.di.CommunicationModule
import com.slyworks.controller.di.ControllerModule
import com.slyworks.di.ActivityScope
import com.slyworks.di.ApplicationScope
import com.slyworks.di.BaseActivityScope
import com.slyworks.firebase_commons.di.FirebaseCommonsModule
import com.slyworks.medix.helpers.ListenerManager
import com.slyworks.medix.helpers.NotificationHelper
import com.slyworks.medix.helpers.VibrationManager
import com.slyworks.network.di.NetworkModule
import com.slyworks.userdetails.di.UserDetailsUtilsModule
import com.slyworks.utils.di.UtilsModule
import dagger.Module
import dagger.Provides
import javax.inject.Named

@Module(includes = [
    UserDetailsUtilsModule::class,
    UtilsModule::class,
    NetworkModule::class,
    FirebaseCommonsModule::class,
    ControllerModule::class,
    CommunicationModule::class,
    AuthApplicationScopedModule::class
])
object ApplicationModule{
    @ApplicationScope
    @Provides
    fun provideVibrationManager(context: Context):VibrationManager
     = VibrationManager(context)

    @ApplicationScope
    @Provides
    fun provideNotificationHelper():NotificationHelper
       = NotificationHelper()

    @ApplicationScope
    @Provides
    fun provideListenerManager(callManager: CallManager,
                               cloudMessageManager: CloudMessageManager,
                               notificationHelper: NotificationHelper,
                               connectionStatusManager: ConnectionStatusManager): ListenerManager =
        ListenerManager(callManager, cloudMessageManager, notificationHelper, connectionStatusManager)
}
