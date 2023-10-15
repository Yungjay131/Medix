package app.slyworks.base_feature._di

import app.slyworks.base_feature.broadcast_receivers.VoiceCallRequestBroadcastReceiver
import app.slyworks.base_feature._di.scopes.BroadcastReceiverScope
import dagger.Component


/**
 * Created by Joshua Sylvanus, 1:58 PM, 24-Feb-2023.
 */
@Component(dependencies = [BaseFeatureComponent::class])
@BroadcastReceiverScope
interface VoiceCallRequestBRComponent {
    companion object{
        @JvmStatic
        fun getInitialBuilder():DaggerVoiceCallRequestBRComponent.Builder =
            DaggerVoiceCallRequestBRComponent.builder()
                .baseFeatureComponent(BaseFeatureComponent.getInstance())
    }

    fun inject(receiver:VoiceCallRequestBroadcastReceiver)
}