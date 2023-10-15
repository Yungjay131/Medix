package app.slyworks.auth_feature._di

import app.slyworks.auth_feature.login.LoginActivity
import app.slyworks.auth_feature.onboarding.OnBoardingActivity
import app.slyworks.auth_feature.registration.RegistrationActivity
import app.slyworks.base_feature._di.BaseFeatureComponent
import dagger.Component


/**
 * Created by Joshua Sylvanus, 6:53 AM, 25/11/2022.
 */
@Component(
    modules = [AuthFeatureModule::class],
    dependencies = [BaseFeatureComponent::class])
@AuthFeatureScope
interface AuthFeatureComponent {
    companion object {
        @JvmStatic
        fun getInitialBuilder():DaggerAuthFeatureComponent.Builder =
            DaggerAuthFeatureComponent.builder()
                .baseFeatureComponent(BaseFeatureComponent.getInstance())
    }

    fun inject(activity:OnBoardingActivity)
    fun inject(activity:LoginActivity)
    fun inject(activity:RegistrationActivity)
}