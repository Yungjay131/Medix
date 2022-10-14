package com.slyworks.medix.di.components

import com.slyworks.di.DialogFragmentScope
import com.slyworks.medix.ui.dialogs.ExitDialog
import com.slyworks.medix.ui.dialogs.LogoutDialog
import dagger.Subcomponent


/**
 *Created by Joshua Sylvanus, 9:57 PM, 16/08/2022.
 */
@DialogFragmentScope
@Subcomponent
interface DialogFragmentComponent {
    fun inject(dialogFragment:LogoutDialog)

    @Subcomponent.Builder
    interface Builder{
        fun build():DialogFragmentComponent
    }
}