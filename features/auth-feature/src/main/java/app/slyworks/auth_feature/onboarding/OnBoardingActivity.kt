package app.slyworks.auth_feature.onboarding

import android.content.res.Configuration
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import app.slyworks.auth_feature._di.AuthFeatureComponent
import app.slyworks.auth_feature.databinding.ActivityOnBoardingBinding
import app.slyworks.base_feature.BaseActivity
import app.slyworks.base_feature.MOnBackPressedCallback
import app.slyworks.utils_lib.EXTRA_IS_ACTIVITY_RECREATED
import app.slyworks.utils_lib.GENERAL
import app.slyworks.base_feature.custom_views.NetworkStatusView
import app.slyworks.base_feature.custom_views.setStatus
import app.slyworks.utils_lib.LOGIN_ACTIVITY_INTENT_FILTER
import app.slyworks.utils_lib.REGISTRATION_ACTIVITY_INTENT_FILTER
import app.slyworks.utils_lib.utils.isInLandscape
import dev.joshuasylvanus.navigator.Navigator
import javax.inject.Inject

class OnBoardingActivity : BaseActivity() {
    private lateinit var binding:ActivityOnBoardingBinding

    @Inject
    override lateinit var viewModel: OnBoardingActivityViewModel

    override fun cancelOngoingOperation() {}

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(EXTRA_IS_ACTIVITY_RECREATED, true)
        super.onSaveInstanceState(outState)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        initDI()

        super.onCreate(savedInstanceState)

        binding = ActivityOnBoardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initData()
        initViews()
    }

    private fun initDI(){
        /*AuthFeatureComponent.getInitialBuilder()
            .build()
            .inject(this)
    */
    }

    private fun initData() {
        this.onBackPressedDispatcher
            .addCallback(this, MOnBackPressedCallback(this))
    }

    private fun initViews() {
        binding.btnGetStarted.setOnClickListener {
            Navigator.intentFor(this, REGISTRATION_ACTIVITY_INTENT_FILTER)
                .navigate()
        }

        binding.btnLogin.setOnClickListener {
            Navigator.intentFor(this, LOGIN_ACTIVITY_INTENT_FILTER)
                .navigate()
        }

        if(isInLandscape()){
            val animImage1:Animation = AnimationUtils.loadAnimation(this, app.slyworks.base_feature.R.anim.onboarding_image_1_anim)
            val animImage2:Animation = AnimationUtils.loadAnimation(this, app.slyworks.base_feature.R.anim.onboarding_image_2_anim)
            val animImage3:Animation = AnimationUtils.loadAnimation(this, app.slyworks.base_feature.R.anim.onboarding_image_3_anim)
            val animImage4:Animation = AnimationUtils.loadAnimation(this, app.slyworks.base_feature.R.anim.onboarding_image_4_anim)
            val animImage5:Animation = AnimationUtils.loadAnimation(this, app.slyworks.base_feature.R.anim.onboarding_image_5_anim)
            val animLayout:Animation = AnimationUtils.loadAnimation(this, app.slyworks.base_feature.R.anim.onboarding_layout_anim)

            binding.onboardingImage1.startAnimation(animImage1)
            binding.onboardingImage2.startAnimation(animImage2)
            binding.onboardingImage3.startAnimation(animImage3)
            binding.onboardingImage4.startAnimation(animImage4)
            binding.onboardingImage5.startAnimation(animImage5)
        }else {
            val animLayout:Animation = AnimationUtils.loadAnimation(this, app.slyworks.base_feature.R.anim.onboarding_layout_anim)
            binding.layoutBtns.startAnimation(animLayout)
        }
    }


}