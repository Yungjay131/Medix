package app.slyworks.requests_feature._di

import app.slyworks.base_feature._di.BaseFeatureComponent
import app.slyworks.di_base_lib.FeatureScope
import app.slyworks.requests_feature.RequestsActivity
import app.slyworks.requests_feature.ViewRequestActivity
import dagger.Component


/**
 * Created by Joshua Sylvanus, 8:12 PM, 20-May-2023.
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

    fun inject(activity:RequestsActivity)
    fun inject(activity:ViewRequestActivity)
}