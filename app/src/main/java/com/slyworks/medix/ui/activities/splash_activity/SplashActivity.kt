package com.slyworks.medix.ui.activities.splash_activity

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.lifecycle.lifecycleScope
import app.slyworks.navigator.Navigator
import com.slyworks.auth.UsersManager
import com.slyworks.constants.KEY_LAST_SIGN_IN_TIME
import com.slyworks.medix.R
import com.slyworks.medix.appComponent
import com.slyworks.medix.ui.activities.BaseActivity
import com.slyworks.utils.TimeUtils
import com.slyworks.utils.PreferenceManager
import com.slyworks.medix.ui.activities.main_activity.MainActivity
import com.slyworks.medix.ui.activities.onboarding_activity.OnBoardingActivity
import com.slyworks.medix.utils.*
import com.slyworks.userdetails.UserDetailsUtils
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SplashActivity : BaseActivity() {
    //region Vars
    private var mHandler:Handler? = Handler(Looper.myLooper()!!)

    @Inject
    lateinit var viewModel: SplashActivityViewModel
    //endregion


    override fun onCreate(savedInstanceState: Bundle?) {
        initDI()
        super.onCreate(savedInstanceState)
    }

    private fun initDI(){
        application.appComponent
            .activityComponentBuilder()
            .setActivity(this)
            .build()
            .inject(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        //mHandler!!.removeCallbacksAndMessages(null)
    }

    private fun initViews(){
        val iv:ImageView = findViewById(R.id.ivLogo_splash)

        val animationLogo = AnimationUtils.loadAnimation(this, R.anim.splash_logo_anim)
        iv.startAnimation(animationLogo)
    }

    private fun initViews2() {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS )
    }

    /*trying to fix situations where the app was minimized when still
        * in the SplashScreen, to ensure proper functioning*/
    override fun onResume() {
        super.onResume()
        //mHandler!!.post { _startActivity() }
       _startActivity()
    }

    private fun _startActivity(){
        viewModel.isSessionValid
            .observe(this){
            (if(it)
                    Navigator.intentFor<MainActivity>(this@SplashActivity)
                else
                    Navigator.intentFor<OnBoardingActivity>(this@SplashActivity)
            )
                    .finishCaller()
                    .navigate()

        }
    }

   /* private fun _startActivity() {
        lifecycleScope.launch {
            val status = checkUserDetailsAvailability()

            (if(status && isLoginSessionValid())
                Navigator.intentFor<MainActivity>(this@SplashActivity)
            else
                Navigator.intentFor<OnBoardingActivity>(this@SplashActivity))
                .finishCaller()
                .navigate()
        }
    }*/

   /* private fun isLoginSessionValid():Boolean{
        val lastSignInTime: Long = preferenceManager.get(KEY_LAST_SIGN_IN_TIME, System.currentTimeMillis())
        return timeUtils.isWithinTimePeriod(lastSignInTime, 3, TimeUnit.DAYS)
    }

    private suspend fun checkUserDetailsAvailability(): Boolean{
        return lifecycleScope.async(Dispatchers.IO) {
            var result: Boolean = false
            val childJob = lifecycleScope.launch(Dispatchers.IO) {
                usersManager.getUserFromDataStore()
                    .collectLatest {
                        if (it.firebaseUID.isEmpty()) {
                            result = false
                            this.coroutineContext.cancel()
                            return@collectLatest
                        }

                        userDetailsUtils.user = it
                        result = true
                        this.coroutineContext.cancel()
                    }
            }

                childJob.join()
                return@async result
            }.await()
    }*/

    override fun onBackPressed() {}
}