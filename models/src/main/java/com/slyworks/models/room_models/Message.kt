package com.slyworks.models.room_models

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.slyworks.constants.INCOMING_MESSAGE
import com.slyworks.constants.NOT_SENT
import kotlinx.parcelize.Parcelize


/**
 *Created by Joshua Sylvanus, 6:16 PM, 1/9/2022.
 */
@Parcelize
@Entity
data class Message(
    @ColumnInfo(name = "type") var type:String = "",
    @ColumnInfo(name = "from_uid") var fromUID:String = "",
    @ColumnInfo(name = "to_uid") var toUID:String = "",
    @ColumnInfo(name = "sender_fullname") var senderFullName:String = "",
    @ColumnInfo(name = "receiver_fullname") var receiverFullName:String = "",
    @ColumnInfo(name = "content") var content:String = "",
    @PrimaryKey
    @ColumnInfo(name = "time_stamp") var timeStamp:String = "",
    @ColumnInfo(name = "message_id") var messageID:String = "",
    @ColumnInfo(name = "status") var status:Double = NOT_SENT,
    @ColumnInfo(name = "sender_image_uri") var senderImageUri:String = "",
    @ColumnInfo(name = "account_type") var accountType:String = "",
    @ColumnInfo(name = "sender_fcm_registration_token")var FCMRegistrationToken:String = "")
    : Parcelable, Comparable<Message> {

    constructor():this(
        type = "",
        fromUID = "",
        toUID = "",
        senderFullName = "",
        receiverFullName = "",
        content = "",
        timeStamp = "",
        messageID = "",
        status = NOT_SENT,
        senderImageUri = "",
        accountType = "",
        FCMRegistrationToken = "")

    companion object{
        fun cloneFrom(message:Message):Message{
            return Message(
                type = INCOMING_MESSAGE,
                fromUID = message.fromUID,
                toUID = message.toUID,
                senderFullName = message.senderFullName,
                receiverFullName = message.receiverFullName,
                content = message.content,
                timeStamp = message.timeStamp,
                messageID = message.messageID,
                status = message.status,
                senderImageUri = message.senderImageUri,
                accountType = message.accountType,
                FCMRegistrationToken = message.FCMRegistrationToken
            )
        }
    }
    override fun compareTo(other: Message): Int {
        val otherTimeStamp:Long = other.timeStamp.toLong()
        if(this.timeStamp.toLong() > otherTimeStamp)
            return 1
        else if(this.timeStamp.toLong() < otherTimeStamp)
            return -1
        else if(this.timeStamp.toLong() == otherTimeStamp)
            return 0
        else
            throw UnsupportedOperationException("cannot sort unknown order of value")
    }
}