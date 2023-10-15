package app.slyworks.base_feature._di

import android.content.Context
import app.slyworks.base_feature.NotificationHelper
import app.slyworks.base_feature.PermissionManager
import app.slyworks.base_feature.VibrationManager
import app.slyworks.data_lib._di.DataModule
import app.slyworks.firebase_commons_lib._di.FirebaseCommonsModule
import app.slyworks.location_lib._di.LocationModule
import app.slyworks.base_feature.network_register._di.NetworkModule
import app.slyworks.room_lib._di.RoomModule
import app.slyworks.utils_lib._di.UtilsModule
import dagger.Module
import dagger.Provides
import javax.inject.Singleton


/**
 * Created by Joshua Sylvanus, 9:45 PM, 02-Dec-2022.
 */
@Module(
    includes = [
        DataModule::class,
        RoomModule::class,
        FirebaseCommonsModule::class,
        NetworkModule::class,
        LocationModule::class,
        UtilsModule::class
    ]
)
object BaseFeatureModule {
    @Provides
    fun providePermissionManager():PermissionManager =
        PermissionManager()

    @Provides
    @Singleton
    fun provideNotificationHelper(context: Context):NotificationHelper =
        NotificationHelper(context)

  /* @Provides
   @Singleton
   fun provideListenerManager(ctx:Context,
                              cm:CallManager,
                              crm:ConsultationRequestsManager,
                              nh: NotificationHelper,
                              csm: ConnectionStatusManager):ListenerManager =
       ListenerManager(ctx, cm, crm, nh, csm)*/

    @Provides
    fun provideVibrationManager(context: Context): VibrationManager =
        VibrationManager(context)
}