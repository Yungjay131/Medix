package app.slyworks.video_call_feature._di

import androidx.appcompat.app.AppCompatActivity
import app.slyworks.base_feature._di.BaseFeatureComponent
import app.slyworks.di_base_lib.FeatureScope
import app.slyworks.video_call_feature.VideoCallActivity
import dagger.Component


/**
 * Created by Joshua Sylvanus, 7:50 PM, 05-Dec-2022.
 */
/*
*
*   implementation project(path: ':features:base-feature')
    implementation project(path: ':features:navigation-feature')
    implementation project(path: ':libraries:data-lib')
    implementation project(path: ':libraries:communication-lib')
    implementation project(path: ':libraries:utils-lib')
    implementation project(path: ':libraries:constants-lib')
    implementation project(path: ':libraries:di-base-lib')

* */
@Component(
    modules = [VideoCallFeatureModule::class],
    dependencies = [BaseFeatureComponent::class])
@FeatureScope
interface VideoCallFeatureComponent {
    companion object{
        @JvmStatic
        fun getInitialBuilder():DaggerVideoCallFeatureComponent.Builder =
            DaggerVideoCallFeatureComponent.builder()
                .baseFeatureComponent(BaseFeatureComponent.getInstance())
    }

   fun inject(activity: VideoCallActivity)
}