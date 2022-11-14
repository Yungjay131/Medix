package com.slyworks.medix.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.slyworks.medix.R


/**
 *Created by Joshua Sylvanus, 9:29 PM, 12/11/2022.
 */
class PatientRegistrationIntroDialog : BaseDialogFragment() {
    //region Vars
    //endregion

    override fun isCancelable(): Boolean  = false

    override fun onDestroy() {
        super.onDestroy()
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
        return inflater.inflate(R.layout.dialog_patient_reg_intro, container, false)
    }
}