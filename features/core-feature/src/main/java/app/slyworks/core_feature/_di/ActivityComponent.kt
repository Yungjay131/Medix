package app.slyworks.core_feature._di

import app.slyworks.base_feature._di.BaseFeatureComponent
import app.slyworks.core_feature.location.LocationActivity
import app.slyworks.core_feature.main.HomeActivity
import app.slyworks.di_base_lib.FeatureScope
import dagger.Component


/**
 * Created by Joshua Sylvanus, 6:28 PM, 05-Dec-2022.
 */

@Component(
    modules = [ActivityViewModelModule::class],
    dependencies = [BaseFeatureComponent::class])
@FeatureScope
interface ActivityComponent {
    companion object{
        @JvmStatic
        fun getInitialBuilder():DaggerActivityComponent.Builder =
            DaggerActivityComponent.builder()
                .baseFeatureComponent(BaseFeatureComponent.getInstance())
    }

    fun inject(activity:HomeActivity)
    fun inject(activity:LocationActivity)

    fun fragmentComponentBuilder(): FragmentComponent.Builder
}