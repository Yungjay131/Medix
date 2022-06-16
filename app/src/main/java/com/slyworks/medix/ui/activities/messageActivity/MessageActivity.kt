package com.slyworks.medix.ui.activities.messageActivity

import android.app.Activity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.slyworks.constants.*
import com.slyworks.medix.AppController.clearAndRemove
import com.slyworks.medix.R
import com.slyworks.medix.Subscription
import com.slyworks.medix.UserDetailsUtils
import com.slyworks.medix.ui.activities.BaseActivity
import com.slyworks.models.room_models.Message
import com.slyworks.medix.navigation.ActivityWrapper
import com.slyworks.medix.navigation.NavigationManager
import com.slyworks.medix.ui.custom_views.EdgeItemDecorator
import com.slyworks.medix.ui.custom_views.SpacingItemDecorator
import com.slyworks.medix.ui.custom_views.StickyHeaderItemDecorator
import com.slyworks.medix.utils.*
import com.slyworks.medix.utils.ViewUtils.displayImage
import com.slyworks.models.models.Observer
import com.slyworks.models.room_models.FBUserDetails
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MessageActivity : BaseActivity(), Observer {
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

    private lateinit var progress:ConstraintLayout

    private val mSubscriptionsList:MutableList<Subscription> = mutableListOf()

    private lateinit var mAdapter:RVMessageAdapter

    private lateinit var mUserProfile: FBUserDetails
    private lateinit var mViewModel:MessageViewModel

    //endregion

    companion object{
        private var mIsInForeground:Boolean = false
        fun getForegroundStatus():Boolean = mIsInForeground
    }

    override fun onStart() {
        super.onStart()
        super.onStop(this)
        mIsInForeground = true
    }

    override fun onStop() {
        super.onStop()
        super.onStop(this)
        mIsInForeground = false
    }

    override fun onDestroy() {
        mSubscriptionsList.forEach { it.clearAndRemove() }
        super.onDestroy(this)
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_message)

        initViews2()
        initData()

        super.onCreate(this)
    }

    private fun setUserData(userDetails: FBUserDetails){
        ivProfile.displayImage(userDetails.imageUri)

        val name = if(userDetails.accountType == "DOCTOR") "Dr. ${userDetails.fullName}"
        else userDetails.fullName

        tvName.text = name
    }

    private fun initData(){
        //fixme:could be a FBUserDetails object with some values missing if it comes from ChatFragment and not ViewProfileActivity
        mUserProfile = intent.getParcelableExtra<FBUserDetails>(EXTRA_USER_PROFILE_FBU)!!
        setUserData(mUserProfile)

        mViewModel = ViewModelProvider(this).get(MessageViewModel::class.java)
        mViewModel.observeConnectionStatus(mUserProfile.firebaseUID).observe(this){
            if(it != "online")
                tvConnectionStatus.setTextColor(ContextCompat.getColor(this, R.color.appGrey_li_message_from))
            else
                tvConnectionStatus.setTextColor(ContextCompat.getColor(this, R.color.appGreen_text))

            tvConnectionStatus.setText(it)

        }

        mViewModel.observeMessagesForUID(mUserProfile.firebaseUID).observe(this) {
            progress.visibility = View.GONE

            if (it.isNullOrEmpty()){
                showMessage("error occurred retrieving messages", rootView)
                return@observe
            }
            mAdapter.setMessageList(it)
        }

        mViewModel.observeUserDetails(mUserProfile.firebaseUID).observe(this){
            setUserData(it)
        }

    }

    private fun initViews2(){
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
        progress.visibility = View.VISIBLE

        ivBack.setOnClickListener {
           NavigationManager.inflateActivity(
               this@MessageActivity,
               ActivityWrapper.MAIN,
               removeCurrentFromBackStack = true)
        }

        mAdapter = RVMessageAdapter(rvMessages)
        rvMessages.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rvMessages.addItemDecoration(SpacingItemDecorator())
        rvMessages.addItemDecoration(EdgeItemDecorator())
        rvMessages.addItemDecoration(StickyHeaderItemDecorator())
        rvMessages.adapter = mAdapter
        rvMessages.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                CoroutineScope(Dispatchers.Main).launch {
                    delay(6_000)

                    fabScrollUp.visibility = View.GONE
                    fabScrollDown.visibility = View.GONE
                }
            }
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                when(newState){
                    RecyclerView.SCROLL_STATE_IDLE ->{}
                    RecyclerView.SCROLL_STATE_SETTLING ->{}
                    RecyclerView.SCROLL_STATE_DRAGGING ->{
                        val currentFirstVisibleItem = (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                        val currentLastVisibleItem = (recyclerView.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()

                        if(currentFirstVisibleItem > 0){
                            //means the first item is not visible
                            fabScrollUp.visibility = View.VISIBLE
                        }
                        if(currentLastVisibleItem < recyclerView.adapter!!.itemCount - 1){
                            //means the last item is not visible
                            fabScrollDown.visibility = View.VISIBLE
                        }

                        if (currentFirstVisibleItem == 0){
                            fabScrollUp.visibility = View.GONE
                        }

                        if(currentLastVisibleItem == recyclerView.adapter!!.itemCount - 1)
                            fabScrollDown.visibility = View.GONE

                    }
                }
            }

        })

        etMessage.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(p0.toString().isNotEmpty()) {
                   toggleFabSendVisibility(true)
                }else{
                    toggleFabSendVisibility(false)
                }

            }
        })

        //implement in the adapter
        fabScrollUp.setOnClickListener { mAdapter.scrollToTop() }
        fabScrollDown.setOnClickListener { mAdapter.scrollToBottom() }

        ivVideoCall.setOnClickListener {
            /*checking for a property to ensure its the complete FBUserDetails object at this point*/
            if(mUserProfile.firstName.isNullOrEmpty()){
                showMessage("incomplete user details, contact support", rootView)
                return@setOnClickListener
            }

            NavigationManager.inflateActivity(
                this@MessageActivity,
                ActivityWrapper.VIDEO_CALL,
                isToBeFinished = true,
                extras = Bundle().apply {
                    putString(EXTRA_VIDEO_CALL_TYPE, VIDEO_CALL_OUTGOING)
                    putParcelable(EXTRA_VIDEO_CALL_USER_DETAILS, mUserProfile)
                })
        }

        fabSend.setOnClickListener {
            val _content:String = etMessage.text.toString().trim()
            val message: com.slyworks.models.room_models.Message = com.slyworks.models.room_models.Message(
                type = OUTGOING_MESSAGE,
                fromUID = UserDetailsUtils.user!!.firebaseUID,
                toUID = mUserProfile.firebaseUID,
                senderFullName = UserDetailsUtils.user!!.fullName,
                receiverFullName = mUserProfile.fullName,
                content = _content,
                timeStamp = TimeUtils.getCurrentDate().toString(),
                messageID = IDUtils.generateNewMessageID(),
                status = NOT_SENT,
                senderImageUri = UserDetailsUtils.user!!.imageUri,
                accountType = UserDetailsUtils.user!!.accountType,
                FCMRegistrationToken = UserDetailsUtils.user!!.FCMRegistrationToken
            )

            mViewModel.sendMessage(message)

            etMessage.getText().clear()
            closeKeyboard()
        }
    }

    private fun toggleFabSendVisibility(status:Boolean){
        if(status){
            fabSend.visibility = View.VISIBLE
            fabVoiceNote.visibility = View.GONE
        }else{
            fabSend.visibility = View.GONE
            fabVoiceNote.visibility = View.VISIBLE
        }
    }

    private fun closeKeyboard(){
        val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(rootView.windowToken, 0)
    }

    private fun displayMessage(message:String){
        Snackbar.make(rootView, message, Snackbar.LENGTH_LONG).show();
    }
    override fun <T> notify(event: String, data: T?) {}

    override fun onBackPressed() {
        NavigationManager.onBackPressed(this,
            shouldFinishCurrent = true,
            fallbackActivity = ActivityWrapper.MAIN)
    }
}