package com.slyworks.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.slyworks.data.daos.CallHistoryDao
import com.slyworks.data.daos.MessageDao
import com.slyworks.data.daos.MessagePersonDao
import com.slyworks.data.daos.PersonDao
import com.slyworks.models.room_models.CallHistory
import com.slyworks.models.room_models.Message
import com.slyworks.models.room_models.MessagePerson
import com.slyworks.models.room_models.Person


/**
 *Created by Joshua Sylvanus, 3:23 PM, 1/9/2022.
 */
@Database(
    entities = [
        MessagePerson::class,
        Person::class,
        Message::class,
        CallHistory::class,
    ],
    version = 1, exportSchema = true )
abstract class AppDatabase : RoomDatabase(){
    companion object{
        //region Vars
        private var INSTANCE: AppDatabase? = null
        private val mRoomCallback: RoomDatabase.Callback = object : RoomDatabase.Callback(){
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
            }

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
            }

            override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
                super.onDestructiveMigration(db)
            }
        }
        //endregion

        @Synchronized
        fun getInstance(context: Context): AppDatabase {
            if(INSTANCE == null){
                INSTANCE = Room.databaseBuilder(
                                context.applicationContext,
                                AppDatabase::class.java,
                           "AppDatabase")
                         .fallbackToDestructiveMigration()
                         .addCallback(mRoomCallback)
                         .build()
            }

            return INSTANCE!!
        }
    }

    abstract fun getMessageDao(): MessageDao
    abstract fun getMessagePersonDao(): MessagePersonDao
    abstract fun getPersonDao(): PersonDao
    abstract fun getCallHistoryDao(): CallHistoryDao

}