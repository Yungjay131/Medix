package com.slyworks.medix.ui.custom_views

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import com.slyworks.medix.R


/**
 *Created by Joshua Sylvanus, 7:39 PM, 21/10/2022.
 */
class SelectionItemView
@JvmOverloads
constructor(
    context: Context,
    attrs:AttributeSet? = null,
    defStyleAttr:Int = 0): ConstraintLayout(context, attrs, defStyleAttr) {
    //region Vars
    //endregion

    init{
        val view = inflate(context, R.layout.selection_item, this)
    }
}