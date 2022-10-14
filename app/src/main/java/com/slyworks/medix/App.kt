package com.slyworks.medix

import android.annotation.SuppressLint
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.work.*
import com.facebook.stetho.Stetho
import com.slyworks.constants.KEY_FCM_UPLOAD_TOKEN
import com.slyworks.medix.concurrency.workers.FCMTokenUploadWorker
import com.slyworks.medix.concurrency.workers.MessageWorker
import com.slyworks.medix.concurrency.workers.ProfileUpdateWorker
import com.slyworks.medix.concurrency.workers.StartServiceWorker
import com.slyworks.medix.di.components.ApplicationComponent
import com.slyworks.medix.di.components.DaggerApplicationComponent
import timber.log.Timber


/**
 * Created by Joshua Sylvanus, 3:59 PM, 12/10/2021.
 */

val Context.appComponent: ApplicationComponent
get() = (applicationContext as App)._appComponent

/*TODO:add dynamic feature module*/
class App: Application() {
    //region Vars
    lateinit var _appComponent: ApplicationComponent
    //endregion

    companion object{
        @SuppressLint("StaticFieldLeak")
        private var mContext: Context? = null

        fun getContext(): Context { return mContext!! }

        fun initStartServiceWork(){
            val startServiceWorkRequest:OneTimeWorkRequest =
                OneTimeWorkRequestBuilder<StartServiceWorker>()
                    .setConstraints(
                        Constraints.Builder()
                            .setRequiredNetworkType(NetworkType.CONNECTED)
                            .build())
                    .build()

            WorkManager.getInstance(mContext!!)
                .enqueueUniqueWork(
                    "StartServiceWorker",
                    ExistingWorkPolicy.KEEP,
                    startServiceWorkRequest)
        }

        fun initFCMTokenUploadWork(token:String){
            val data = Data.Builder()
                .putString(KEY_FCM_UPLOAD_TOKEN, token)
                .build()

            val fcmUploadWorkRequest: OneTimeWorkRequest =
                OneTimeWorkRequestBuilder<FCMTokenUploadWorker>()
                    .setInputData(data)
                    .setConstraints(
                        Constraints.Builder()
                            .setRequiredNetworkType(NetworkType.CONNECTED)
                            .build())
                    .build()

            WorkManager.getInstance(mContext!!)
                .enqueueUniqueWork(
                    "FCMTokenUploadWorker",
                    ExistingWorkPolicy.REPLACE,
                    fcmUploadWorkRequest)
        }

        fun initMessageWork(){
            /*re-queued in onStop() of MessageWorker*/
            val messageWorkRequest: OneTimeWorkRequest =
                OneTimeWorkRequestBuilder<MessageWorker>()
                    .setConstraints(
                        Constraints.Builder()
                            .setRequiredNetworkType(NetworkType.CONNECTED)
                            .build())
                    .build()

            WorkManager.getInstance(mContext!!)
                .enqueueUniqueWork(
                    "MessageWorker",
                    ExistingWorkPolicy.APPEND_OR_REPLACE,
                    messageWorkRequest)
        }

        fun initProfileUpdateWork(){
            /*TODO:would be set from settings*/
            val profileUpdateWorkRequest:OneTimeWorkRequest =
                OneTimeWorkRequestBuilder<ProfileUpdateWorker>()
                    .setConstraints(
                        Constraints.Builder()
                            .setRequiredNetworkType(NetworkType.CONNECTED)
                            .build())
                    .build()

            WorkManager.getInstance(mContext!!)
                .enqueueUniqueWork(
                    "ProfileUpdateWorker",
                    ExistingWorkPolicy.REPLACE,
                    profileUpdateWorkRequest)
        }
    }

    override fun onCreate() {
        super.onCreate()
        initDI()
        initTimber()
        initContext()
        initStartServiceWork()
        initStetho()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            initNotificationChannels()
    }

    private fun initDI(){
        /* inject UserDetailUtils, AppDatabase, context etc into dependency graph */
        _appComponent =
        DaggerApplicationComponent
            .builder()
            .componentContext(this)
            .build()
    }

    private fun initStetho(){
        if(!BuildConfig.DEBUG)
            return

        Stetho.initializeWithDefaults(this)
    }

    private fun initTimber(){
        /* to ensure logging does not occur in RELEASE builds*
          the actual dependency is in :models */
        if(!BuildConfig.DEBUG)
            return

        Timber.plant(object: Timber.DebugTree(){
            override fun createStackElementTag(element: StackTraceElement): String {
                return String.format(
                    "%s:%s",
                    element.methodName,
                    super.createStackElementTag(element))
            }
        })
    }

    private fun initContext(){ mContext = this; }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initNotificationChannels(){
        /*would be using ~2 notification channels, 1 for general app notifications, 1 for ForegroundService*/
        createNotificationChannel1()
        createNotificationChannel2()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel1(){
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val CHANNEL_ID = getString(R.string.notification_channel_1_id)
        val channelName = getString(R.string.notification_channel_1_name)
        val channelDescriptionText = getString(R.string.notification_channel_1_description)
        val channelImportance = NotificationManager.IMPORTANCE_HIGH

        val channel: NotificationChannel = NotificationChannel(CHANNEL_ID, channelName, channelImportance)
        channel.setDescription(channelDescriptionText)

        val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel2(){
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val CHANNEL_ID = getString(R.string.notification_channel_2_id)
        val channelName = getString(R.string.notification_channel_2_name)
        val channelDescriptionText = getString(R.string.notification_channel_2_description)
        val channelImportance = NotificationManager.IMPORTANCE_HIGH

        val channel: NotificationChannel = NotificationChannel(CHANNEL_ID, channelName, channelImportance)
        channel.setDescription(channelDescriptionText)

        val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}