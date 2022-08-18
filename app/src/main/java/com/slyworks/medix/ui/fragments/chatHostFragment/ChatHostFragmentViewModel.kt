package com.slyworks.medix.ui.fragments.chatHostFragment

import androidx.lifecycle.ViewModel
import com.slyworks.models.room_models.FBUserDetails
import com.slyworks.userdetails.UserDetailsUtils
import javax.inject.Inject


/**
 *Created by Joshua Sylvanus, 4:21 PM, 07/08/2022.
 */
class ChatHostFragmentViewModel
    @Inject
    constructor(private val userDetailsUtils: UserDetailsUtils) : ViewModel() {
    fun getUserDetailsUser(): FBUserDetails  = userDetailsUtils.user!!
}