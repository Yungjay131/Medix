package app.slyworks.auth_feature.onboarding

import android.content.res.Configuration
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.Button
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.NestedScrollView
import app.slyworks.auth_feature.R
import app.slyworks.auth_feature._di.AuthFeatureComponent
import app.slyworks.auth_feature.databinding.ActivityOnBoardingBinding
import app.slyworks.base_feature.BaseActivity
import app.slyworks.base_feature.MOnBackPressedCallback
import app.slyworks.constants_lib.EXTRA_IS_ACTIVITY_RECREATED
import app.slyworks.constants_lib.GENERAL
import app.slyworks.base_feature.custom_views.NetworkStatusView
import app.slyworks.base_feature.custom_views.setStatus
import app.slyworks.constants_lib.LOGIN_ACTIVITY_INTENT_FILTER
import app.slyworks.constants_lib.REGISTRATION_ACTIVITY_INTENT_FILTER
import com.google.android.material.imageview.ShapeableImageView
import dev.joshuasylvanus.navigator.Navigator
import javax.inject.Inject

class OnBoardingActivity : BaseActivity() {
    private var networkStatusView: NetworkStatusView? = null

    private lateinit var binding:ActivityOnBoardingBinding

    @Inject
    lateinit var viewModel: OnBoardingActivityViewModel

    override fun isValid(): Boolean = false

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(EXTRA_IS_ACTIVITY_RECREATED, true)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        initDI()

        super.onCreate(savedInstanceState)

        binding = ActivityOnBoardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initData()

        if(!isInLandscape()){
            initViews_normal()
            initAnimations_normal()
        }else{
            initViews_landscape()
            initAnimations_landscape()
        }
    }

    private fun isInLandscape(): Boolean =
        resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    private fun initDI(){
        AuthFeatureComponent.getInitialBuilder()
            .build()
            .inject(this)
    }

    private fun initData() {
        this.onBackPressedDispatcher
            .addCallback(this, MOnBackPressedCallback(this))

        viewModel.subscribeToNetwork().observe(this) {
            if(networkStatusView == null)
                networkStatusView = NetworkStatusView.from(binding.root, GENERAL)

            networkStatusView!!.setStatus(it)
        }
    }

    private fun initViews_landscape() {
        binding.btnGetStarted.setOnClickListener {
            Navigator.intentFor(this, REGISTRATION_ACTIVITY_INTENT_FILTER)
                .navigate()
        }

        binding.btnLogin.setOnClickListener {
            Navigator.intentFor(this, LOGIN_ACTIVITY_INTENT_FILTER)
                .navigate()
        }
    }

    private fun initViews_normal() {
        binding.btnGetStarted.setOnClickListener {
            Navigator.intentFor(this, REGISTRATION_ACTIVITY_INTENT_FILTER)
                .navigate()
        }

        binding.btnLogin.setOnClickListener {
            Navigator.intentFor(this, LOGIN_ACTIVITY_INTENT_FILTER)
                .navigate()
        }
    }

    private fun initAnimations_landscape() {
        val animLayout = AnimationUtils.loadAnimation(this, app.slyworks.base_feature.R.anim.onboarding_layout_anim)
        binding.layoutBtns.startAnimation(animLayout)
    }

    private fun initAnimations_normal() {
        val animImage1 = AnimationUtils.loadAnimation(this, app.slyworks.base_feature.R.anim.onboarding_image_1_anim)
        val animImage2 = AnimationUtils.loadAnimation(this, app.slyworks.base_feature.R.anim.onboarding_image_2_anim)
        val animImage3 = AnimationUtils.loadAnimation(this, app.slyworks.base_feature.R.anim.onboarding_image_3_anim)
        val animImage4 = AnimationUtils.loadAnimation(this, app.slyworks.base_feature.R.anim.onboarding_image_4_anim)
        val animImage5 = AnimationUtils.loadAnimation(this, app.slyworks.base_feature.R.anim.onboarding_image_5_anim)
        val animLayout = AnimationUtils.loadAnimation(this, app.slyworks.base_feature.R.anim.onboarding_layout_anim)

        binding.onboardingImage1.startAnimation(animImage1)
        binding.onboardingImage2.startAnimation(animImage2)
        binding.onboardingImage3.startAnimation(animImage3)
        binding.onboardingImage4.startAnimation(animImage4)
        binding.onboardingImage5.startAnimation(animImage5)
    }


}