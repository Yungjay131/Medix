package com.slyworks.medix.ui.activities.requestsActivity

import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.activity.OnBackPressedDispatcher
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.slyworks.constants.*
import com.slyworks.medix.managers.LoginManager
import com.slyworks.medix.R
import com.slyworks.medix.databinding.ActivityViewRequestBinding
import com.slyworks.medix.navigation.ActivityWrapper
import com.slyworks.medix.navigation.Navigator
import com.slyworks.medix.navigation.Navigator.Companion.getExtra
import com.slyworks.medix.navigation.addExtra
import com.slyworks.medix.ui.activities.BaseActivity
import com.slyworks.medix.ui.activities.loginActivity.LoginActivity
import com.slyworks.medix.ui.activities.loginActivity.ViewRequestViewModel
import com.slyworks.medix.utils.MOnBackPressedCallback
import com.slyworks.medix.utils.UserDetailsUtils
import com.slyworks.medix.utils.ViewUtils.displayImage
import com.slyworks.medix.utils.ViewUtils.displaySnackBar
import com.slyworks.medix.utils.ViewUtils.setChildViewsStatus
import com.slyworks.medix.utils.showMessage
import com.slyworks.models.models.ConsultationResponse
import com.slyworks.models.room_models.FBUserDetails

class ViewRequest : BaseActivity() {
    //region Vars
    private lateinit var binding:ActivityViewRequestBinding

    private lateinit var requestStatus:String
    private lateinit var userUID:String
    private lateinit var userDetails:FBUserDetails

    private lateinit var mViewModel:ViewRequestViewModel
    //endregion

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityViewRequestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initData()
        initViews()
    }

    private fun initData(){
        this.onBackPressedDispatcher
            .addCallback(this, MOnBackPressedCallback(this))

        /*checking if there is a signed in user*/
        if(!LoginManager.getInstance().getLoginStatus()){
            Navigator.intentFor<LoginActivity>(this)
                .addExtra<String>(EXTRA_LOGIN_DESTINATION, this::class.simpleName!!)
                .newAndClearTask()
                .finishCaller()
                .navigate()
        }


        val b:Bundle = intent.getExtra<Bundle>(EXTRA_ACTIVITY)!!
        with(b){
            requestStatus = getString(EXTRA_CLOUD_MESSAGE_STATUS)!!
            userUID = getString(EXTRA_CLOUD_MESSAGE_FROM_UID)!!
        }

        mViewModel = ViewModelProvider(this).get(ViewRequestViewModel::class.java)
        mViewModel.progressState.observe(this){
            binding.progressLayout.isVisible = it
            binding.rootView.setChildViewsStatus(it)
        }

        mViewModel.successState.observe(this){
             if(it){
                 userDetails = mViewModel.successData.value!!
                 binding.toolbarViewRequest.ivProfileSmallViewRequest.displayImage(userDetails.imageUri)
                 binding.toolbarViewRequest.ivProfileViewRequest.displayImage(userDetails.imageUri)
                 binding.toolbarViewRequest.tvProfileSmallViewRequest.text = userDetails.fullName
                 binding.tvNameViewRequest.text = userDetails.fullName
                 binding.tvSexViewRequest.text = userDetails.sex
                 binding.tvAgeViewRequest.text = userDetails.age
             }
        }

        mViewModel.errorState.observe(this){
            if(it)
              showMessage(mViewModel.errorData.value!!, binding.rootView)
        }

        mViewModel.getUserDetails(userUID)
    }

    private fun initViews(){
        val response = ConsultationResponse(
            toUID = userDetails.firebaseUID,
            fromUID = UserDetailsUtils.user!!.firebaseUID,
            toFCMRegistrationToken = UserDetailsUtils.user!!.FCMRegistrationToken,
            status = REQUEST_ACCEPTED,
            fullName = UserDetailsUtils.user!!.fullName)

        val navigateBackFunc:(View) -> Unit = { this.onBackPressedDispatcher.onBackPressed() }
        binding.toolbarViewRequest.ivBackViewRequest.setOnClickListener(navigateBackFunc)
        binding.toolbarViewRequest.ivBackViewRequest2.setOnClickListener(navigateBackFunc)
        binding.btnAcceptViewRequest.setOnClickListener{
            mViewModel.respondToRequest(response)
            mViewModel.progressState.postValue(true)
        }
        binding.btnDeclineViewRequest.setOnClickListener {
            mViewModel.respondToRequest(response.apply { status = REQUEST_DECLINED } )
            mViewModel.progressState.postValue(true)
        }
    }
}