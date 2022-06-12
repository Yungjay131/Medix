package com.slyworks.models.room_models

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import kotlinx.parcelize.Parcelize


/**
 *Created by Joshua Sylvanus, 2:35 PM, 1/9/2022.
 */
@Parcelize
data class MessagePersonWithMessages2(
    @Embedded val person: MessagePerson,
    @Relation(
        parentColumn = "firebase_uid",
        entityColumn = "from_uid"
    ) val from_messages: MutableList<Message>,

    @Relation(
        parentColumn = "firebase_uid",
        entityColumn = "to_uid"
    )
    val to_messages:MutableList<Message>,

    @Relation(
        parentColumn = "firebase_uid",
        entityColumn = "from_uid"
    ) val deferred_messages: MutableList<Message>
): Parcelable

/**
 *Created by Joshua Sylvanus, 4:55 PM, 1/20/2022.
 */
data class MessagePersonWithMessages(
    var person: MessagePerson?,
    var details: MessageDetails,
    var messages:MutableList<Message>
)