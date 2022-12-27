package app.slyworks.core_feature.view_profile

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.constraintlayout.widget.Group
import androidx.constraintlayout.widget.Guideline
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import app.slyworks.constants_lib.*
import app.slyworks.controller_lib.AppController
import app.slyworks.controller_lib.Observer
import app.slyworks.controller_lib.Subscription
import app.slyworks.controller_lib.clearAndRemove
import app.slyworks.core_feature.AppBarStateChangeListener
import app.slyworks.core_feature.ProfileHostFragment
import app.slyworks.core_feature.ProfileHostFragmentViewModel
import app.slyworks.core_feature.R
import app.slyworks.data_lib.models.ConsultationRequestVModel
import app.slyworks.data_lib.models.FBUserDetailsVModel
import app.slyworks.models_commons_lib.models.MessageMode
import app.slyworks.navigation_feature.Navigator
import app.slyworks.utils_lib.utils.displayImage
import app.slyworks.utils_lib.utils.displayMessage
import app.slyworks.utils_lib.utils.plusAssign
import app.slyworks.utils_lib.utils.setChildViewsStatus
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import de.hdodenhof.circleimageview.CircleImageView
import io.reactivex.rxjava3.disposables.CompositeDisposable
import timber.log.Timber
import javax.inject.Inject

class ViewProfileFragment : Fragment(), Observer {
    //region Vars
    private lateinit var ivBack:ImageView
    private lateinit var ivBack2:ImageView
    private lateinit var ivProfileSmall:CircleImageView
    private lateinit var tvNameSmall:TextView
    private lateinit var progress_small:ProgressBar

    private lateinit var rootView:CoordinatorLayout
    private lateinit var progress:ProgressBar
    private lateinit var ivProfile:ImageView
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
    private lateinit var group_toolbar:Group

    private lateinit var appBarLayout: AppBarLayout

    private lateinit var rootView_inner:ConstraintLayout
    private lateinit var guide_vertical_1:Guideline
    private lateinit var guide_vertical_2:Guideline

    private var ANCHOR:Int = R.id.divider_horizontal_3

    private val subscriptionList:MutableList<Subscription> = mutableListOf()
    private val disposables:CompositeDisposable = CompositeDisposable()

    private var userProfile: FBUserDetailsVModel? = null

    private var areFABsDisplayed:Boolean = false

    lateinit var viewModel: ProfileHostFragmentViewModel
    //endregion

