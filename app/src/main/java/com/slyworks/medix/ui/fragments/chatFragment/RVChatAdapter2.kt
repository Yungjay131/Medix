package com.slyworks.medix.ui.fragments.chatFragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import com.slyworks.constants.*
import com.slyworks.controller.AppController
import com.slyworks.medix.R
import com.slyworks.utils.TimeUtils
import com.slyworks.medix.utils.ViewUtils.displayImage
import com.slyworks.models.room_models.Person
import de.hdodenhof.circleimageview.CircleImageView


class RVChatAdapter2(diffUtil: DiffUtil.ItemCallback<Person> = PersonDiffUtilCallback())
    : ListAdapter<Person, RVChatAdapter2.ViewHolder>(diffUtil) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.li_chat, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
       val entity: Person = getItem(position)
        holder.bind(entity)
    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        //region Vars
        private var rootView: ConstraintLayout = itemView.findViewById(R.id.rootView)

        private var ivProfile: CircleImageView = itemView.findViewById(R.id.ivProfile_li_chat)
        private var tvPersonName: MaterialTextView = itemView.findViewById(R.id.tvMessagePersonName_li_chat)
        private var tvLastMessage: MaterialTextView = itemView.findViewById(R.id.tvLastMessage_li_chat)
        private var tvLastMessageTimestamp: MaterialTextView = itemView.findViewById(R.id.tvLastMessageTimeStamp_li_chat)
        private var ivLastMessageStatus: ImageView = itemView.findViewById(R.id.ivLastMessageStatus_li_chat)

        private var layout_unread_messages: ConstraintLayout = itemView.findViewById(R.id.layout_unread_messages_li_chat)
        private var tvUnreadMessageCount: MaterialTextView = itemView.findViewById(R.id.tvUnreadMessageCount_li_chat)

        private val mStatusMap:MutableMap<Double, Int> = mutableMapOf(
            NOT_SENT to R.drawable.ic_access_time,
            SENT to R.drawable.ic_check,
            DELIVERED to R.drawable.ic_check_double,
            READ to R.drawable.ic_done_all
        )
        //endregion

        fun bind(entity: Person){
            ivProfile.displayImage(entity.imageUri)

            var name:String = entity.fullName
            if(entity.userAccountType == "DOCTOR")
                name = "Dr $name"

            tvPersonName.text = name

            if(!entity.lastMessageTimeStamp.isNullOrEmpty()){
                /*val time:String = TimeUtils.convertTimeToString(entity.lastMessageTimeStamp)
                tvLastMessageTimestamp.text = time*/
            }

            tvLastMessage.text = entity.lastMessageContent

            if(entity.lastMessageType == OUTGOING_MESSAGE)
                ivLastMessageStatus.displayImage(mStatusMap[entity.lastMessageStatus]!!)
            else
                ivLastMessageStatus.visibility = View.GONE

            if(entity.unreadMessageCount > 0){
                layout_unread_messages.visibility = View.VISIBLE
                tvUnreadMessageCount.text = entity.unreadMessageCount.toString()

                val color:Int = ContextCompat.getColor(layout_unread_messages.context, R.color.appGreen)
                tvLastMessageTimestamp.setTextColor(color)
                tvLastMessage.setTextColor(color)
            }else{
                layout_unread_messages.visibility = View.GONE
            }

            rootView.setOnClickListener {
                AppController.notifyObservers(EVENT_OPEN_MESSAGE_ACTIVITY_2, entity)
            }
        }
    }
}