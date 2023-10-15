package app.slyworks.base_feature._di

import app.slyworks.base_feature.workers.FCMTokenUploadWorker
import app.slyworks.base_feature._di.scopes.WorkerScope
import dagger.Component


/**
 *Created by Joshua Sylvanus, 12:40 AM, 24-Feb-2023.
 */
@Component(dependencies = [BaseFeatureComponent::class])
@WorkerScope
interface FCMTokenUploadWorkerComponent {
   companion object{
       @JvmStatic
       fun getInitialBuilder():DaggerFCMTUWorkerComponent.Builder =
           DaggerFCMTUWorkerComponent.builder()
               .baseFeatureComponent(BaseFeatureComponent.getInstance())
   }

    fun inject(worker:FCMTokenUploadWorker)
}