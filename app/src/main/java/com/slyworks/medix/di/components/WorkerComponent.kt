package com.slyworks.medix.di.components

import com.slyworks.di.ApplicationScope
import com.slyworks.di.WorkerScope
import com.slyworks.medix.concurrency.workers.FCMTokenUploadWorker
import com.slyworks.medix.concurrency.workers.MessageWorker
import com.slyworks.medix.concurrency.workers.ProfileUpdateWorker
import com.slyworks.medix.concurrency.workers.StartServiceWorker
import dagger.Subcomponent


/**
 *Created by Joshua Sylvanus, 8:50 PM, 16/08/2022.
 */
@WorkerScope
@Subcomponent
interface WorkerComponent {
    fun inject(worker:MessageWorker)
    fun inject(worker:ProfileUpdateWorker)
    fun inject(worker:FCMTokenUploadWorker)
    fun inject(worker:StartServiceWorker)

    @Subcomponent.Builder
    interface Builder{
        fun build():WorkerComponent
    }
}