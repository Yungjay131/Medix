package com.slyworks.medix.ui.activities.settingsActivity

import android.os.Bundle
import com.slyworks.medix.R
import com.slyworks.medix.navigation.NavigationManager
import com.slyworks.medix.ui.activities.BaseActivity

class SettingsActivity : BaseActivity() {
    companion object{
        private var mIsInForeground:Boolean = false
        fun getForegroundStatus():Boolean = mIsInForeground
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
        setContentView(R.layout.activity_settings)
        super.onCreate(this)
    }

    override fun onBackPressed() {
        NavigationManager.onBackPressed(this, true)
    }
}