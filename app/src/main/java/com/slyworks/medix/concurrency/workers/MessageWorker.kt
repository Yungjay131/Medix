package com.slyworks.medix.concurrency.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.slyworks.medix.App
import com.slyworks.medix.managers.DataManager
import com.slyworks.medix.utils.UserDetailsUtils
import com.slyworks.medix.managers.UsersManager
import com.slyworks.data.AppDatabase
import com.slyworks.medix.managers.MessageManager
import com.slyworks.models.room_models.FBUserDetails
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.collectLatest


/**
 *Created by Joshua Sylvanus, 11:10 AM, 16/05/2022.
 */
class MessageWorker(private var context: Context, params:WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val dataManager: DataManager = DataManager()

        val l = AppDatabase.getInstance(context)
            .getMessageDao()
            .getUnsentMessages()

            /*TODO:make sure that any Message with NOT_SENT originated from you*/
         return Result.success()
    }

}