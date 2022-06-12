package com.slyworks.models.room_models

import androidx.room.ColumnInfo
import androidx.room.Entity


/**
 *Created by Joshua Sylvanus, 9:58 AM, 13/05/2022.
 */
@Entity
data class FCMToken(
    @ColumnInfo(name = "token") var token:String = "") {
    constructor():this(
        token = "")
}