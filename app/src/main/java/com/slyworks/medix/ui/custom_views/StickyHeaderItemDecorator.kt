package com.slyworks.medix.ui.custom_views

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.core.view.drawToBitmap
import androidx.recyclerview.widget.RecyclerView
import com.slyworks.medix.App
import com.slyworks.medix.R
import com.slyworks.medix.ui.activities.messageActivity.RVMessageAdapter


/**
 *Created by Joshua Sylvanus, 8:44 PM, 05/05/2022.
 */
class StickyHeaderItemDecorator:RecyclerView.ItemDecoration() {
    //region Vars
    private val mPaint:Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(App.getContext(), R.color.Grey)
    }

    private var mCurrentHeaderBitmap:Bitmap? = null
    //endregion

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val stickyViewHolders: Sequence<RVMessageAdapter.HeaderViewHolder> =
            parent.children
                .map { parent.findContainingViewHolder(it) }
                .filter { it is RVMessageAdapter.HeaderViewHolder }
                .map { it as RVMessageAdapter.HeaderViewHolder }

        if(stickyViewHolders.toList().isEmpty())
            return

        stickyViewHolders.forEach { it.itemView.alpha = 1f }

        /*take first StickyViewHolder and its params*/
        val viewHolder:RVMessageAdapter.HeaderViewHolder = stickyViewHolders.first()

        val viewHolderBitmap: Bitmap = viewHolder.itemView.drawToBitmap()
        val viewHolderY:Float = viewHolder.itemView.y ?:0f

        /*init headerBitmap if needed*/
        if(mCurrentHeaderBitmap == null || viewHolderY <= 0f)
            mCurrentHeaderBitmap = viewHolderBitmap

        /*calculate bitmap top offset*/
        val bitmapHeight:Int = mCurrentHeaderBitmap?.height ?: 0
        val bitmapTopOffset =
            if(0 <= viewHolderY && viewHolderY <= bitmapHeight)
               viewHolderY - bitmapHeight
            else
                0f

        /*hide view*/
        viewHolder.itemView.alpha =
            if(viewHolderY < 0f)
                0f
            else
                1f

        /*draw bitmap header*/
        mCurrentHeaderBitmap?.let{
            c.drawBitmap(it, 0f, bitmapTopOffset, mPaint)
        }
    }


}