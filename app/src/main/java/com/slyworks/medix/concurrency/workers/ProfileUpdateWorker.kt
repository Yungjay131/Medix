package com.slyworks.medix.concurrency.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.slyworks.auth.UsersManager
import com.slyworks.constants.KEY_UPLOAD_USER_PROFILE
import com.slyworks.firebase_commons.FirebaseUtils
import com.slyworks.medix.App
import com.slyworks.medix.appComponent
import com.slyworks.models.room_models.FBUserDetails
import com.slyworks.userdetails.UserDetailsUtils
import com.slyworks.utils.PreferenceManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject


/**
 *Created by Joshua Sylvanus, 1:43 PM, 16/05/2022.
 */
class ProfileUpdateWorker(private var context: Context,
                          params:WorkerParameters) : CoroutineWorker(context,params) {
    //region Vars
    @Inject
    lateinit var preferenceManager: PreferenceManager
    @Inject
    lateinit var usersManager:UsersManager
    @Inject
    lateinit var userDetailsUtils: UserDetailsUtils
    @Inject
    lateinit var firebaseUtils: FirebaseUtils
    //endregion

    init{
        (applicationContext as App).appComponent
            .workerComponentBuilder()
            .build()
            .inject(this)
    }

    override suspend fun doWork(): Result {
       val isChanged:Boolean = preferenceManager.get(KEY_UPLOAD_USER_PROFILE, false) ?: return Result.failure()
       if(!isChanged)
           return Result.failure()

        val job: Deferred<Result> = CoroutineScope(Dispatchers.IO).async {
            var r:Result = Result.retry()
            val childJob:Deferred<Unit> = async {
                val childJob1:Deferred<FBUserDetails?> = async inner_async@{
                    var details: FBUserDetails? = null

                    val childJob2:Deferred<Unit> = async {
                        usersManager.getUserFromDataStore()
                            .collectLatest {
                                userDetailsUtils.user = it
                                details = it
                                this.coroutineContext.cancel()
                            }
                    }

                    childJob2.await()
                    return@inner_async details
                }

                val userDetails = childJob1.await()
                firebaseUtils.getUserDataForUIDRef(userDetails!!.firebaseUID)
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