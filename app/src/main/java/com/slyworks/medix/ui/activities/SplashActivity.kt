package com.slyworks.medix.ui.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.lifecycle.lifecycleScope
import com.slyworks.constants.KEY_LAST_SIGN_IN_TIME
import com.slyworks.medix.R
import com.slyworks.medix.utils.UserDetailsUtils
import com.slyworks.medix.managers.PreferenceManager
import com.slyworks.medix.managers.TimeUtils
import com.slyworks.medix.managers.UsersManager
import com.slyworks.medix.ui.activities.mainActivity.MainActivity
import com.slyworks.medix.ui.activities.onBoardingActivity.OnBoardingActivity
import com.slyworks.medix.utils.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest

class SplashActivity : BaseActivity() {
    //region Vars
    private var mHandler:Handler? = Handler(Looper.myLooper()!!)
    //endregion


    override fun onDestroy() {
        super.onDestroy()
        mHandler!!.removeCallbacksAndMessages(null)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
      /*  setTheme(R.style.AppTheme_splash3)
        setContentView(R.layout.activity_splash)*/
        super.onCreate(savedInstanceState)
    }

    private fun initViews(){
        val iv:ImageView = findViewById(R.id.ivLogo_splash)

        val animationLogo = AnimationUtils.loadAnimation(this, R.anim.splash_logo_anim)
        iv.startAnimation(animationLogo)
    }

    /*trying to fix situations where the app was minimized when still
        * in the SplashScreen, to ensure proper functioning*/
    override fun onResume() {
        super.onResume()

        //initViews()
        mHandler!!.post { _startActivity() }
    }

    private fun _startActivity() {
        lifecycleScope.launch {
            val destination:Class<*>
            val status = checkUserDetailsAvailability()

            destination =
                     if(status){
                        if(isLoginSessionValid())
                            MainActivity::class.java
                        else
                            OnBoardingActivity::class.java
                     }else{
                         OnBoardingActivity::class.java
                     }

            val intent = Intent(this@SplashActivity, destination)
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }
    }

    private fun isLoginSessionValid():Boolean{
        val lastSignInTime: Long = PreferenceManager.get(KEY_LAST_SIGN_IN_TIME, System.currentTimeMillis())
        return TimeUtils.isWithin3DayPeriod(lastSignInTime)
    }

    private suspend fun checkUserDetailsAvailability(): Boolean{
        return lifecycleScope.async(Dispatchers.IO) {
            var result: Boolean = false
            val childJob = lifecycleScope.launch(Dispatchers.IO) {
                UsersManager.getUserFromDataStore()
                    .collectLatest {
                        if (it.firebaseUID.isEmpty()) {
                            result = false
                            this.coroutineContext.cancel()
                            return@collectLatest
                        }

                        UserDetailsUtils.user = it
                        result = true
                        this.coroutineContext.cancel()
                    }
            }

                childJob.join()
                return@async result
            }.await()
    }

    private fun initViews2() {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS )
    }

    override fun onBackPressed() {}
}