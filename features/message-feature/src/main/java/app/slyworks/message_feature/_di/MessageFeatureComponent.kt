package app.slyworks.message_feature._di

import app.slyworks.base_feature._di.BaseFeatureComponent
import app.slyworks.di_base_lib.FeatureScope
import app.slyworks.message_feature.message.MessageActivity
import dagger.Component


/**
 * Created by Joshua Sylvanus, 6:43 PM, 05-Dec-2022.
 */

@Component(
    modules = [MessageFeatureModule::class],
    dependencies = [
         BaseFeatureComponent::class ])
@FeatureScope
interface MessageFeatureComponent {
    companion object{
        @JvmStatic
        fun getInitialBuilder():DaggerMessageFeatureComponent.Builder =
            DaggerMessageFeatureComponent.builder()
                .baseFeatureComponent(BaseFeatureComponent.getInstance())
    }

    fun inject(activity: MessageActivity)
}