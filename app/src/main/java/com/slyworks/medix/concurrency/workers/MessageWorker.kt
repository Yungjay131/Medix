package com.slyworks.medix.concurrency.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.slyworks.communication.MessageManager
import com.slyworks.medix.App
import com.slyworks.medix.appComponent
import com.slyworks.models.room_models.Message
import com.slyworks.room.daos.MessageDao
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject


/**
 *Created by Joshua Sylvanus, 11:10 AM, 16/05/2022.
 */
class MessageWorker(private val context: Context,
                    params:WorkerParameters) : CoroutineWorker(context, params) {
    //region Vars
    @Inject
    lateinit var messageDao: MessageDao
    @Inject
    lateinit var messageManager: MessageManager
    //endregion

    init{
        (applicationContext as App).appComponent
            .workerComponentBuilder()
            .build()
            .inject(this)
    }

    override suspend fun doWork(): Result {
        val l:List<Message> = messageDao.getUnsentMessages()

        l.forEach {
          messageManager.sendMessage(it)
                        .subscribe()
        }

         return Result.success()
    }

}