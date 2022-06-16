package com.slyworks.medix.ui.fragments

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.constraintlayout.widget.Group
import androidx.constraintlayout.widget.Guideline
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.slyworks.constants.*
import com.slyworks.medix.*
import com.slyworks.medix.AppController.clearAndRemove
import com.slyworks.medix.ui.activities.messageActivity.MessageActivity
import com.slyworks.medix.ui.activities.videoCallActivity.VideoCallActivity
import com.slyworks.medix.utils.*
import com.slyworks.medix.utils.ViewUtils.displayImage
import com.slyworks.medix.utils.ViewUtils.setChildViewsStatus
import com.slyworks.models.models.*
import com.slyworks.models.room_models.FBUserDetails
import de.hdodenhof.circleimageview.CircleImageView

class ViewProfileFragment : Fragment(), com.slyworks.models.models.Observer {
    //region Vars
    private val TAG: String? = ViewProfileFragment::class.simpleName

    private lateinit var ivBack:ImageView
    private lateinit var ivBack2:ImageView
    private lateinit var ivProfileSmall:CircleImageView
    private lateinit var tvNameSmall:TextView
    private lateinit var progress_small:ProgressBar

    private lateinit var rootView:CoordinatorLayout
    private lateinit var progress:ProgressBar
    private lateinit var scrollView:ScrollView
    private lateinit var ivProfile:CircleImageView
    private lateinit var tvFirstName:TextView
    private lateinit var tvLastName:TextView
    private lateinit var tvSex:TextView
    private lateinit var tvSpecialization:TextView

    private lateinit var fabToggleFABsStatus:FloatingActionButton
    private lateinit var fabSendRequest:FloatingActionButton
    private lateinit var fabMessage:FloatingActionButton
    private lateinit var fabVoiceCall:FloatingActionButton
    private lateinit var fabVideoCall:FloatingActionButton

    private lateinit var group_fabs:Group

    private lateinit var appBarLayout: AppBarLayout

    private lateinit var rootView_inner:ConstraintLayout
    private lateinit var guide_vertical_1:Guideline
    private lateinit var guide_vertical_2:Guideline

    //private lateinit var shimmer:ShimmerFrameLayout

    private lateinit var srlMain:SwipeRefreshLayout
    private var ANCHOR:Int = R.id.tvHeaderSpecialization_frag_view_profile

    private val mSubscriptionList:MutableList<Subscription> = mutableListOf()

    private var mUserProfile: FBUserDetails? = null

    private var mAreFABsDisplayed:Boolean = false

    private val MIN_TIME = 5_000
    private var LAST_CHECK_TIME = 0L
    //endregion

