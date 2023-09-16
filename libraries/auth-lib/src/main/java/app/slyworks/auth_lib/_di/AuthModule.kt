package app.slyworks.auth_lib._di

import app.slyworks.auth_lib.*
import app.slyworks.data_lib.CryptoHelper
import app.slyworks.data_lib.DataManager
import app.slyworks.firebase_commons_lib.FirebaseUtils
import app.slyworks.utils_lib.PreferenceManager
import app.slyworks.utils_lib.TaskManager
import app.slyworks.utils_lib.TimeHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import dagger.Module
import dagger.Provides
import javax.inject.Singleton


/**
 * Created by Joshua Sylvanus, 9:48 PM, 11/08/2022.
 */
@Module
object AuthModule {
    @Provides
    fun provideUsersManager(fu: FirebaseUtils,
                            dm: DataManager): UsersManager =
    UsersManager(fu, dm)

    @Provides
    fun providePersonsManager(dm: DataManager): PersonsManager =
    PersonsManager(dm)

    @Provides
    fun provideLoginManager(pm: PreferenceManager,
                            fa: FirebaseAuth,
                            um: UsersManager,
                            fu: FirebaseUtils,
                            th:TimeHelper,
                            ch: CryptoHelper,
                            dm:DataManager,
                            asl : MAuthStateListener): LoginManager =
        LoginManager(pm, fa, um, fu, th, ch, dm, asl);

    @Provides
    fun provideVerificationHelper(asl: MAuthStateListener,
                                  fa: FirebaseAuth,
                                  fu: FirebaseUtils,
                                  ch: CryptoHelper
    ): VerificationHelper =
        VerificationHelper(asl, fa, fu, ch)

    @Provides
    fun provideRegistrationManager(fa: FirebaseAuth,
                                   fm: FirebaseMessaging,
                                   fu: FirebaseUtils,
                                   tm: TaskManager,
                                   ch: CryptoHelper,
                                   asl: MAuthStateListener): RegistrationManager =
        RegistrationManager(fa, fm, fu, tm, ch, asl)

    @Provides
    @Singleton
    fun provideMAuthStateListener(pf:PreferenceManager):MAuthStateListener =
        MAuthStateListener(pf)
}