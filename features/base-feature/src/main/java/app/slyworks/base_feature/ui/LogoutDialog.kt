package app.slyworks.base_feature.ui

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import app.slyworks.base_feature.BaseDialogFragment
import app.slyworks.base_feature.R
import app.slyworks.utils_lib.ONBOARDING_ACTIVITY_INTENT_FILTER
import dev.joshuasylvanus.navigator.Navigator


/**
 *Created by Joshua Sylvanus, 6:02 PM, 03/05/2022.
 */
class LogoutDialog(private val logoutFunc:() -> Unit): BaseDialogFragment() {
    //region Vars
    private lateinit var tvCancel: TextView
    private lateinit var tvLogout: TextView

    //endregion


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
            logoutFunc()

            Navigator.intentFor(requireContext(), ONBOARDING_ACTIVITY_INTENT_FILTER)
                .newAndClearTask()
                .navigate()
        }
    }

}