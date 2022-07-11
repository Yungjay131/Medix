package com.slyworks.medix.ui.fragments.chatFragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import com.slyworks.constants.*
import com.slyworks.medix.App
import com.slyworks.medix.utils.AppController
import com.slyworks.medix.R
import com.slyworks.medix.managers.TimeUtils
import com.slyworks.medix.utils.ViewUtils.displayImage
import com.slyworks.models.room_models.MessagePersonWithMessages
import de.hdodenhof.circleimageview.CircleImageView


/**
 *Created by Joshua Sylvanus, 10:31 AM, 1/13/2022.
 */
class RVChatAdapter() : RecyclerView.Adapter<RVChatAdapter.ViewHolder>() {
    //region Vars
    private var mList:MutableList<MessagePersonWithMessages> = mutableListOf()
    //endregion

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view:View = LayoutInflater.from(parent.context).inflate(R.layout.li_chat,parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder:RVChatAdapter.ViewHolder, position: Int) {
       holder.bind(mList[position])
    }

    override fun getItemCount(): Int {
       return mList.size
    }

    fun addData(list:MutableList<MessagePersonWithMessages>){
        val index = if(mList.size == 0) 0 else mList.size - 1
        mList.addAll(list)
        notifyItemRangeInserted(index, list.size)
    }

    fun setData(list:MutableList<MessagePersonWithMessages>){
        mList = list
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        //region Vars
        private var rootView:ConstraintLayout = itemView.findViewById(R.id.rootView)

        private var ivProfile:CircleImageView = itemView.findViewById(R.id.ivProfile_li_chat)
        private var tvPersonName:MaterialTextView = itemView.findViewById(R.id.tvMessagePersonName_li_chat)
        private var tvLastMessage:MaterialTextView = itemView.findViewById(R.id.tvLastMessage_li_chat)
        private var tvLastMessageTimestamp:MaterialTextView = itemView.findViewById(R.id.tvLastMessageTimeStamp_li_chat)
        private var ivLastMessageStatus: ImageView = itemView.findViewById(R.id.ivLastMessageStatus_li_chat)

        private var layout_unread_messages:ConstraintLayout = itemView.findViewById(R.id.layout_unread_messages_li_chat)
        private var tvUnreadMessageCount:MaterialTextView = itemView.findViewById(R.id.tvUnreadMessageCount_li_chat)

        private val mStatusMap:MutableMap<Double, Int> = mutableMapOf(
            NOT_SENT to R.drawable.ic_access_time,
            SENT to R.drawable.ic_check,
            DELIVERED to R.drawable.ic_check_double,
            READ to R.drawable.ic_done_all
        )
        //endregion
        private fun getUnreadMessageCount(messages:MutableList<com.slyworks.models.room_models.Message>):Int{
            var unreadCount:Int = 0
            messages.forEach {
                if(it.status != READ) unreadCount.plus(1)
            }

            return unreadCount
        }
        fun bind(entity: MessagePersonWithMessages){
            val prefix = if(entity.details.userAccountType == "PATIENT") "" else "Dr."
            ivProfile.displayImage(entity.details.senderImageUri)
            tvPersonName.text = "$prefix ${entity.details.fullName}"

            if(!entity.details.lastMessageTimeStamp.isNullOrEmpty())
               tvLastMessageTimestamp.text = TimeUtils.convertTimeToString(entity.details.lastMessageTimeStamp!!)

            val condition_1:Boolean = entity.details.lastMessageType != null
            val condition_2:Boolean = entity.details.lastMessageType == INCOMING_MESSAGE
            val condition_3:Boolean = entity.details.lastMessageType!!.isNotEmpty()
            if(condition_1 && condition_2  && condition_3 )
               ivLastMessageStatus.displayImage(mStatusMap[entity.details.lastMessageStatus]!!)
            else
                ivLastMessageStatus.visibility = View.GONE

            if(entity.details.lastMessageContent != null)
               tvLastMessage.text = entity.details.lastMessageContent
            else
                tvLastMessage.visibility = View.GONE

            val unreadMessageCount:Int = getUnreadMessageCount(entity.messages)
            if(unreadMessageCount > 0) {
                layout_unread_messages.visibility = View.VISIBLE
                tvUnreadMessageCount.text = unreadMessageCount.toString()
                tvLastMessageTimestamp.setTextColor(ContextCompat.getColor(App.getContext(),R.color.appGreen_text))
            }else{
                layout_unread_messages.visibility = View.GONE
            }

            rootView.setOnClickListener {
                AppController.notifyObservers(EVENT_OPEN_MESSAGE_ACTIVITY, entity)
            }
        }
    }


}