    companion object {

        @JvmStatic
        fun newInstance(args:Any):ViewProfileFragment {
            return ViewProfileFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(EXTRA_USER_PROFILE_ARGS, args as FBUserDetails)
                }
            }

        }
    }

    override fun onDestroy() {
        mSubscriptionList.forEach { it.clearAndRemove() }
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mUserProfile = arguments?.getParcelable<FBUserDetails>(EXTRA_USER_PROFILE_ARGS)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_view_profile, container, false)
        initViews_1(view)
        return view
    }

    private fun initViews_1(view:View){
        ivBack = view.findViewById(R.id.ivBack_frag_view_profile)
        ivBack2 = view.findViewById(R.id.ivBack_frag_view_profile2)
        ivProfileSmall = view.findViewById(R.id.ivProfile_small_frag_view_profile)
        tvNameSmall = view.findViewById(R.id.tvProfile_small_frag_view_profile)
        progress_small = view.findViewById(R.id.progress)
        
        rootView = view.findViewById(R.id.rootView)
        progress = view.findViewById(R.id.progress_frag_view_profile)
        scrollView = view.findViewById(R.id.scrollView_frag_view_profile)
        ivProfile = view.findViewById(R.id.ivProfile_frag_view_profile)
        tvFirstName = view.findViewById(R.id.tvFirstName_frag_view_profile)
        tvLastName = view.findViewById(R.id.tvLastName_frag_view_profile)
        tvSex = view.findViewById(R.id.tvSex_frag_view_profile)
        tvSpecialization = view.findViewById(R.id.tvHeaderSpecialization_frag_view_profile)

        rootView_inner = view.findViewById(R.id.rootView_inner)

        fabToggleFABsStatus = view.findViewById(R.id.fabToggleFabsStatus_frag_view_profile)
        fabSendRequest = view.findViewById(R.id.fabSendRequest_frag_view_profile)
        fabMessage = view.findViewById(R.id.fabMessage_frag_view_profile)
        fabVoiceCall = view.findViewById(R.id.fabVoiceCall_frag_view_profile)
        fabVideoCall = view.findViewById(R.id.fabVideoCall_frag_view_profile)

        group_fabs = view.findViewById(R.id.group_1_frag_view_profile);

        appBarLayout = view.findViewById(R.id.appBarLayout_frag_view_profile)

        guide_vertical_1 = view.findViewById(R.id.guide_vertical_1)
        guide_vertical_2 = view.findViewById(R.id.guide_vertical_2)

        srlMain = view.findViewById(R.id.srlMain_frag_view_profile)

        ivProfile.displayImage(mUserProfile!!.imageUri)
        ivProfileSmall.displayImage(mUserProfile!!.imageUri)

        tvFirstName.text = mUserProfile!!.firstName
        tvLastName.text = mUserProfile!!.lastName
        tvSex.text = mUserProfile!!.sex

        val anim_fab_open:Animation = AnimationUtils.loadAnimation(requireContext(), R.anim.compound_fab_open_anim)
        val anim_fab_close:Animation = AnimationUtils.loadAnimation(requireContext(), R.anim.compound_fab_close_anim)
        val anim_fab_rotate_forward:Animation = AnimationUtils.loadAnimation(requireContext(), R.anim.compound_fab_rotate_forward)
        val anim_fab_rotate_backward:Animation = AnimationUtils.loadAnimation(requireContext(), R.anim.compound_fab_rotate_backward)

        fun toggleFABAnimation(status:Boolean){
            if (status){
                fabToggleFABsStatus.startAnimation(anim_fab_rotate_backward)
                fabSendRequest.startAnimation(anim_fab_close)
                fabMessage.startAnimation(anim_fab_close)
                fabVoiceCall.startAnimation(anim_fab_close)
                fabVideoCall.startAnimation(anim_fab_close)
            } else{
                fabToggleFABsStatus.startAnimation(anim_fab_rotate_forward)
                fabSendRequest.startAnimation(anim_fab_open)
                fabMessage.startAnimation(anim_fab_open)
                fabVoiceCall.startAnimation(anim_fab_open)
                fabVideoCall.startAnimation(anim_fab_open)
            }

            mAreFABsDisplayed = status
        }

       appBarLayout.addOnOffsetChangedListener(object:AppBarStateChangeListener(){
           override fun onStateChanged(appBarLayout: AppBarLayout, state: AppBarState) {
               when(state){
                   AppBarState.IDLE ->{}
                   AppBarState.EXPANDED ->{}
                   AppBarState.COLLAPSED ->{
                       /*close fab*/
                       toggleFABAnimation(false)
                   }

               }
           }
       })

        fabToggleFABsStatus.setOnClickListener {
            toggleFABAnimation(mAreFABsDisplayed)
        }

        fabSendRequest.setOnClickListener {
            val message:String = "Hi i'm ${UserDetailsUtils.user!!.fullName}. Please i would like a consultation with you"
            val data: ConsultationRequestData =
                ConsultationRequestData(message, UserDetailsUtils.user!!.firebaseUID, FCM_REQUEST)
            val fcmMessage: FirebaseCloudMessage =
                FirebaseCloudMessage(mUserProfile!!.FCMRegistrationToken, data)
            //CloudMessageManager.sendCloudMessage(fcmMessage)

            val request: Request =
                Request(mUserProfile!!.firebaseUID, UserDetailsUtils.user!!.firebaseUID, message)
            val request2: ConsultationRequest =
                ConsultationRequest(mUserProfile!!.firebaseUID, UserDetailsUtils.user!!, REQUEST_PENDING)
            CloudMessageManager.sendConsultationRequest(request2)

            Log.e(TAG, "initViews: FirebaseCloudMessage sent to ${mUserProfile!!.fullName}")
        }

        fabSendRequest.setOnLongClickListener{
            val message:String = "Hi i'm ${UserDetailsUtils.user!!.fullName}. Please i would like a consultation with you"
            val request: ConsultationRequest =
                ConsultationRequest(mUserProfile!!.firebaseUID, UserDetailsUtils.user!!, REQUEST_PENDING)

            CloudMessageManager.sendConsultationRequest(request, mode = MessageMode.CLOUD_MESSAGE)

            Log.e(TAG, "initViews: FirebaseCloudMessage sent to ${mUserProfile!!.fullName}")
            true
        }

        fabMessage.setOnClickListener {
            val intent:Intent = Intent(requireContext(), MessageActivity::class.java)
            intent.putExtra(EXTRA_USER_PROFILE_FBU, mUserProfile)
            startActivity(intent)
            requireActivity().finish()
        }

        fabVoiceCall.setOnClickListener {

        }

        fabVideoCall.setOnClickListener {
             val intent:Intent = Intent(requireContext(), VideoCallActivity::class.java)
             intent.putExtra(EXTRA_VIDEO_CALL_TYPE, VIDEO_CALL_OUTGOING)
             intent.putExtra(EXTRA_VIDEO_CALL_USER_DETAILS, mUserProfile)
             startActivity(intent)
             requireActivity().finish()
        }

        if(mUserProfile == null || mUserProfile?.specialization.isNullOrEmpty()) return
        tvSpecialization.visibility = View.VISIBLE
        inflateSpecializationViews(mUserProfile!!.specialization!!)
    }


    private fun inflateSpecializationViews(list:MutableList<String>){
       for(i in 0 until list.size)
           addTextView(list[i])
    }

    private fun addTextView(text:String){
        val inflater:LayoutInflater = LayoutInflater.from(requireContext())

        val layout:ConstraintLayout = inflater.inflate(R.layout.layout_textview, rootView_inner, false) as ConstraintLayout
        layout.setId(View.generateViewId())
        val textView:TextView = layout.findViewById(R.id.tvText_layout_textView)
        textView.text = text

        layout.layoutParams.height = 55

        val constraintSet:ConstraintSet = ConstraintSet()
        constraintSet.clone(rootView_inner)

        rootView_inner.addView(layout)

        constraintSet.constrainWidth(layout.id, ConstraintSet.MATCH_CONSTRAINT)
        constraintSet.constrainHeight(layout.id, ConstraintSet.WRAP_CONTENT)

        constraintSet.connect(layout.id, ConstraintSet.START, guide_vertical_1.id, ConstraintSet.END)
        constraintSet.connect(layout.id, ConstraintSet.END, guide_vertical_2.id, ConstraintSet.START)
        constraintSet.connect(
            layout.id,
            ConstraintSet.TOP,
            ANCHOR,
            ConstraintSet.BOTTOM,
            resources.getDimensionPixelSize(R.dimen.layout_size_margin2))

        constraintSet.applyTo(rootView_inner)
        ANCHOR = layout.id
    }

    private fun initViews_2(view:View){
        progress.visibility = View.VISIBLE
        scrollView.setChildViewsStatus(false)
        srlMain.setOnRefreshListener { refresh() }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initData()
        initViews_2(view)
    }

    private fun initData(){
        AppController.addEvent(EVENT_GET_CONSULTATION_REQUEST)
        val subscription_1: Subscription = AppController.subscribeTo(EVENT_GET_CONSULTATION_REQUEST, this)

        AppController.addEvent(EVENT_SEND_REQUEST)
        val subscription_2: Subscription = AppController.subscribeTo(EVENT_SEND_REQUEST, this)

        mSubscriptionList.add(subscription_1)
        mSubscriptionList.add(subscription_2)

        CloudMessageManager.checkRequestStatus(mUserProfile!!.firebaseUID)
    }
    private fun toggleFABStatus(status:Boolean,vararg FAB:FloatingActionButton) {
        val color_blue = ContextCompat.getColor(requireContext(), R.color.appPink)
        val color_grey = ContextCompat.getColor(requireContext(), R.color.appGrey_shimmer)
        if (status) {
            FAB.forEach {
                it.setBackgroundTintList(ColorStateList.valueOf(color_blue))
                it.isEnabled = status
            }
        } else{
            FAB.forEach {
                it.setBackgroundTintList(ColorStateList.valueOf(color_grey))
                it.isEnabled = status
            }
     }
    }

    private fun refresh(){
        val currentTime = System.currentTimeMillis()
        if(currentTime - LAST_CHECK_TIME < MIN_TIME) return

        srlMain.isRefreshing = true
        LAST_CHECK_TIME = currentTime
        CloudMessageManager.checkRequestStatus(mUserProfile!!.firebaseUID)

    }
    private fun displayMessage(message:String){
        Snackbar.make(rootView, message, Snackbar.LENGTH_LONG).show();
    }



    override fun <T> notify(event: String, data: T?) {
        when(event){
            EVENT_GET_CONSULTATION_REQUEST ->{
                if(srlMain.isRefreshing) srlMain.isRefreshing = false

                progress.visibility = View.GONE
                scrollView.setChildViewsStatus(true)

                val result = data as String?
                when(result){
                    REQUEST_PENDING -> {
                        toggleFABStatus(false, fabSendRequest,fabMessage,fabVoiceCall, fabVideoCall)
                        displayMessage("you have a pending request. you cannot message or video call this user until your request is accepted")
                    }
                    REQUEST_ACCEPTED -> {
                        toggleFABStatus(true, fabSendRequest,fabMessage,fabVoiceCall, fabVideoCall)
                    }
                    REQUEST_DECLINED -> {
                        toggleFABStatus(true, fabSendRequest)
                        toggleFABStatus(false, fabMessage,fabVoiceCall,fabVideoCall)
                        displayMessage("Sorry this user declined your consultation request. You can still send another consultation request")
                    }
                    REQUEST_NOT_SENT -> {
                        toggleFABStatus(true, fabSendRequest)
                        toggleFABStatus(false, fabMessage,fabVoiceCall,fabVideoCall)
                    }
                    REQUEST_ERROR,REQUEST_FAILED -> {
                        displayMessage("oh oh something went wrong, check your network and swipe down to refresh")
                    }
                }
            }
            EVENT_SEND_REQUEST ->{
                displayMessage("your request was successfully sent")
            }
        }
    }
}