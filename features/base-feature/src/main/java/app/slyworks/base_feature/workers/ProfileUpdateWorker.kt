package app.slyworks.base_feature.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import app.slyworks.utils_lib.KEY_UPLOAD_USER_PROFILE
import app.slyworks.data_lib.DataManager
import app.slyworks.data_lib.vmodels.FBUserDetailsVModel
import app.slyworks.data_lib.firebase.FirebaseUtils
import app.slyworks.utils_lib.PreferenceManager
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


/**
 *Created by Joshua Sylvanus, 1:43 PM, 16/05/2022.
 */
class ProfileUpdateWorker(private val context: Context,
                          params:WorkerParameters) : CoroutineWorker(context,params) {
    //region Vars
    @Inject
    lateinit var preferenceManager: PreferenceManager
    @Inject
    lateinit var dataManager: DataManager
    @Inject
    lateinit var firebaseUtils: FirebaseUtils
    //endregion

    init{
        /*(applicationContext as App).appComponent
            .workerComponentBuilder()
            .build()
            .inject(this)*/
    }

    override suspend fun doWork(): Result {
       val isChanged:Boolean = preferenceManager.get(KEY_UPLOAD_USER_PROFILE, false) ?: return Result.failure()
       if(!isChanged)
           return Result.failure()

        var r:Result = Result.retry()

        val details: FBUserDetailsVModel =
            dataManager.getUserDetailsProperty<FBUserDetailsVModel>("userDetails")
            ?: return Result.success()

        /*TODO:there should be a try-catch-finally here*/
        return suspendCoroutine<Result> { continuation ->
            firebaseUtils.getUserDataForUIDRef(details.firebaseUID)
                .setValue(details)
                .addOnCompleteListener {
                    if (it.isSuccessful)
                        continuation.resume(Result.success())
                    else {
                        continuation.resume(Result.retry())
                    }
                }
        }
    }
}