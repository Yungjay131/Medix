package com.slyworks.models.room_models

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize


/**
 *Created by Joshua Sylvanus, 2:32 PM, 1/9/2022.
 */

@Parcelize
@Entity
data class FBUserDetails(
    @ColumnInfo(name = "account_type") var accountType: String = "",
    @ColumnInfo(name = "first_name") var firstName: String = "",
    @ColumnInfo(name = "last_name") var lastName: String = "",
    @ColumnInfo(name = "fullname") var fullName:String = "",
    @ColumnInfo(name = "email") var email: String = "",
    @ColumnInfo(name = "sex") var sex:String = "",
    @ColumnInfo(name = "age") var age:String = "",
    @PrimaryKey
    @ColumnInfo(name = "firebase_uid") var firebaseUID: String = "",
    @ColumnInfo(name = "agora_uid") var agoraUID: String = "",
    @ColumnInfo(name = "fcm_registration_token") var FCMRegistrationToken:String = "",
    @ColumnInfo(name = "image_uri") var imageUri: String = "",
    @Ignore var history: MutableList<String>? = mutableListOf(),
    @Ignore var specialization: MutableList<String>? = mutableListOf() ): Parcelable {
    constructor():
            this("","","","","", "","","","","","", mutableListOf(), mutableListOf())
}
