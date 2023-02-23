package app.slyworks.base_feature._di

import android.content.Context
import app.slyworks.base_feature.services.MFirebaseMessagingService
import app.slyworks.di_base_lib.AppComponent
import app.slyworks.di_base_lib.MFirebaseMSScope
import dagger.BindsInstance
import dagger.Component


/**
 * Created by Joshua Sylvanus, 10:38 AM, 15-Dec-2022.
 */
@Component(
    modules = [MFirebaseMSModule::class],
    dependencies = [BaseFeatureComponent::class])
@MFirebaseMSScope
interface MFirebaseMSComponent {
    companion object {
        @JvmStatic
        fun getInitialBuilder(): DaggerMFirebaseMSComponent.Builder =
            DaggerMFirebaseMSComponent.builder()
                .baseFeatureComponent(BaseFeatureComponent.getInstance())
    }

    fun inject(service: MFirebaseMessagingService)
}