package com.slyworks.medix.ui.dialogs

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.slyworks.medix.R


/**
 *Created by Joshua Sylvanus, 7:11 PM, 1/4/2022.
 */

class ForgotPasswordBSDialog(private var initFunction:((view:View?)->Unit)? = null)
    : BaseBottomSheetDialogFragment() {

    companion object {
        //region Vars
        private var INSTANCE: ForgotPasswordBSDialog? = null

        //endregion
        @JvmStatic
        fun getInstance(
            initFunction: ((view: View?) -> Unit)? = null
        ): ForgotPasswordBSDialog {
            return ForgotPasswordBSDialog(initFunction)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.bottomsheet_forgot_password, null)
        initFunction?.invoke(view)
        return view
    }

}