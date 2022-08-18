package com.slyworks.medix.ui.dialogs

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.slyworks.medix.R


/**
 *Created by Joshua Sylvanus, 11:13 PM, 1/4/2022.
 */
class TermsAndConditionsBSDialog() : BaseBottomSheetDialogFragment() {

    companion object{
        @JvmStatic
        fun getInstance(): TermsAndConditionsBSDialog {
           return TermsAndConditionsBSDialog()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view:View = inflater.inflate(R.layout.bottomsheet_terms_and_conditions, null)
        val ivCancel:ImageView = view.findViewById(R.id.ivCancelBSTC)
        ivCancel.setOnClickListener { dismiss() }
        return view
    }
}