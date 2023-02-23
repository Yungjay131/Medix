package app.slyworks.medix._di

import androidx.appcompat.app.AppCompatActivity
import app.slyworks.base_feature._di.BaseFeatureComponent
import app.slyworks.di_base_lib.FeatureScope
import app.slyworks.medix.splash.SplashActivity
import dagger.Component


/**
 * Created by Joshua Sylvanus, 6:53 AM, 25/11/2022.
 */
@Component(
    modules = [ApplicationModule::class],
    dependencies = [BaseFeatureComponent::class])
@FeatureScope
interface ApplicationComponent {
    companion object{
        @JvmStatic
        fun getInitialBuilder():DaggerApplicationComponent.Builder =
            DaggerApplicationComponent.builder()
                .baseFeatureComponent(BaseFeatureComponent.getInstance())
    }

    fun inject(activity: SplashActivity)
}