package com.slyworks.medix.ui.fragments.chatFragment

import androidx.recyclerview.widget.DiffUtil
import com.slyworks.models.room_models.CallHistory
import com.slyworks.models.room_models.Message
import com.slyworks.models.room_models.Person

/**
 *Created by Joshua Sylvanus, 5:47 AM, 28/04/2022.
 */
class PersonDiffUtilCallback : DiffUtil.ItemCallback<Person>(){
    override fun areItemsTheSame(oldItem: Person, newItem: Person): Boolean {
        return oldItem.firebaseUID == newItem.firebaseUID
    }

    override fun areContentsTheSame(oldItem: Person, newItem: Person): Boolean {
        return oldItem == newItem
    }
}

class MessageDiffUtilCallback : DiffUtil.ItemCallback<Message>(){
    override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
        return oldItem.messageID == newItem.messageID
    }

    override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
        return oldItem.content == newItem.content
    }

}
class CallsHistoryDiffUtilCallback : DiffUtil.ItemCallback<CallHistory>(){
    override fun areItemsTheSame(oldItem: CallHistory, newItem: CallHistory): Boolean {
        return oldItem.callerUID == newItem.callerUID
    }

    override fun areContentsTheSame(oldItem: CallHistory, newItem: CallHistory): Boolean {
        return oldItem.status == newItem.status
    }
}