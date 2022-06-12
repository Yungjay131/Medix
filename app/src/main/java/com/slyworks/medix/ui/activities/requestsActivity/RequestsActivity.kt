package com.slyworks.medix.ui.activities.requestsActivity

import android.os.Bundle
import com.slyworks.constants.EXTRA_LOGIN_DESTINATION
import com.slyworks.medix.LoginManager
import com.slyworks.medix.R
import com.slyworks.medix.navigation.ActivityWrapper
import com.slyworks.medix.navigation.NavigationManager
import com.slyworks.medix.ui.activities.BaseActivity

class RequestsActivity : BaseActivity() {
    companion object{
        private var mIsInForeground:Boolean = false
        fun getForegroundStatus():Boolean{ return mIsInForeground }
    }

    override fun onStart() {
        super.onStart()
        super.onStart(this)
        mIsInForeground = true
    }

    override fun onStop() {
        super.onStop()
        super.onStop(this)
        mIsInForeground = false
    }

    override fun onDestroy() {
        super.onDestroy(this)
        super.onDestroy()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_request)

        initData()
        super.onCreate(this)
    }

    private fun initData(){
        /*checking if there is a signed in user*/
        if(!LoginManager.getInstance().getLoginStatus()){
            NavigationManager.inflateActivity(this,
                                             ActivityWrapper.LOGIN,
                                             false,
                                              isToBeFinished = false,
                                              extras = Bundle().apply {
                                                  putString(EXTRA_LOGIN_DESTINATION, this::class.simpleName)
                                              })
        }
    }

    override fun onBackPressed() {
        NavigationManager.onBackPressed(this, true)
    }
}