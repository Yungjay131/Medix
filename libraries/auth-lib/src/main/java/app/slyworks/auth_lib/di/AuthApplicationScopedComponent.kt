package app.slyworks.auth_lib.di

import app.slyworks.auth_lib.LoginManager
import app.slyworks.auth_lib.PersonsManager
import app.slyworks.auth_lib.UsersManager
import app.slyworks.crypto_lib.di.CryptoComponent
import app.slyworks.data_lib.DataManager
import app.slyworks.data_lib.di.DataComponent
import app.slyworks.di_base_lib.ApplicationScope
import app.slyworks.di_base_lib.AuthLibScope
import app.slyworks.firebase_commons_lib.FirebaseUtils
import app.slyworks.firebase_commons_lib.di.FirebaseCommonsComponent
import app.slyworks.utils_lib.PreferenceManager
import app.slyworks.utils_lib.TaskManager
import app.slyworks.utils_lib.TimeHelper
import app.slyworks.utils_lib.di.UtilsComponent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import dagger.Binds
import dagger.BindsInstance
import dagger.Component


/**
 * Created by Joshua Sylvanus, 8:48 PM, 02-Dec-2022.
 */
@Component(modules = [AuthApplicationScopedModule::class])
@AuthLibScope
interface AuthApplicationScopedComponent {
    companion object{
        private var instance:AuthApplicationScopedComponent? = null

        @JvmStatic
        fun getInstance():AuthApplicationScopedComponent{
            if(instance == null)
                instance =
                DaggerAuthApplicationScopedComponent.builder()
                    .setCryptoComponent(
                        CryptoComponent.getInstance())
                    .setDataComponent(
                        DataComponent.getInstance())
                    .setFBCComponent(
                        FirebaseCommonsComponent.getInstance())
                    .setUtilsComponent(
                        UtilsComponent.getInstance())
                    .build()

            return instance!!
        }
    }

    fun provideUsersManager():UsersManager
    fun providePersonsManager(): PersonsManager
    fun provideLoginManager(): LoginManager

    @Component.Builder
    interface Builder{
       fun setFBCComponent(@BindsInstance fbcComponent: FirebaseCommonsComponent): Builder
       fun setUtilsComponent(@BindsInstance uComponent:UtilsComponent):Builder
       fun setCryptoComponent(@BindsInstance cCryptoComponent: CryptoComponent):Builder
       fun setDataComponent(@BindsInstance dComponent: DataComponent):Builder
       fun build(): AuthApplicationScopedComponent
    }
}
