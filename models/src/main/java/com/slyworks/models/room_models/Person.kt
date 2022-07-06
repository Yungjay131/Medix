package com.slyworks.models.room_models

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.slyworks.constants.NOT_SENT
import kotlinx.parcelize.Parcelize


/**
 *Created by Joshua Sylvanus, 10:20 PM, 26/04/2022.
 */
@Parcelize
@Entity
data class Person(
    @PrimaryKey
    @ColumnInfo(name = "firebase_uid") var firebaseUID:String = "",
    @ColumnInfo(name = "account_type") var userAccountType:String = "",
    @ColumnInfo(name = "last_message_type") var lastMessageType:String = "",
    @ColumnInfo(name = "last_message_content") var lastMessageContent:String? = "",
    @ColumnInfo(name = "last_message_status") var lastMessageStatus:Double = NOT_SENT,
    @ColumnInfo(name = "last_message_timestamp") var lastMessageTimeStamp:String = "",
    @ColumnInfo(name = "sender_image_uri") var senderImageUri:String = "",
    @ColumnInfo(name = "fullname") var fullName:String = "",
    @ColumnInfo(name = "unread_message_count") var unreadMessageCount:Int = 0,
    @ColumnInfo(name = "fcm_registration_token") var FCMRegistrationToken:String = "",

):Parcelable {
    constructor() : this(
        firebaseUID = "",
        userAccountType = "",
        lastMessageType = "",
        lastMessageContent = "",
        lastMessageStatus = NOT_SENT,
        lastMessageTimeStamp = "",
        senderImageUri = "",
        fullName = "",
        unreadMessageCount = 0,
        FCMRegistrationToken = "")

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Person
        if (firebaseUID != other.firebaseUID) return false

        return true
    }

    override fun hashCode(): Int {
        return firebaseUID.hashCode()
    }


}