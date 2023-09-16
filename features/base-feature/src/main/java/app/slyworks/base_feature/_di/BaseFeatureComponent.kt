package app.slyworks.base_feature._di

import android.content.Context
import app.slyworks.auth_lib.*
import app.slyworks.base_feature.ListenerManager
import app.slyworks.base_feature.NotificationHelper
import app.slyworks.base_feature.PermissionManager
import app.slyworks.base_feature.VibrationManager
import app.slyworks.communication_lib.*
import app.slyworks.data_lib.CryptoHelper
import app.slyworks.data_lib.DataManager
import app.slyworks.di_base_lib.BaseFeatureScope
import app.slyworks.data_lib.FCMClientApi
import app.slyworks.di_base_lib.AppComponent
import app.slyworks.firebase_commons_lib.FirebaseUtils
import app.slyworks.location_lib.LocationTracker
import app.slyworks.network_lib.NetworkRegister
import app.slyworks.room_lib.daos.CallHistoryDao
import app.slyworks.room_lib.daos.ConsultationRequestDao
import app.slyworks.room_lib.daos.MessageDao
import app.slyworks.room_lib.daos.PersonDao
import app.slyworks.utils_lib.PreferenceManager
import app.slyworks.utils_lib.TimeHelper
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton


/**
 * Created by Joshua Sylvanus, 9:51 PM, 02-Dec-2022.
 */

@Component(modules = [BaseFeatureModule::class])
@Singleton
interface BaseFeatureComponent {
    companion object{
        private var instance: BaseFeatureComponent? = null

        @JvmStatic
        fun getInstance():BaseFeatureComponent{
            if(instance == null)
                instance =
                DaggerBaseFeatureComponent.builder()
                    .context(AppComponent.getContext())
                    .build()

            return instance!!
        }
    }

    fun getContext():Context

    fun getPermissionManager(): PermissionManager
    fun getListenerManager(): ListenerManager
    fun getVibrationManager(): VibrationManager
    fun getNotificationHelper(): NotificationHelper

    fun getPreferenceManager(): PreferenceManager
    fun getTimeHelper(): TimeHelper

    fun getLoginManager(): LoginManager
    fun getUsersManager(): UsersManager
    fun getRegistrationManager(): RegistrationManager
    fun getVerificationHelper(): VerificationHelper
    fun getPersonsManager(): PersonsManager
    fun getMAuthStateListener(): MAuthStateListener

    fun getCallManager():CallManager
    fun getMessageManager(): MessageManager
    fun getCallHistoryManager(): CallHistoryManager
    fun getConsultationRequestsManager(): ConsultationRequestsManager
    fun getConnectionStatusManager(): ConnectionStatusManager

    fun getDataManager(): DataManager

    fun getNetworkRegister(): NetworkRegister

    fun getCryptoHelper(): CryptoHelper

    fun getFCMApiClient(): FCMClientApi

    fun getFirebaseUtils():FirebaseUtils

    fun getLocationTracker(): LocationTracker

   /* fun getMessageDao(): MessageDao
    fun getPersonDao(): PersonDao
    fun getConsultationRequestDao(): ConsultationRequestDao
    fun getCallHistoryDao(): CallHistoryDao*/

    @Component.Builder
    interface Builder{
        @BindsInstance
        fun context(context: Context):Builder
        fun build():BaseFeatureComponent
    }
}