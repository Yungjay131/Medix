package com.slyworks.models.room_models


/**
 *Created by Joshua Sylvanus, 10:54 PM, 1/17/2022.
 */
data class FBUserDetailsWrapper(
    var details: FBUserDetails = FBUserDetails()
){
        constructor():this(FBUserDetails())
    }

data class MessagePersonWrapper(
    var messages:MutableList<Message> = mutableListOf()){
        constructor():this(mutableListOf())
    }