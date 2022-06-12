package com.slyworks.medix.ui.fragments.homeFragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import com.slyworks.medix.R
import com.slyworks.models.room_models.MessagePersonWithMessages
import de.hdodenhof.circleimageview.CircleImageView


/**
 *Created by Joshua Sylvanus, 1:27 PM, 1/12/2022.
 */
class RVRecentConversationsAdapter : RecyclerView.Adapter<RVRecentConversationsAdapter.ViewHolder>() {
    //region Vars
    private var mList:MutableList<MessagePersonWithMessages> = mutableListOf()
    //endregion

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view:View = LayoutInflater.from(parent.context).inflate(R.layout.li_chat, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val entity: MessagePersonWithMessages = mList.get(position) ?: return
        holder.bind(entity)
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        //region Vars
        private val ivProfile:CircleImageView = itemView.findViewById(R.id.ivProfile_li_chat)
        private val tvMessagePersonName:MaterialTextView = itemView.findViewById(R.id.tvMessagePersonName_li_chat)
        private val ivLastMessageStatus: ImageView = itemView.findViewById(R.id.ivLastMessageStatus_li_chat)
        private val tvLastMessage:MaterialTextView = itemView.findViewById(R.id.tvLastMessage_li_chat)

        private val layout_message_count:ConstraintLayout = itemView.findViewById(R.id.layout_unread_messages_li_chat)
        private val tvMessageCount:MaterialTextView = itemView.findViewById(R.id.tvUnreadMessageCount_li_chat)
        //endregion

        fun bind(entity: MessagePersonWithMessages){}
    }
}