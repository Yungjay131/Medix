package com.slyworks.medix.concurrency.workers

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.slyworks.medix.App
import com.slyworks.medix.services.ListenerService
import com.slyworks.medix.utils.showToast


/**
 *Created by Joshua Sylvanus, 12:13 PM, 1/21/2022.
 */
/*the task would be re queued in ListenerService*/
class StartServiceWorker(private val appContext: Context,
                         workerParams:WorkerParameters) : Worker(appContext,workerParams){
    override fun doWork(): Result {
        val intent: Intent = Intent(appContext, ListenerService::class.java)
        ContextCompat.startForegroundService(appContext, intent)

        showToast("StartServiceWorker#doWork executed")

        return Result.success()
    }

    override fun onStopped() {
        super.onStopped()
        val intent:Intent = Intent(appContext, ListenerService::class.java)
        appContext.stopService(intent)
        showToast("StartServiceWorker#onStopped executed")
    }
}
