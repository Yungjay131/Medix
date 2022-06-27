package com.slyworks.medix.concurrency

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.slyworks.constants.KEY_FCM_UPLOAD_TOKEN
import com.slyworks.medix.getUserDataRef
import kotlinx.coroutines.*


/**
 *Created by Joshua Sylvanus, 10:47 AM, 16/05/2022.
 */
class FCMTokenUploadWorker(private var context: Context,
                           params:WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        //val token:String = PreferenceManager.get<String>(KEY_FCM_REGISTRATION, null) ?: return Result.failure()

        val token:String = inputData.getString(KEY_FCM_UPLOAD_TOKEN) ?: return Result.failure()

        val job:Deferred<Result> = CoroutineScope(Dispatchers.IO).async {
            var r: Result = Result.retry()
            val childJob: Deferred<Unit> = async async@{
                getUserDataRef()
                    .setValue(token)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            r = Result.success()
                        } else {
                            r = Result.retry()
                        }

                        this.coroutineContext.cancel()
                    }
            }

            childJob.await()
            return@async r
        }

        return job.await()
    }


}