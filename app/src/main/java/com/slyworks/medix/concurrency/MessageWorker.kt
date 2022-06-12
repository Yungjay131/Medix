package com.slyworks.medix.concurrency

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.slyworks.medix.App
import com.slyworks.medix.DataManager
import com.slyworks.medix.UserDetailsUtils
import com.slyworks.medix.UsersManager
import com.slyworks.data.AppDatabase
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

        if(l.isNullOrEmpty()) {
            /*re-enqueuing MessageWork*/
            App.initMessageWork()
            return Result.success()
        }

        val job: Deferred<Result> = CoroutineScope(Dispatchers.IO).async {
                val _l:MutableList<Boolean> = mutableListOf()
                l.forEach { message ->
                    /*TODO:make sure that any Message with NOT_SENT originated from you*/
                    val userDetails: FBUserDetails
                    UsersManager.getUserFromDataStore()
                        .collectLatest {
                           UserDetailsUtils.user = it
                        }

                    val status = dataManager.sendMessage(message)
                    _l.add(status)
                }

            if(_l.any{ !it }) {
                App.initMessageWork()
                return@async Result.retry()
            }
            else{
                App.initMessageWork()
                return@async Result.success()
            }
        }

        return job.await()
    }

}