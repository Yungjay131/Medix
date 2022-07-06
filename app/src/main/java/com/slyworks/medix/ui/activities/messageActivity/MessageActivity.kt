package com.slyworks.medix.ui.activities.messageActivity

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxbinding4.widget.textChanges
import com.slyworks.constants.*
import com.slyworks.medix.R
import com.slyworks.medix.utils.UserDetailsUtils
import com.slyworks.medix.managers.TimeUtils
import com.slyworks.medix.ui.activities.BaseActivity
import com.slyworks.medix.navigation.Navigator
import com.slyworks.medix.navigation.Navigator.Companion.getParcelable
import com.slyworks.medix.navigation.addExtra
import com.slyworks.medix.ui.activities.videoCallActivity.VideoCallActivity
import com.slyworks.medix.ui.custom_views.EdgeItemDecorator
import com.slyworks.medix.ui.custom_views.SpacingItemDecorator
import com.slyworks.medix.ui.custom_views.StickyHeaderItemDecorator
import com.slyworks.medix.utils.*
import com.slyworks.medix.utils.ViewUtils.closeKeyboard3
import com.slyworks.medix.utils.ViewUtils.displayImage
import com.slyworks.models.room_models.FBUserDetails
import com.slyworks.models.room_models.Message
import de.hdodenhof.circleimageview.CircleImageView
import io.reactivex.rxjava3.disposables.CompositeDisposable

class MessageActivity : BaseActivity() {
    //region Vars
    private lateinit var rootView:ConstraintLayout

    private lateinit var ivBack:ImageView
    private lateinit var ivProfile:CircleImageView
    private lateinit var tvName:TextView
    private lateinit var tvConnectionStatus:TextView
    private lateinit var ivVideoCall:ImageView
    private lateinit var ivVoiceCall:ImageView
    private lateinit var ivMore:ImageView

    private lateinit var rvMessages:RecyclerView
    private lateinit var fabScrollUp:FloatingActionButton
    private lateinit var fabScrollDown:FloatingActionButton

    private lateinit var ivEmoji:ImageView
    private lateinit var ivAttachment:ImageView
    private lateinit var etMessage:EditText
    private lateinit var fabSend:FloatingActionButton
    private lateinit var fabVoiceNote:FloatingActionButton

    private lateinit var progress:ProgressBar

    private val mSubscriptions:CompositeDisposable = CompositeDisposable()

    private lateinit var mAdapter:RVMessageAdapter

    private lateinit var mUserProfile: FBUserDetails
    private lateinit var mViewModel:MessageViewModel
    //endregion

    override fun onDestroy() {
        super.onDestroy()
        mSubscriptions.clear()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_message)

