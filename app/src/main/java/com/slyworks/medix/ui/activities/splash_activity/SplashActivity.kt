package com.slyworks.medix.ui.activities.splash_activity

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.lifecycle.lifecycleScope
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
import com.slyworks.navigation.Navigator
import com.slyworks.userdetails.UserDetailsUtils
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

class SplashActivity : BaseActivity() {
    //region Vars
    @Inject
    lateinit var usersManager:UsersManager;
    @Inject
    lateinit var userDetailsUtils:UserDetailsUtils;
    @Inject
    lateinit var preferenceManager:PreferenceManager

    private var mHandler:Handler? = Handler(Looper.myLooper()!!)
    //endregion


    override fun onCreate(activity: Activity) {
        initDI()

        super.onCreate(activity)
    }

    private fun initDI(){
        application.appComponent
            .activityComponentBuilder()
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

    private fun _startActivity() {
        lifecycleScope.launch {
            val navigator: Navigator.ActivityContinuation
            val status = checkUserDetailsAvailability()

            navigator =
                     if(status && isLoginSessionValid())
                         Navigator.intentFor<MainActivity>(this@SplashActivity)
                     else
                         Navigator.intentFor<OnBoardingActivity>(this@SplashActivity)


            navigator.newAndClearTask()
                .finishCaller()
                .navigate()
        }
    }

    private fun isLoginSessionValid():Boolean{
        val lastSignInTime: Long = preferenceManager.get(KEY_LAST_SIGN_IN_TIME, System.currentTimeMillis())
        return TimeUtils.isWithin3DayPeriod(lastSignInTime)
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
    }

    override fun onBackPressed() {}
}