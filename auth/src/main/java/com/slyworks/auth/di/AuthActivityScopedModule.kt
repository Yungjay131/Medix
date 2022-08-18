package com.slyworks.auth.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.slyworks.auth.RegistrationManager
import com.slyworks.di.ActivityScope
import com.slyworks.firebase_commons.FirebaseUtils
import dagger.Module
import dagger.Provides


/**
 *Created by Joshua Sylvanus, 9:37 PM, 23/07/2022.
 */

@Module(includes = [
      //FirebaseCommonsActivityScopedModule::class
     ])
class AuthActivityScopedModule {
    @Provides
    @ActivityScope
    fun provideRegistrationManager(firebaseAuth: FirebaseAuth,
                                   firebaseMessaging: FirebaseMessaging,
                                   firebaseUtils: FirebaseUtils):RegistrationManager{
        return RegistrationManager(firebaseAuth,
                                   firebaseMessaging,
                                   firebaseUtils)
    }

}