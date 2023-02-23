package app.slyworks.voice_call_feature._di

import androidx.appcompat.app.AppCompatActivity
import app.slyworks.base_feature._di.BaseFeatureComponent
import app.slyworks.di_base_lib.FeatureScope
import app.slyworks.voice_call_feature.VoiceCallActivity
import dagger.Component


/**
 * Created by Joshua Sylvanus, 7:50 PM, 05-Dec-2022.
 */

@Component(
    modules = [VoiceCallFeatureModule::class],
    dependencies = [BaseFeatureComponent::class])
@FeatureScope
interface VoiceCallFeatureComponent {
    companion object{
        @JvmStatic
        fun getInitialBuilder():DaggerVoiceCallFeatureComponent.Builder =
            DaggerVoiceCallFeatureComponent.builder()
                .baseFeatureComponent(BaseFeatureComponent.getInstance())
    }

   fun inject(activity: VoiceCallActivity)
}