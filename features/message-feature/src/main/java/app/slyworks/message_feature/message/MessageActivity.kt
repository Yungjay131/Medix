package app.slyworks.message_feature.message

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.slyworks.base_feature.BaseActivity
import app.slyworks.base_feature.MOnBackPressedCallback
import app.slyworks.constants_lib.*
import app.slyworks.data_lib.models.FBUserDetailsVModel
import app.slyworks.data_lib.models.MessageVModel
import app.slyworks.message_feature.R
import app.slyworks.message_feature.custom_views.SpacingItemDecorator
import app.slyworks.message_feature.databinding.ActivityMessageBinding
import app.slyworks.navigation_feature.Navigator
import app.slyworks.navigation_feature.Navigator.Companion.getParcelable
import app.slyworks.utils_lib.IDHelper.Companion.generateNewMessageID
import app.slyworks.utils_lib.utils.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.jakewharton.rxbinding4.widget.textChanges
import de.hdodenhof.circleimageview.CircleImageView
import io.reactivex.rxjava3.disposables.CompositeDisposable
import javax.inject.Inject

class MessageActivity : BaseActivity() {
    //region Vars
    private val disposables: CompositeDisposable = CompositeDisposable()
    private lateinit var userProfile: FBUserDetailsVModel

    private lateinit var binding:ActivityMessageBinding
    private lateinit var adapter: RVMessageAdapter


    @Inject
    lateinit var viewModel: MessageActivityViewModel
    //endregion

    override fun onDestroy() {
        super.onDestroy()
        disposables.clear()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        initDI()

        super.onCreate(savedInstanceState)
        binding = ActivityMessageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initData()
        initViews()
    }

    private fun initDI(){
        /*application.appComponent
            .activityComponentBuilder()
            .setActivity(this)
            .build()
            .inject(this)*/
    }

    private fun initData(){
        //fixme:could be a FBUserDetails object with some values missing if it comes from ChatFragment and not ViewProfileActivity
        userProfile = intent.getParcelable<FBUserDetailsVModel>(EXTRA_USER_PROFILE_FBU)!!

        this.onBackPressedDispatcher
            .addCallback(this, MOnBackPressedCallback(this))

        viewModel.uiStateLD.observe(this){
            when(it){
                is MessageA_UIState.END_CALL -> handleEndCallState()
                is MessageA_UIState.MessageUpdate -> handleMessageUpdateState(it.message)
                is MessageA_UIState.Loading -> handleLoadingState(it.status)
                is MessageA_UIState.StartCallDetailsUpdate -> handleStartCallDetailsUpdate(it.status, it.details)
                is MessageA_UIState.MessageListUpdate -> handleMessageListUpdate(it.status, it.list)
                is MessageA_UIState.UserDetailsUpdate -> handleUserDetailsUpdate(it.details)
                is MessageA_UIState.ConnectionStatusUpdate -> handleConnectionStatusUpdate(it.status)
            }
        }

        /*clearing the user's unread message count*/
        viewModel.updatePersonLastMessageInfo(userProfile.firebaseUID)
    }

    private fun initViews(){
        binding.ivProfileFragMessage.displayImage(userProfile.imageUri)

        val name =
            if(userProfile.accountType == "DOCTOR")
                "Dr. ${userProfile.fullName}"
            else
                userProfile.fullName
        binding.tvNameFragMessage.text = name

        binding.ivbackFragMessage.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        adapter = RVMessageAdapter(binding.rvMessagesFragMessage, viewModel.timeHelper)
        binding.rvMessagesFragMessage.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.rvMessagesFragMessage.addItemDecoration(SpacingItemDecorator())
        //binding.rvMessagesFragMessage.addItemDecoration(EdgeItemDecorator())
        //binding.rvMessagesFragMessage.addItemDecoration(StickyHeaderItemDecorator())
        binding.rvMessagesFragMessage.adapter = adapter

        disposables +=
            binding.lMessage.etMessageMessage.textChanges()
                .subscribe { toggleFabSendVisibility(it.isNotEmpty()) }

        //implement in the adapter
        binding.fabScrollDownFragMessage.setOnClickListener { adapter.scrollToTop() }
        binding.fabScrollDownFragMessage.setOnClickListener { adapter.scrollToBottom() }

        binding.ivVideoCallFragMessage.setOnClickListener { startCall() }

        binding.lMessage.fabSendLayoutMessage.setOnClickListener {
            val message: MessageVModel = MessageVModel(
                type = OUTGOING_MESSAGE,
                fromUID = viewModel.getUserDetailsUtils().firebaseUID,
                toUID = userProfile.firebaseUID,
                senderFullName = viewModel.getUserDetailsUtils().fullName,
                receiverFullName = userProfile.fullName,
                content = binding.lMessage.etMessageMessage.text.toString().trim(),
                timeStamp = System.currentTimeMillis().toString(),
                messageID = generateNewMessageID(),
                status = NOT_SENT,
                senderImageUri = viewModel.getUserDetailsUtils().imageUri,
                accountType = viewModel.getUserDetailsUtils().accountType,
                FCMRegistrationToken = viewModel.getUserDetailsUtils().FCMRegistrationToken,
                receiverImageUri = userProfile.imageUri )

            viewModel.sendMessage(message)

            binding.lMessage.etMessageMessage.getText().clear()
            closeKeyboard3()
        }
    }

    private fun startCall(){
        /*checking for a property to ensure its the complete FBUserDetails object at this point*/
        if(userProfile.firstName.isNullOrEmpty()){
            showMessage("setting up your call, please wait", binding.root)
            viewModel.getUserDetails(userProfile.firebaseUID)
            return
        }

        /*fixme:VideoCallActivity is expecting a Bundle, fix that*/
        Navigator.intentFor(this, VIDEOCALL_ACTIVITY_INTENT_FILTER)
            .addExtra(EXTRA_VIDEO_CALL_TYPE, VIDEO_CALL_OUTGOING)
            .addExtra(EXTRA_VIDEO_CALL_USER_DETAILS, userProfile)
            .navigate()
    }

    private fun toggleFabSendVisibility(status:Boolean){
        binding.lMessage.fabSendLayoutMessage.isVisible = status
        binding.lMessage.fabRecordLayoutMessage.isVisible = !status
    }

    private fun handleEndCallState(){}
    private fun handleMessageUpdateState(message:String){ displayMessage(message, binding.root) }
    private fun handleLoadingState(status:Boolean){
    /*TODO:show shimmer here */
        binding.root.setChildViewsStatus(status)
    }

    private fun handleStartCallDetailsUpdate(status:Boolean, details:FBUserDetailsVModel?){
        if(status){
            userProfile = details!!
            startCall()
        }
    }
    private fun handleUserDetailsUpdate(details:FBUserDetailsVModel){}
    private fun handleMessageListUpdate(status:Boolean, list:List<MessageVModel>?){
      if(status)
        adapter.submitList(list)
    }

    private fun handleConnectionStatusUpdate(status:String){
        if (status != "online")
            binding.tvConnectionStatusFragMessage.setTextColor(
                ContextCompat.getColor(this, app.slyworks.base_feature.R.color.appGrey_li_message_from))
        else
            binding.tvConnectionStatusFragMessage.setTextColor(
                ContextCompat.getColor(this, app.slyworks.base_feature.R.color.appGreen_text))

        binding.tvConnectionStatusFragMessage.setText(status)
    }


}