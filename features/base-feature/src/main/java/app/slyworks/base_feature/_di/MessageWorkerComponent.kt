package app.slyworks.base_feature._di

import app.slyworks.base_feature.workers.MessageWorker
import app.slyworks.base_feature._di.scopes.WorkerScope
import dagger.Component


/**
 *Created by Joshua Sylvanus, 1:36 PM, 24-Feb-2023.
 */
@Component(dependencies = [BaseFeatureComponent::class])
@WorkerScope
interface MessageWorkerComponent {
    companion object{
        @JvmStatic
        fun getInitialBuilder():DaggerMessageWorkerComponent.Builder =
            DaggerMessageWorkerComponent.builder()
                .baseFeatureComponent(BaseFeatureComponent.getInstance())
    }

    fun inject(worker: MessageWorker)
}