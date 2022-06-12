package com.slyworks.medix.concurrency

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.slyworks.constants.KEY_UPLOAD_USER_PROFILE
import com.slyworks.medix.UserDetailsUtils
import com.slyworks.medix.UsersManager
import com.slyworks.medix.getUserDataForUIDRef
import com.slyworks.medix.utils.PreferenceManager
import com.slyworks.models.room_models.FBUserDetails
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest


/**
 *Created by Joshua Sylvanus, 1:43 PM, 16/05/2022.
 */
class ProfileUpdateWorker(private var context: Context, params:WorkerParameters) : CoroutineWorker(context,params) {

    override suspend fun doWork(): Result {
        val isChanged:Boolean = PreferenceManager.get(KEY_UPLOAD_USER_PROFILE, false) ?: return Result.failure()
       if(!isChanged) return Result.failure()

        val job: Deferred<Result> = CoroutineScope(Dispatchers.IO).async {
            var r:Result = Result.retry()
            val childJob:Deferred<Unit> = async {
                val childJob1:Deferred<FBUserDetails?> = async inner_async@{
                    var details: FBUserDetails? = null

                    val childJob2:Deferred<Unit> = async {
                        UsersManager.getUserFromDataStore()
                            .collectLatest {
                                UserDetailsUtils.user = it
                                details = it
                                this.coroutineContext.cancel()
                            }
                    }

                    childJob2.await()
                    return@inner_async details
                }

                val userDetails = childJob1.await()
                getUserDataForUIDRef(userDetails!!.firebaseUID)
                    .setValue(userDetails)
                    .addOnCompleteListener {
                        if(it.isSuccessful){
                            r = Result.success()
                            this.coroutineContext.cancel()
                        } else{
                            r = Result.retry()
                            this.coroutineContext.cancel()
                        }
                    }

            }
            childJob.await()
            return@async r
        }

        return job.await()
    }
}