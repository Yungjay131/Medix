package com.slyworks.medix.ui.dialogs

import android.content.DialogInterface
import androidx.fragment.app.DialogFragment


open class BaseDialogFragment: DialogFragment() {
    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
    }


}