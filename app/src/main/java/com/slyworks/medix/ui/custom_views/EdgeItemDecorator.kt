package com.slyworks.medix.ui.custom_views

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.slyworks.medix.R
import com.slyworks.medix.ui.activities.messageActivity.RVMessageAdapter
import com.slyworks.medix.utils.px


/**
 *Created by Joshua Sylvanus, 7:27 PM, 05/05/2022.
 */
typealias MHolder = RVMessageAdapter.MViewHolder
class EdgeItemDecorator : RecyclerView.ItemDecoration() {
    //region Vars
    val mPath = Path()
    val mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    var mIsLastInGroup:Boolean = false
    //endregion


    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
         mPath.reset()

        for(i in 0 until parent.childCount){
            val view:View = parent.getChildAt(i)
            val currentViewHolder:RVMessageAdapter.MViewHolder? =
                parent.getChildViewHolder(view) as RVMessageAdapter.MViewHolder
            var previousViewHolder:RVMessageAdapter.MViewHolder? = null
            var nextViewHolder:RVMessageAdapter.MViewHolder? =  null

            currentViewHolder?.let {
                when(it){
                    is RVMessageAdapter.ToViewHolder -> mPaint.color = ContextCompat.getColor(parent.context, R.color.appBlue_li_message_to)
                    is RVMessageAdapter.FromViewHolder -> mPaint.color = ContextCompat.getColor(parent.context, R.color.appGrey_li_message_from)
                }
                previousViewHolder =
                parent.findViewHolderForAdapterPosition(currentViewHolder.bindingAdapterPosition?.minus(1)) as RVMessageAdapter.MViewHolder

                nextViewHolder =
                parent.findViewHolderForAdapterPosition(currentViewHolder.bindingAdapterPosition?.plus(1)) as RVMessageAdapter.MViewHolder
            }

            if(currentViewHolder is RVMessageAdapter.HeaderViewHolder){
                super.onDraw(c, parent, state)
                continue
            }
            else if(nextViewHolder == null || previousViewHolder == null){
                mIsLastInGroup = true
            } else if(currentViewHolder is RVMessageAdapter.ToViewHolder &&
                (nextViewHolder is RVMessageAdapter.FromViewHolder ||
                        nextViewHolder is RVMessageAdapter.HeaderViewHolder)){
                /*its different*/
                mIsLastInGroup = true
            }else if(currentViewHolder is RVMessageAdapter.FromViewHolder &&
                (nextViewHolder is RVMessageAdapter.ToViewHolder ||
                        nextViewHolder is RVMessageAdapter.HeaderViewHolder)){
                mIsLastInGroup = true
            }

            if(mIsLastInGroup){
                /*lol..just trying to get comfortable with destructuring in Kotlin*/
                var (x,y) = 0f to 0f
                if(currentViewHolder is RVMessageAdapter.ToViewHolder){
                    /*on the right*/
                    x = view.right.toFloat()
                    y = view.bottom.toFloat()
                    mPath.moveTo(x,y)
                    mPath.lineTo(x + 8.px, y)

                }else if(currentViewHolder is RVMessageAdapter.FromViewHolder){
                    x = view.left.toFloat()
                    y = view.bottom.toFloat()
                    mPath.moveTo(x,y)
                    mPath.lineTo(x - 8.px, y)
                }

                mPath.lineTo(x, y - 8.px)
                mPath.close()
                c.drawPath(mPath,mPaint)
            }
        }
    }


}