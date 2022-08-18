package com.slyworks.firebase_commons.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.slyworks.di.ActivityScope
import com.slyworks.di.ApplicationScope
import com.slyworks.firebase_commons.FirebaseUtils
import dagger.Module
import dagger.Provides


/**
 *Created by Joshua Sylvanus, 10:18 PM, 23/07/2022.
 */

@Module
object FirebaseCommonsModule {
    @Provides
    @ApplicationScope
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    @ApplicationScope
    fun provideFirebaseStorage(): FirebaseStorage {
        return FirebaseStorage.getInstance()
    }
    @Provides
    @ApplicationScope
    fun provideFirebaseDatabase() : FirebaseDatabase{
        return FirebaseDatabase.getInstance()
    }

    @Provides
    @ApplicationScope
    fun provideFirebaseMessaging(): FirebaseMessaging {
        return FirebaseMessaging.getInstance()
    }

    @Provides
    @ApplicationScope
    fun provideFirebaseUtils(firebaseDatabase:FirebaseDatabase,
                             firebaseStorage:FirebaseStorage):FirebaseUtils{
        return FirebaseUtils(firebaseDatabase, firebaseStorage)
    }
}