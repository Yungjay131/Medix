package com.slyworks.auth.di

import com.google.firebase.auth.FirebaseAuth
import com.slyworks.auth.LoginManager
import com.slyworks.auth.PersonsManager
import com.slyworks.auth.UsersManager
import com.slyworks.di.ActivityScope
import com.slyworks.di.ApplicationScope
import com.slyworks.firebase_commons.FirebaseUtils
import com.slyworks.room.daos.MessageDao
import com.slyworks.room.daos.PersonDao
import com.slyworks.userdetails.UserDetailsUtils
import com.slyworks.utils.PreferenceManager
import com.slyworks.utils.TimeUtils
import dagger.Module
import dagger.Provides


/**
 *Created by Joshua Sylvanus, 9:48 PM, 11/08/2022.
 */
@Module(includes = [])
class AuthApplicationScopedModule {
    @Provides
    @ApplicationScope
    fun provideUsersManager(personDao: PersonDao,
                            messageDao: MessageDao,
                            userDetailsUtils: UserDetailsUtils,
                            firebaseUtils: FirebaseUtils): UsersManager {
        return UsersManager(personDao,
            messageDao,
            userDetailsUtils,
            firebaseUtils)
    }

    @Provides
    @ApplicationScope
    fun providePersonsManager(personDao: PersonDao): PersonsManager =
        PersonsManager(personDao)

    @Provides
    @ApplicationScope
    fun provideLoginManager(preferenceManager: PreferenceManager,
                            firebaseAuth: FirebaseAuth,
                            usersManager:UsersManager,
                            userDetailsUtils: UserDetailsUtils,
                            firebaseUtils: FirebaseUtils,
                            timeUtils:TimeUtils): LoginManager {
        return LoginManager(preferenceManager,
            firebaseAuth,
            usersManager,
            userDetailsUtils,
            firebaseUtils,
            timeUtils);
    }
}