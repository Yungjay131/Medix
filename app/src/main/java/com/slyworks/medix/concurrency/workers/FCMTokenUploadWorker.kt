package com.slyworks.medix.concurrency.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.slyworks.constants.KEY_FCM_UPLOAD_TOKEN
import com.slyworks.auth.UsersManager
import com.slyworks.firebase_commons.FirebaseUtils
import com.slyworks.medix.App
import com.slyworks.medix.appComponent
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject


/**
 *Created by Joshua Sylvanus, 10:47 AM, 16/05/2022.
 */
class FCMTokenUploadWorker(private val context: Context,
                           params:WorkerParameters) : CoroutineWorker(context, params) {
    //region Vars
    @Inject
    lateinit var firebaseUtils: FirebaseUtils
    @Inject
    lateinit var usersManager: UsersManager
    //endregion

    init{
        (applicationContext as App).appComponent
            .workerComponentBuilder()
            .build()
            .inject(this)
    }


    override suspend fun doWork(): Result {
        /*ensure there is a signed in use first*/
        val _job:Deferred<String?> =
            CoroutineScope(Dispatchers.Default).async {
                var result:String? = null

                val innerJob:Deferred<Unit> = async {
                    usersManager.getUserFromDataStore()
                        .collectLatest {
                            result = it.firebaseUID
                            this.coroutineContext.cancel()
                        }
                }

                innerJob.await()
                return@async result
            }

        val token:String = inputData.getString(KEY_FCM_UPLOAD_TOKEN) ?: return Result.failure()
        val UID:String = _job.await() ?: return Result.failure()

        val job:Deferred<Result> = CoroutineScope(Dispatchers.IO).async {
            var r: Result = Result.retry()
            val childJob: Deferred<Unit> = async async@{
              firebaseUtils.getUserDataRefForWorkManager(UID)
                    .setValue(token)
                    .addOnCompleteListener {
                        r =
                        if (it.isSuccessful) {
                            Result.success()
                        } else {
                            Result.retry()
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