package app.slyworks.base_feature.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import app.slyworks.base_feature._di.FCMTokenUploadWorkerComponent
import app.slyworks.utils_lib.KEY_FCM_UPLOAD_TOKEN
import app.slyworks.data_lib.DataManager
import app.slyworks.data_lib.firebase.FirebaseUtils
import app.slyworks.data_lib.helpers.storage.IUserDetailsHelper
import app.slyworks.utils_lib.FBU_FIREBASE_UID
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


/**
 *Created by Joshua Sylvanus, 10:47 AM, 16/05/2022.
 */
class FCMTokenUploadWorker(private val context: Context,
                           params:WorkerParameters) : CoroutineWorker(context, params) {
    @Inject
    lateinit var firebaseUtils: FirebaseUtils

    @Inject
    lateinit var userDetailsHelper: IUserDetailsHelper

    init{
       FCMTokenUploadWorkerComponent.getInitialBuilder()
           .build()
           .inject(this)
    }


    override suspend fun doWork(): Result {
        /*ensure there is a signed in user first*/
        val token:String = inputData.getString(KEY_FCM_UPLOAD_TOKEN) ?: return Result.failure()

        val UID:String? = userDetailsHelper.getUserDetailsProperty<String>(FBU_FIREBASE_UID)
        if(UID.isNullOrEmpty())
            return Result.success()

        return suspendCoroutine<Result> { continuation ->
            firebaseUtils.getUserDataRefForWorkManager(UID)
                .setValue(token)
                .addOnCompleteListener {
                    if (it.isSuccessful)
                        continuation.resume(Result.success())
                    else
                        continuation.resume(Result.retry())
                }
        }

    }


}