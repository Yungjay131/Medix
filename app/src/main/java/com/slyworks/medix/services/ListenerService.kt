package com.slyworks.medix.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.slyworks.constants.APP_SERVICE_ID
import com.slyworks.medix.App
import com.slyworks.medix.helpers.ListenerManager
import com.slyworks.medix.helpers.NotificationHelper
import com.slyworks.medix.utils.*


/**
 *Created by Joshua Sylvanus, 12:03 PM, 1/21/2022.
 */

class ListenerService : Service() {
    //region Vars
    private var mListenerManager: ListenerManager? = null
    private var shouldWorkerBeReInitialized:Boolean = false
    //endregion

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        startForeground(APP_SERVICE_ID, NotificationHelper.createAppServiceNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if(ActivityUtils.isThereActivityInForeground()){
            stopSelf()
            shouldWorkerBeReInitialized = false
            return START_NOT_STICKY
        }

        /*initializing class for listening to network events*/
        //mListenerManager = ListenerManager()
        mListenerManager!!.start()

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()

        if(mListenerManager != null) {
            mListenerManager!!.stop()
            mListenerManager = null
        }

        /*re-queueing Work for WorkManager*/
        if(shouldWorkerBeReInitialized)
           App.initStartServiceWork()
    }
}