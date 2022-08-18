package com.slyworks.medix.di

import androidx.lifecycle.viewmodel.CreationExtras
import com.slyworks.auth.LoginManager
import com.slyworks.auth.PersonsManager
import com.slyworks.auth.RegistrationManager
import com.slyworks.auth.UsersManager
import com.slyworks.communication.CallHistoryManager
import com.slyworks.communication.ConnectionStatusManager
import com.slyworks.communication.MessageManager
import com.slyworks.medix.helpers.VibrationManager
import com.slyworks.network.NetworkRegister
import com.slyworks.userdetails.UserDetailsUtils
import com.slyworks.utils.PreferenceManager


/**
 *Created by Joshua Sylvanus, 9:07 PM, 03/08/2022.
 */

val networkRegisterKey: CreationExtras.Key<NetworkRegister> = object : CreationExtras.Key<NetworkRegister>{}
val loginManagerKey: CreationExtras.Key<LoginManager> = object : CreationExtras.Key<LoginManager>{}
val registrationManagerKey: CreationExtras.Key<RegistrationManager> = object : CreationExtras.Key<RegistrationManager>{}
val preferenceManagerKey: CreationExtras.Key<PreferenceManager> = object : CreationExtras.Key<PreferenceManager>{}
val userDetailsUtilsKey: CreationExtras.Key<UserDetailsUtils> = object : CreationExtras.Key<UserDetailsUtils>{}
val usersManagerKey: CreationExtras.Key<UsersManager> = object : CreationExtras.Key<UsersManager>{}
val callHistoryManagerKey: CreationExtras.Key<CallHistoryManager> = object : CreationExtras.Key<CallHistoryManager>{}
val messageManagerKey: CreationExtras.Key<MessageManager> = object : CreationExtras.Key<MessageManager>{}
val connectionStatusManagerKey: CreationExtras.Key<ConnectionStatusManager> = object : CreationExtras.Key<ConnectionStatusManager>{}
val personsManagerKey: CreationExtras.Key<PersonsManager> = object : CreationExtras.Key<PersonsManager>{}
val vibrationManagerKey: CreationExtras.Key<VibrationManager> = object : CreationExtras.Key<VibrationManager>{}