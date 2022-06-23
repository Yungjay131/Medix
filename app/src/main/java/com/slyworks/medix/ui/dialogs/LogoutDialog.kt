package com.slyworks.medix.ui.dialogs

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.slyworks.medix.R
import com.slyworks.medix.LoginManager
import com.slyworks.medix.navigation.ActivityWrapper
import com.slyworks.medix.navigation.NavigationManager
import com.slyworks.medix.navigation.Navigator
import com.slyworks.medix.ui.activities.loginActivity.LoginActivity
import com.slyworks.medix.ui.activities.onBoardingActivity.OnBoardingActivity


/**
 *Created by Joshua Sylvanus, 6:02 PM, 03/05/2022.
 */
class LogoutDialog: BaseDialogFragment() {
    //region Vars
    private lateinit var tvCancel: TextView
    private lateinit var tvLogout: TextView
    //endregion


    companion object{
        @JvmStatic
        fun getInstance():LogoutDialog = LogoutDialog()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext(), theme).apply {
            val dialogView = onCreateView(LayoutInflater.from(requireContext()),null, savedInstanceState)
            dialogView?.let {
                onViewCreated(it,savedInstanceState)
            }
            setView(dialogView)
        }.create()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_logout2, container, false)
        initViews(view)
        return view
    }

    private fun initViews(view: View){
        tvCancel = view.findViewById(R.id.tvCancel_dialog_logout)
        tvLogout = view.findViewById(R.id.tvLogout_dialog_logout)

        tvCancel.setOnClickListener { dismiss() }
        tvLogout.setOnClickListener {
            LoginManager.getInstance()
                .logoutUser()

            Navigator.intentFor<OnBoardingActivity>(requireActivity())
                .newAndClearTask()
                .finishCaller()
                .navigate()
        }
    }

}