        initViews1()
        initViews2()
        initData()
    }

    private fun setUserData(userDetails: FBUserDetails){
        ivProfile.displayImage(userDetails.imageUri)

        val name =
            if(userDetails.accountType == "DOCTOR")
                "Dr. ${userDetails.fullName}"
            else
                userDetails.fullName
        tvName.text = name
    }

    private fun initData(){
        //fixme:could be a FBUserDetails object with some values missing if it comes from ChatFragment and not ViewProfileActivity
        mUserProfile = intent.getParcelable<FBUserDetails>(EXTRA_USER_PROFILE_FBU)
        setUserData(mUserProfile)

        this.onBackPressedDispatcher
            .addCallback(this, MOnBackPressedCallback(this))

        mViewModel = ViewModelProvider(this).get(MessageViewModel::class.java)
        mViewModel.observeConnectionStatus(mUserProfile.firebaseUID)
                  .observe(this){
                      if (it != "online")
                          tvConnectionStatus.setTextColor(
                              ContextCompat.getColor(this, R.color.appGrey_li_message_from)
                          )
                      else
                          tvConnectionStatus.setTextColor(
                              ContextCompat.getColor(this, R.color.appGreen_text)
                          )

                      tvConnectionStatus.setText(it)
        }

        mViewModel.observeMessagesForUID(mUserProfile.firebaseUID)
                  .observe(this) {
                      mAdapter.setMessageList(it)
        }

        mViewModel.observeUserDetails(mUserProfile.firebaseUID)
            .observe(this){
                setUserData(it)
        }

        mViewModel.mProgressLiveData.observe(this){
            progress.isVisible = it
        }

        mViewModel.mStatusLiveData.observe(this){
            showMessage(it,rootView)
        }
    }

    private fun initViews1(){
        rootView = findViewById(R.id.rootView)
        ivBack = findViewById(R.id.ivback_frag_message)
        ivProfile = findViewById(R.id.ivProfile_frag_message)
        tvName = findViewById(R.id.tvName_frag_message)
        tvConnectionStatus = findViewById(R.id.tvConnectionStatus_frag_message)

        ivVoiceCall = findViewById(R.id.ivVoiceCall_frag_message)
        ivVideoCall = findViewById(R.id.ivVideoCall_frag_message)
        ivMore = findViewById(R.id.ivMore_frag_message)

        rvMessages = findViewById(R.id.rvMessages_frag_message)

        fabScrollUp = findViewById(R.id.fab_scroll_up_frag_message)
        fabScrollDown = findViewById(R.id.fab_scroll_down_frag_message)

        ivEmoji = findViewById(R.id.ivEmoji)
        ivAttachment = findViewById(R.id.ivAttachment)
        etMessage = findViewById(R.id.etMessage_message)
        fabSend = findViewById(R.id.fab_send_layout_message)
        fabVoiceNote = findViewById(R.id.fab_record_layout_message)

        progress = findViewById(R.id.progress_layout)
    }

    private fun initViews2(){
        progress.visibility = View.VISIBLE

        ivBack.setOnClickListener {
           onBackPressedDispatcher.onBackPressed()
        }

        mAdapter = RVMessageAdapter(rvMessages)
        rvMessages.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rvMessages.addItemDecoration(SpacingItemDecorator())
        //rvMessages.addItemDecoration(EdgeItemDecorator())
        //rvMessages.addItemDecoration(StickyHeaderItemDecorator())
        rvMessages.adapter = mAdapter

        val d = etMessage.textChanges()
            .subscribe {
                toggleFabSendVisibility(it.isNotEmpty())
            }
        mSubscriptions.add(d)

        //implement in the adapter
        fabScrollUp.setOnClickListener { mAdapter.scrollToTop() }
        fabScrollDown.setOnClickListener { mAdapter.scrollToBottom() }

        ivVideoCall.setOnClickListener {
            /*checking for a property to ensure its the complete FBUserDetails object at this point*/
            if(mUserProfile.firstName.isNullOrEmpty()){
                showMessage("incomplete user details, contact support", rootView)
                return@setOnClickListener
            }

            /*fixme:VideoCallActivity is expecting a Bundle, fix that*/
            Navigator.intentFor<VideoCallActivity>(this)
                .addExtra(EXTRA_VIDEO_CALL_TYPE, VIDEO_CALL_OUTGOING)
                .addExtra(EXTRA_VIDEO_CALL_USER_DETAILS, mUserProfile)
                .finishCaller()
                .navigate()
        }

        fabSend.setOnClickListener {
            val message: Message = Message(
                type = OUTGOING_MESSAGE,
                fromUID = UserDetailsUtils.user!!.firebaseUID,
                toUID = mUserProfile.firebaseUID,
                senderFullName = UserDetailsUtils.user!!.fullName,
                receiverFullName = mUserProfile.fullName,
                content = etMessage.text.toString().trim(),
                timeStamp = TimeUtils.getCurrentDate().toString(),
                messageID = IDUtils.generateNewMessageID(),
                status = NOT_SENT,
                senderImageUri = UserDetailsUtils.user!!.imageUri,
                accountType = UserDetailsUtils.user!!.accountType,
                FCMRegistrationToken = UserDetailsUtils.user!!.FCMRegistrationToken
            )

            mViewModel.sendMessage(message)

            etMessage.getText().clear()
            closeKeyboard3()
        }
    }

    private fun toggleFabSendVisibility(status:Boolean){
        fabSend.isVisible = status
        fabVoiceNote.isVisible = !status
    }

    private fun displayMessage(message:String) =
        Snackbar.make(rootView, message, Snackbar.LENGTH_LONG).show();

}