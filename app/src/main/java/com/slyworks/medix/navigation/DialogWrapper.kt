package com.slyworks.medix.navigation

import android.view.View
import androidx.fragment.app.DialogFragment
import com.slyworks.medix.ui.dialogs.*
import io.reactivex.rxjava3.subjects.PublishSubject

/**
 *Created by Joshua Sylvanus, 5:34 PM, 1/27/2022.
 */

sealed class DialogWrapper{
    abstract fun getInstance(args: Any? = null): DialogFragment

    object EXIT : DialogWrapper() {
        override fun getInstance(args: Any?): DialogFragment {
            return ExitDialog.getInstance()
        }

    }

    object FORGOT_PASSWORD : DialogWrapper() {
        override fun getInstance(args: Any?): DialogFragment {
            return ForgotPasswordBSDialog.getInstance(args as (view: View?) ->Unit)
        }

    }

    object TERMS_AND_CONDITIONS : DialogWrapper() {
        override fun getInstance(args: Any?): DialogFragment {
            return TermsAndConditionsBSDialog.getInstance()
        }

    }

    object CHANGE_PHOTO : DialogWrapper() {
        override fun getInstance(args: Any?): DialogFragment {
            return ChangePhotoDialog.getInstance()
        }

    }

    object LOG_OUT : DialogWrapper() {
        override fun getInstance(args: Any?): DialogFragment {
            return LogoutDialog.getInstance()
        }
    }

    object SWITCH_TO_VIDEO_CALL : DialogWrapper() {
        override fun getInstance(args: Any?): DialogFragment {
            return SwitchToVideoCallDialog(args as PublishSubject<Boolean>)
        }
    }


}