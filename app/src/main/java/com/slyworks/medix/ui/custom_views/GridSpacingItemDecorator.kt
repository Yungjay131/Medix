package com.slyworks.medix.ui.custom_views

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.slyworks.medix.App
import com.slyworks.medix.R


/**
 *Created by Joshua Sylvanus, 12:34 AM, 1/18/2022.
 */
class GridSpacingItemDecorator() : RecyclerView.ItemDecoration() {
    //region Vars
    //endregion
    companion object{
        val space:Int = App.getContext().resources.getDimensionPixelSize(R.dimen.grid_layout_spacing)
    }
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
       outRect.left = space
       outRect.right = space
       outRect.bottom = space

        //add top margin only for the first item
        /*if(parent.getChildLayoutPosition(view) == 0)
            outRect.top = space
        else
            outRect.top = 0*/
    }
}