package com.slyworks.medix.ui.activities.messageActivity

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.slyworks.constants.*
import com.slyworks.medix.App
import com.slyworks.medix.R
import com.slyworks.medix.managers.TimeUtils
import com.slyworks.models.room_models.Message
import com.slyworks.medix.utils.ViewUtils.displayImage


/**
 *Created by Joshua Sylvanus, 1:36 AM, 1/19/2022.
 */
class RVMessageAdapter(var recyclerView:RecyclerView) : RecyclerView.Adapter<RVMessageAdapter.MViewHolder>() {
    //region Vars
    private var mList:MutableList<Message> = mutableListOf()
    //endregion

    companion object{
        //region Vars
        private val DEFAULT_ITEMVIEW_HEIGHT:Int =  App.getContext().resources.getDimensionPixelSize(R.dimen.itemView_height)
        private val ITEM_TYPE_INCOMING = 1_000
        private val ITEM_TYPE_OUTGOING = 2_000
        private val ITEM_TYPE_HEADER = 3_000
        //endregion
        private fun getMessageStatusDrawable(status:Double):Int{
            /*TODO:would be a good place for IntDef*/
            return when(status){
                NOT_SENT -> R.drawable.ic_access_time
                SENT -> R.drawable.ic_check
                DELIVERED -> R.drawable.ic_done_all2
                READ -> R.drawable.ic_read
                else -> -1
            }
        }
    }

    private fun smoothScroll(toPosition:Int, duration:Int = 500){
        //duration in milliseconds
        val TARGET_SEEK_SCROLL_DISTANCE_PX:Int = 10_000
        var itemHeight:Int = if(recyclerView.getChildAt(0) != null) recyclerView.getChildAt(0).height // Height of first visible view! NB: ViewGroup method!
                             else DEFAULT_ITEMVIEW_HEIGHT
        itemHeight += 4 // Example pixel Adjustment for decoration?

        val firstVisibleItemPosition:Int = (recyclerView.getLayoutManager() as LinearLayoutManager).findFirstCompletelyVisibleItemPosition()

        val difference:Int = firstVisibleItemPosition - toPosition
        var index:Int = Math.abs(difference * itemHeight)
        if(index == 0){
            val size: Float = recyclerView.getChildAt(0).getY()
            index = Math.abs(size).toInt()
        }

        val totalPixels:Int = index // Best guess: Total number of pixels to scroll

        val smoothScroller: LinearSmoothScroller = object : LinearSmoothScroller(recyclerView.context){
            override fun getVerticalSnapPreference(): Int {
                return LinearSmoothScroller.SNAP_TO_START
            }
            override fun calculateTimeForScrolling(dx: Int): Int {
                val d:Int = duration * dx
                val _ms:Float = d / totalPixels.toFloat()
                var  ms:Int = _ms.toInt()

                //double the interval for fast scroll
                if(dx < TARGET_SEEK_SCROLL_DISTANCE_PX){
                    ms *= 2// Crude deceleration!
                }

                return ms
            }

        }

        smoothScroller.targetPosition = toPosition
        recyclerView.layoutManager!!.startSmoothScroll(smoothScroller)
    }
    override fun getItemViewType(position: Int): Int {
        when(mList.get(position).type){
            INCOMING_MESSAGE -> return ITEM_TYPE_INCOMING
            OUTGOING_MESSAGE -> return ITEM_TYPE_OUTGOING
            HEADER -> return ITEM_TYPE_HEADER
            else -> throw IllegalArgumentException("wrong message type")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MViewHolder {
        val view:View
        when(viewType){
            ITEM_TYPE_INCOMING -> {
                view = LayoutInflater.from(parent.context).inflate(R.layout.li_message_from, parent,false)
                return FromViewHolder(view)
            }
            ITEM_TYPE_OUTGOING -> {
                view = LayoutInflater.from(parent.context).inflate(R.layout.li_message_to, parent,false)
                return ToViewHolder(view)
            }
            ITEM_TYPE_HEADER ->{
                view = LayoutInflater.from(parent.context).inflate(R.layout.li_message_header, parent,false)
                return HeaderViewHolder(view)
            }
            else -> {
               throw IllegalArgumentException("wrong message type")
            }

        }
    }

    override fun onBindViewHolder(holder: MViewHolder, position: Int) {
        val viewType = getItemViewType(position)

        when(viewType){
            ITEM_TYPE_INCOMING -> holder.bind(mList.get(position))
            ITEM_TYPE_OUTGOING -> holder.bind(mList.get(position))
            ITEM_TYPE_HEADER -> holder.bind(mList.get(position))
        }
    }

    override fun getItemCount(): Int {
        return mList.size
    }


    fun setMessageList(messages:MutableList<Message>){
        mList = messages
        mList.sort()
        /*TODO:sort list*/
        val l:MutableList<Message> = mutableListOf()
        for(i in 0 until mList.size){
            if(i+1 != mList.size - 1) {
                val a = mList[i].timeStamp
                val b = mList[i+1].timeStamp

                l.add(mList[i])
                if(!TimeUtils.checkIfDateIsSameDay(a,b)){
                    val m: Message = Message(
                        timeStamp = b,
                        type = HEADER
                    )
                    l.add(m)
                }
            }

        }

        mList = l
        notifyDataSetChanged()
        smoothScroll(mList.size - 1)
    }

    fun scrollToTop(){
      smoothScroll(0)
    }
    fun scrollToBottom(){
        smoothScroll(itemCount - 1)
    }



    abstract class MViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        abstract fun bind(entity: Message)
    }
    inner class HeaderViewHolder(itemView: View):MViewHolder(itemView){
        //region Vars
        private val tvDate:TextView = itemView.findViewById(R.id.tvDate_message_header)
        //endregion

        override fun bind(entity: Message) {
            val date:String = TimeUtils.convertTimeToString(entity.timeStamp)
            tvDate.setText(date)
        }
    }

    inner class ToViewHolder(itemView:View):MViewHolder(itemView){
        //region Vars
        private val tvContent: TextView = itemView.findViewById(R.id.tvContent_message_to)
        private val tvTimeStamp: TextView = itemView.findViewById(R.id.tvTimeStamp_message_to)
        private val ivMessageStatus: ImageView = itemView.findViewById(R.id.ivMessageStatus_message_to)
        //endregion

        override fun bind(entity: Message) {
            tvContent.text = entity.content
            tvTimeStamp.text = TimeUtils.convertTimeToString(entity.timeStamp)

            val drawable:Int = getMessageStatusDrawable(entity.status)
            ivMessageStatus.displayImage(drawable)
        }
    }

    inner class FromViewHolder(itemView: View) : MViewHolder(itemView){
        //region Vars
        private val tvContent:TextView  = itemView.findViewById(R.id.tvContent_message_from)
        private val tvTimeStamp:TextView = itemView.findViewById(R.id.tvTimeStamp_message_from)
        //endregion

        override fun bind(entity: Message){
            tvContent.text = entity.content
            tvTimeStamp.text = TimeUtils.convertTimeToString(entity.timeStamp)
        }
    }
}