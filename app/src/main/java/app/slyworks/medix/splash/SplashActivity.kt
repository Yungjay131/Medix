package app.slyworks.medix.splash

import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import app.slyworks.utils_lib.MAIN_ACTIVITY_INTENT_FILTER
import app.slyworks.utils_lib.ONBOARDING_ACTIVITY_INTENT_FILTER
import app.slyworks.medix._di.ApplicationComponent
import dev.joshuasylvanus.navigator.Navigator


import javax.inject.Inject

class SplashActivity : AppCompatActivity() {
    //region Vars
    @Inject
    lateinit var viewModel: SplashActivityViewModel
    //endregion


   override fun onCreate(savedInstanceState: Bundle?) {
        initDI()
        super.onCreate(savedInstanceState)
       // setContentView(R.layout.activity_splash)
    }

    private fun initDI(){
       ApplicationComponent.getInitialBuilder()
           .build()
           .inject(this)
    }

    /*private fun initViews(){
        val iv:ImageView = findViewById(R.id.ivLogo_splash)

        val animationLogo = AnimationUtils.loadAnimation(this,
         app.slyworks.ui_commons_feature.R.anim.splash_logo_anim)
        iv.startAnimation(animationLogo)
    }*/

    private fun initViews2() {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS )
    }

    /*trying to fix situations where the app was minimized when still
        * in the SplashScreen, to ensure proper functioning*/
    override fun onResume() {
        super.onResume()
       navigateToCorrectActivity()
    }

    private fun navigateToCorrectActivity(){
        viewModel.uiStateLD.observe(this){ state: SplashActivityUIState ->
            when(state){
                is SplashActivityUIState.SessionInvalid -> {
                    Navigator.intentFor(this@SplashActivity, MAIN_ACTIVITY_INTENT_FILTER)
                        .newAndClearTask()
                        .navigate()
                }

                is SplashActivityUIState.SessionValid -> {
                    Navigator.intentFor(this@SplashActivity, ONBOARDING_ACTIVITY_INTENT_FILTER)
                        .newAndClearTask()
                        .navigate()
                }
            }
        }

        viewModel.checkUserLoggedInStatus()
    }

    override fun onBackPressed() {}
}