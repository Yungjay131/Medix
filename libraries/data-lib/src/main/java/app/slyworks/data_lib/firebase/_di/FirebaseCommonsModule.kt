package app.slyworks.firebase_commons_lib._di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import app.slyworks.data_lib.firebase.FirebaseUtils
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides


/**
 * Created by Joshua Sylvanus, 10:18 PM, 23/07/2022.
 */

@Module
object FirebaseCommonsModule {
    @Provides
    fun provideFirebaseAuth(): FirebaseAuth =
        FirebaseAuth.getInstance()

    @Provides
    fun provideFirebaseStorage(): FirebaseStorage =
        FirebaseStorage.getInstance()

    @Provides
    fun provideFirebaseDatabase() : FirebaseDatabase =
        FirebaseDatabase.getInstance()

    @Provides
    fun provideFirebaseMessaging(): FirebaseMessaging =
        FirebaseMessaging.getInstance()

    @Provides
    fun provideFirebaseFirestore():FirebaseFirestore =
        FirebaseFirestore.getInstance()

    @Provides
    fun provideFirebaseUtils(firebaseDatabase:FirebaseDatabase,
                             firebaseStorage:FirebaseStorage,
                             firebaseFirestore:FirebaseFirestore): FirebaseUtils =
        FirebaseUtils(firebaseDatabase, firebaseStorage, firebaseFirestore)
}