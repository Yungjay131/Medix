package app.slyworks.base_feature._di

import app.slyworks.base_feature.broadcast_receivers.VideoCallRequestBroadcastReceiver
import app.slyworks.base_feature._di.scopes.BroadcastReceiverScope
import dagger.Component


/**
 *Created by Joshua Sylvanus, 1:58 PM, 24-Feb-2023.
 */
@Component(dependencies = [BaseFeatureComponent::class])
@BroadcastReceiverScope
interface VideoCallRequestBRComponent {
    companion object{
        @JvmStatic
        fun getInitialBuilder():DaggerVideoCallRequestBRComponent.Builder =
            DaggerVideoCallRequestBRComponent.builder()
                .baseFeatureComponent(BaseFeatureComponent.getInstance())
    }

    fun inject(receiver:VideoCallRequestBroadcastReceiver)
}