    companion object {

        @JvmStatic
        fun newInstance(args:Any): ViewProfileFragment =
             ViewProfileFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(EXTRA_USER_PROFILE_ARGS, args as FBUserDetailsVModel)
                }
            }

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

         viewModel = (parentFragment as ProfileHostFragment).viewModel
    }

    override fun onDestroy() {
        subscriptionList.forEach { it.clearAndRemove() }
        disposables.clear()
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userProfile = requireArguments().getParcelable<FBUserDetailsVModel>(EXTRA_USER_PROFILE_ARGS)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_view_profile, container, false)
    }

    private fun initViews(view:View){
        ivBack = view.findViewById(R.id.ivBack_frag_view_profile)
        ivBack2 = view.findViewById(R.id.ivBack_frag_view_profile2)

        ivProfileSmall = view.findViewById(R.id.ivProfile_small_frag_view_profile)
        tvNameSmall = view.findViewById(R.id.tvProfile_small_frag_view_profile)
        progress_small = view.findViewById(R.id.progress)

        rootView = view.findViewById(R.id.rootView)
        progress = view.findViewById(R.id.progress_frag_view_profile)
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
        group_toolbar = view.findViewById(R.id.group_2_frag_view_profile)

        appBarLayout = view.findViewById(R.id.appBarLayout_frag_view_profile)

        guide_vertical_1 = view.findViewById(R.id.guide_vertical_2)
        guide_vertical_2 = view.findViewById(R.id.guide_vertical_5)

        progress.visibility = View.VISIBLE
        rootView.setChildViewsStatus(false)

        ivProfile.displayImage(userProfile!!.imageUri)
        ivProfileSmall.displayImage(userProfile!!.imageUri)

        ivBack.setOnClickListener{
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        ivBack2.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        tvNameSmall.text = "Dr. ${userProfile!!.firstName} ${userProfile!!.lastName}"
        tvFirstName.text = userProfile!!.firstName
        tvLastName.text = userProfile!!.lastName
        tvSex.text = userProfile!!.sex

        val anim_fab_open:Animation = AnimationUtils.loadAnimation(requireContext(), app.slyworks.base_feature.R.anim.compound_fab_open_anim)
        val anim_fab_close:Animation = AnimationUtils.loadAnimation(requireContext(), app.slyworks.base_feature.R.anim.compound_fab_close_anim)
        val anim_fab_rotate_forward:Animation = AnimationUtils.loadAnimation(requireContext(), app.slyworks.base_feature.R.anim.compound_fab_rotate_forward)
        val anim_fab_rotate_backward:Animation = AnimationUtils.loadAnimation(requireContext(), app.slyworks.base_feature.R.anim.compound_fab_rotate_backward)

        fun toggleFABAnimation(status:Boolean){
            if (status){
                group_fabs.visibility = View.VISIBLE
                fabToggleFABsStatus.startAnimation(anim_fab_rotate_forward)
                fabSendRequest.startAnimation(anim_fab_open)
                fabMessage.startAnimation(anim_fab_open)
                fabVoiceCall.startAnimation(anim_fab_open)
                fabVideoCall.startAnimation(anim_fab_open)
            } else{
                fabToggleFABsStatus.startAnimation(anim_fab_rotate_backward)
                fabSendRequest.startAnimation(anim_fab_close)
                fabMessage.startAnimation(anim_fab_close)
                fabVoiceCall.startAnimation(anim_fab_close)
                fabVideoCall.startAnimation(anim_fab_close)
                group_fabs.visibility = View.GONE
            }
        }

       appBarLayout.addOnOffsetChangedListener(object: AppBarStateChangeListener(){
           override fun onStateChanged(appBarLayout: AppBarLayout, state: AppBarState) {
               when(state){
                   AppBarState.IDLE ->{}
                   AppBarState.EXPANDED ->{
                       group_toolbar.visibility = View.GONE
                   }
                   AppBarState.COLLAPSED ->{
                       /*close fab*/
                       group_toolbar.visibility = View.VISIBLE
                       toggleFABAnimation(false.also { areFABsDisplayed = false })
                   }

               }
           }
       })

        fabToggleFABsStatus.setOnClickListener {
            toggleFABAnimation(!areFABsDisplayed.also { areFABsDisplayed = !areFABsDisplayed })
        }

        fabSendRequest.setOnClickListener {
            val request2: ConsultationRequestVModel = ConsultationRequestVModel(userProfile!!.firebaseUID, System.currentTimeMillis().toString(),  viewModel.getUserDetailsUser(), REQUEST_PENDING)
           viewModel.sendConsultationRequest(request2)

            Timber.e("initViews: FirebaseCloudMessage sent to ${userProfile!!.fullName}")
        }

        fabSendRequest.setOnLongClickListener{
            val message:String = "Hi i'm ${viewModel.getUserDetailsUser().fullName}. Please i would like a consultation with you"
            val request: ConsultationRequestVModel = ConsultationRequestVModel(userProfile!!.firebaseUID,  System.currentTimeMillis().toString(), viewModel.getUserDetailsUser(), REQUEST_PENDING)
            viewModel.sendConsultationRequest(request, mode = MessageMode.CLOUD_MESSAGE)

            Timber.e("initViews: FirebaseCloudMessage sent to ${userProfile!!.fullName}")
            true
        }

        fabMessage.setOnClickListener {
            Navigator.intentFor(requireActivity(), MESSAGE_ACTIVITY_INTENT_FILTER)
                .addExtra(EXTRA_USER_PROFILE_FBU, userProfile!!)
                .finishCaller()
                .navigate()
        }

        fabVoiceCall.setOnClickListener {}

        fabVideoCall.setOnClickListener {
            Navigator.intentFor(requireActivity(), VIDEOCALL_ACTIVITY_INTENT_FILTER)
                .addExtra(EXTRA_VIDEO_CALL_TYPE, VIDEO_CALL_OUTGOING)
                .addExtra(EXTRA_VIDEO_CALL_USER_DETAILS, userProfile!!)
                .finishCaller()
                .navigate()
        }

        if(userProfile == null || userProfile?.specialization.isNullOrEmpty())
            return

        tvSpecialization.visibility = View.VISIBLE
        inflateSpecializationViews(userProfile!!.specialization!!)
    }


    private fun inflateSpecializationViews(list:MutableList<String>){
       for(i in 0 until list.size)
           addTextView(list[i])
    }

    private fun addTextView(text:String){
        val layout:TextView = TextView(requireContext())
        layout.setTextColor(ContextCompat.getColor(requireContext(), app.slyworks.base_feature.R.color.appTextColor3))
        layout.setId(View.generateViewId())
        layout.setPadding(0,0,0,8)
        layout.maxLines = 2
        layout.textSize = 20F
        layout.text = text

        val constraintSet:ConstraintSet = ConstraintSet()
        constraintSet.clone(rootView_inner)

        rootView_inner.addView(layout)

        constraintSet.constrainWidth(layout.id, ConstraintSet.MATCH_CONSTRAINT)
        constraintSet.constrainHeight(layout.id, ConstraintSet.WRAP_CONTENT)

        constraintSet.connect(layout.id, ConstraintSet.START, guide_vertical_1.id,
            ConstraintSet.END, resources.getDimensionPixelSize(app.slyworks.base_feature.R.dimen.layout_size_margin6))
        constraintSet.connect(layout.id, ConstraintSet.END, guide_vertical_2.id, ConstraintSet.START)
        constraintSet.connect(
            layout.id,
            ConstraintSet.TOP,
            ANCHOR,
            ConstraintSet.BOTTOM,
            resources.getDimensionPixelSize(app.slyworks.base_feature.R.dimen.layout_size_margin3))

        tvSpecialization.visibility = View.VISIBLE
        constraintSet.applyTo(rootView_inner)
        ANCHOR = layout.id
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initData()
        initViews(view)
    }

    private fun initData(){
        val subscription2: Subscription = AppController.subscribeTo(EVENT_SEND_REQUEST, this)
        subscriptionList.add(subscription2)

        disposables +=
        AppController.subscribeTo<Boolean>(EVENT_SEND_REQUEST)
        .subscribe{
            if(it)
              displayMessage("your request was successfully sent", rootView)
            else
              displayMessage("your request was not sent successfully", rootView)
        }

        viewModel.consultationRequestStatusLiveData.observe(viewLifecycleOwner){
            progress.visibility = View.GONE
            rootView.setChildViewsStatus(true)

            when(it){
                REQUEST_PENDING -> {
                    toggleFABStatus(false, fabSendRequest,fabMessage,fabVoiceCall, fabVideoCall)
                    displayMessage("you have a pending request. you cannot message or video call this user until your request is accepted", rootView)
                }
                REQUEST_ACCEPTED -> {
                    toggleFABStatus(true, fabSendRequest,fabMessage,fabVoiceCall, fabVideoCall)
                    displayMessage("${userProfile!!.fullName} accepted your consultation request", rootView)
                }
                REQUEST_DECLINED -> {
                    toggleFABStatus(true, fabSendRequest)
                    toggleFABStatus(false, fabMessage,fabVoiceCall,fabVideoCall)
                    displayMessage("Sorry this user declined your consultation request. You can still send another consultation request", rootView)
                }
                REQUEST_NOT_SENT -> {
                    toggleFABStatus(true, fabSendRequest)
                    toggleFABStatus(false, fabMessage,fabVoiceCall,fabVideoCall)
                }
                REQUEST_ERROR,
                REQUEST_FAILED -> {
                    displayMessage("oh oh something went wrong, check your network and swipe down to refresh", rootView)
                }
            }
        }
        viewModel.observeConsultationRequestStatus(userProfile!!.firebaseUID)

    }

    private fun toggleFABStatus(status:Boolean,vararg FAB:FloatingActionButton) {
        val color_black = ContextCompat.getColor(requireContext(), app.slyworks.base_feature.R.color.appTextColor3)
        val color_grey = ContextCompat.getColor(requireContext(), app.slyworks.base_feature.R.color.appGrey_shimmer)
        if (status) {
            FAB.forEach {
                //it.setBackgroundTintList(ColorStateList.valueOf(color_blue))
                it.setImageTintList(ColorStateList.valueOf(color_black))
                it.isEnabled = status
            }
        } else{
            FAB.forEach {
                //it.setBackgroundTintList(ColorStateList.valueOf(color_grey))
                it.setImageTintList(ColorStateList.valueOf(color_grey))
                it.isEnabled = status
            }
     }
    }

    override fun <T> notify(event: String, data: T?) {
        when(event){
            EVENT_SEND_REQUEST -> displayMessage("your request was successfully sent", rootView)
        }
    }
}