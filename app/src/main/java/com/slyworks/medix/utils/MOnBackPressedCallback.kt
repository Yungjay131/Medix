package com.slyworks.medix.utils

import android.app.Activity
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.slyworks.medix.ui.dialogs.ExitDialog

class MOnBackPressedCallback(private var activity: Activity)
    : OnBackPressedCallback(true) {
    override fun handleOnBackPressed() {
        if(!ActivityUtils.isLastActivity()){
            isEnabled = false
            activity.onBackPressed()
            return
        }

        ExitDialog.getInstance()
            .show((activity as AppCompatActivity).supportFragmentManager, "exit dialog")
    }
}