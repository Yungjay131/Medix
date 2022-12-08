package app.slyworks.core_feature.chat

import androidx.lifecycle.ViewModel
import app.slyworks.data_lib.DataManager
import app.slyworks.data_lib.models.FBUserDetailsVModel
import javax.inject.Inject


/**
 *Created by Joshua Sylvanus, 4:21 PM, 07/08/2022.
 */
class ChatHostFragmentViewModel
    @Inject
    constructor(private val dataManager: DataManager) : ViewModel() {

    fun getUserDetailsUser(): FBUserDetailsVModel  = dataManager.getUserDetailsParam<FBUserDetailsVModel>()!!
}