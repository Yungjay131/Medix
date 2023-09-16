package app.slyworks.requests_feature

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import app.slyworks.base_feature.BaseActivity
import app.slyworks.base_feature.MOnBackPressedCallback
import app.slyworks.constants_lib.*
import app.slyworks.data_lib.vmodels.FBUserDetailsVModel
import app.slyworks.data_lib.models.ConsultationResponse
import app.slyworks.requests_feature._di.ActivityComponent

import app.slyworks.requests_feature.databinding.ActivityViewRequestBinding
import app.slyworks.utils_lib.utils.displayImage
import app.slyworks.utils_lib.utils.displayMessage
import app.slyworks.utils_lib.utils.setChildViewsStatus
import dev.joshuasylvanus.navigator.Navigator
import dev.joshuasylvanus.navigator.Navigator.Companion.getExtra

import javax.inject.Inject

class ViewRequestActivity : BaseActivity() {

    private lateinit var requestStatus:String
    private lateinit var userUID:String
    private lateinit var userDetails: FBUserDetailsVModel

    private lateinit var binding: ActivityViewRequestBinding

    @Inject
    lateinit var viewModel: ViewRequestViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        initDI()

        super.onCreate(savedInstanceState)

        binding = ActivityViewRequestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initData()
        initViews()
    }

    private fun initDI(){
      ActivityComponent.getInitialBuilder()
          .build()
          .inject(this)
    }

    private fun initData(){
        this.onBackPressedDispatcher
            .addCallback(this, MOnBackPressedCallback(this))

        /* checking if there is a signed in user */
        if(!viewModel.getLoginStatus()){
           Navigator.intentFor(this, LOGIN_ACTIVITY_INTENT_FILTER)
                .addExtra(EXTRA_LOGIN_DESTINATION, VIEW_REQUESTS_ACTIVITY_INTENT_FILTER)
                .addExtra(EXTRA_INITIAL_EXTRA, intent.getExtra<Bundle>(EXTRA_ACTIVITY)!!)
                .newAndClearTask()
                .navigate()
        }

        with(intent.getExtra<Bundle>(EXTRA_ACTIVITY)!!){
            requestStatus = getString(EXTRA_CLOUD_MESSAGE_STATUS)!!
            userUID = getString(EXTRA_CLOUD_MESSAGE_FROM_UID)!!
        }

        viewModel.uiStateLD.observe(this){
            when(it){
                is ViewRequestUIState.LoadingStarted -> {
                    binding.progressLayout.isVisible = true
                    binding.rootView.setChildViewsStatus(false)
                }

                is ViewRequestUIState.LoadingStopped -> {
                    binding.progressLayout.isVisible = false
                    binding.rootView.setChildViewsStatus(true)
                }

                is ViewRequestUIState.UserDetailsRetrieved -> {
                    userDetails = it.details
                    binding.toolbarViewRequest.ivProfileSmallViewRequest.displayImage(userDetails.imageUri)
                    binding.toolbarViewRequest.ivProfileViewRequest.displayImage(userDetails.imageUri)
                    binding.toolbarViewRequest.tvProfileSmallViewRequest.text = userDetails.fullName
                    binding.tvNameViewRequest.text = userDetails.fullName
                    binding.tvSexViewRequest.text = userDetails.sex
                    binding.tvAgeViewRequest.text = userDetails.age
                }

                is ViewRequestUIState.UserDetailsNotRetrieved ->{
                    displayMessage(it.error, binding.root)
                }

                is ViewRequestUIState.SendResponseSuccess -> {
                    displayMessage("response sent", binding.root)
                    finish()
                }

                is ViewRequestUIState.SendResponseFailure ->{
                    displayMessage(it.error, binding.root)
                }

                is ViewRequestUIState.Message ->{
                    displayMessage(it.message, binding.root)
                }


            }
        }

        viewModel.getUserDetails(userUID)
    }

    private fun initViews(){
        val response = ConsultationResponse(
            toUID = userUID,
            fromUID = viewModel.getUserDetailsUtils().firebaseUID,
            toFCMRegistrationToken = viewModel.getUserDetailsUtils().fcm_registration_token,
            status = REQUEST_ACCEPTED,
            fullName = viewModel.getUserDetailsUtils().fullName )

        val navigateBackFunc:(View) -> Unit = { _ -> this.onBackPressedDispatcher.onBackPressed() }

        binding.toolbarViewRequest.ivBackViewRequest.setOnClickListener(navigateBackFunc)
        binding.toolbarViewRequest.ivBackViewRequest2.setOnClickListener(navigateBackFunc)

        binding.btnAcceptViewRequest.setOnClickListener{
            viewModel.respondToRequest(response)
        }
        binding.btnDeclineViewRequest.setOnClickListener {
            viewModel.respondToRequest(response.apply { status = REQUEST_DECLINED } )
        }
    }
}