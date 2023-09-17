package app.slyworks.core_feature.view_profile

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.constraintlayout.widget.Group
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import app.slyworks.constants_lib.*
import app.slyworks.controller_lib.Subscription
import app.slyworks.core_feature.AppBarStateChangeListener
import app.slyworks.core_feature.ProfileHostFragment
import app.slyworks.core_feature.ProfileHostFragmentViewModel
import app.slyworks.core_feature.R
import app.slyworks.core_feature.databinding.FragmentViewProfileBinding
import app.slyworks.data_lib.vmodels.ConsultationRequestVModel
import app.slyworks.data_lib.vmodels.FBUserDetailsVModel
import app.slyworks.data_lib.model.MessageMode
import app.slyworks.utils_lib.*

import app.slyworks.utils_lib.utils.displayImage
import app.slyworks.utils_lib.utils.displayMessage
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dev.joshuasylvanus.navigator.Navigator
import io.reactivex.rxjava3.disposables.CompositeDisposable
import timber.log.Timber
import app.slyworks.base_feature.R as Base_R

class ViewProfileFragment : Fragment() {
    //region Vars

    private lateinit var group_toolbar:Group

    private lateinit var rootView_inner:ConstraintLayout

    private var ANCHOR:Int = R.id.tv_specialization
    private var areFABsDisplayed:Boolean = false

    private val subscriptionList:MutableList<Subscription> = mutableListOf()
    private val disposables:CompositeDisposable = CompositeDisposable()

    private var userProfile: FBUserDetailsVModel? = null

    private lateinit var binding:FragmentViewProfileBinding
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentViewProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initData()
        initViews()
    }

    private fun initData(){
        userProfile = requireArguments().getParcelable<FBUserDetailsVModel>(EXTRA_USER_PROFILE_ARGS)

        viewModel.consultationRequestStatusLiveData.observe(viewLifecycleOwner){

            when(it){
                REQUEST_PENDING -> {
                    toggleFABStatus(false,
                        binding.toolbar.fabSendRequest,
                        binding.toolbar.fabMessage,
                        binding.toolbar.fabVoiceCall,
                        binding.toolbar.fabVideoCall)
                    displayMessage("you have a pending request. you cannot message or video call this user until your request is accepted", binding.root)
                }
                REQUEST_ACCEPTED -> {
                    toggleFABStatus(true,
                        binding.toolbar.fabSendRequest,
                        binding.toolbar.fabMessage,
                        binding.toolbar.fabVoiceCall,
                        binding.toolbar.fabVideoCall)
                    displayMessage("${userProfile!!.fullName} accepted your consultation request", binding.root)
                }
                REQUEST_DECLINED -> {
                    toggleFABStatus(true,
                        binding.toolbar.fabSendRequest)
                    toggleFABStatus(false,
                        binding.toolbar.fabMessage,
                        binding.toolbar.fabVoiceCall,
                        binding.toolbar.fabVideoCall)
                    displayMessage("Sorry this user declined your consultation request. You can still send another consultation request", binding.root)
                }
                REQUEST_NOT_SENT -> {
                    toggleFABStatus(true,
                        binding.toolbar.fabSendRequest)
                    toggleFABStatus(false,
                        binding.toolbar.fabMessage,
                        binding.toolbar.fabVoiceCall,
                        binding.toolbar.fabVideoCall)
                }
                REQUEST_ERROR,
                REQUEST_FAILED -> {
                    displayMessage("oh oh something went wrong, check your network and swipe down to refresh", binding.root)
                }
            }
        }
        viewModel.observeConsultationRequestStatus(userProfile!!.firebaseUID)

    }

    private fun initViews(){
        binding.toolbar.ivProfileFragViewProfile.displayImage(userProfile!!.imageUri)
        binding.toolbar.ivProfileSmallFragViewProfile.displayImage(userProfile!!.imageUri)

        binding.toolbar.ivBackFragViewProfile.setOnClickListener{
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        binding.toolbar.ivBackFragViewProfile2.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.toolbar.tvProfileSmallFragViewProfile.text = "Dr. ${userProfile!!.firstName} ${userProfile!!.lastName}"
        binding.tvFirstName.text = userProfile!!.firstName
        binding.tvLastName.text = userProfile!!.lastName
        binding.tvSex.text = userProfile!!.sex

        val anim_fab_open:Animation = AnimationUtils.loadAnimation(requireContext(), Base_R.anim.compound_fab_open_anim)
        val anim_fab_close:Animation = AnimationUtils.loadAnimation(requireContext(), Base_R.anim.compound_fab_close_anim)
        val anim_fab_rotate_forward:Animation = AnimationUtils.loadAnimation(requireContext(), Base_R.anim.compound_fab_rotate_forward)
        val anim_fab_rotate_backward:Animation = AnimationUtils.loadAnimation(requireContext(), Base_R.anim.compound_fab_rotate_backward)

        fun toggleFABAnimation(status:Boolean){
            if (status){
                binding.toolbar.groupFabs.visibility = View.VISIBLE
                binding.toolbar.fabToggleFabStatus.startAnimation(anim_fab_rotate_forward)
                binding.toolbar.fabSendRequest.startAnimation(anim_fab_open)
                binding.toolbar.fabMessage.startAnimation(anim_fab_open)
                binding.toolbar.fabVoiceCall.startAnimation(anim_fab_open)
                binding.toolbar.fabVideoCall.startAnimation(anim_fab_open)
            } else{
                binding.toolbar.fabToggleFabStatus.startAnimation(anim_fab_rotate_backward)
                binding.toolbar.fabSendRequest.startAnimation(anim_fab_close)
                binding.toolbar.fabMessage.startAnimation(anim_fab_close)
                binding.toolbar.fabVoiceCall.startAnimation(anim_fab_close)
                binding.toolbar.fabVideoCall.startAnimation(anim_fab_close)
                binding.toolbar.groupFabs.visibility = View.GONE
            }
        }

       binding.toolbar.appBarLayoutFragViewProfile.addOnOffsetChangedListener(
           object: AppBarStateChangeListener(){
           override fun onStateChanged(appBarLayout: AppBarLayout, state: AppBarState) {
               when(state){
                   AppBarState.IDLE ->{}
                   AppBarState.EXPANDED ->{
                       binding.toolbar.groupAppBar.visibility = View.GONE
                   }
                   AppBarState.COLLAPSED ->{
                       /*close fab*/
                       binding.toolbar.groupAppBar.visibility = View.VISIBLE
                       toggleFABAnimation(false.also { areFABsDisplayed = false })
                   }

               }
           }
       })

        binding.toolbar.fabToggleFabStatus.setOnClickListener {
            toggleFABAnimation(!areFABsDisplayed.also { areFABsDisplayed = !areFABsDisplayed })
        }

        binding.toolbar.fabSendRequest.setOnClickListener {
            val request2: ConsultationRequestVModel =
                ConsultationRequestVModel(
                    userProfile!!.firebaseUID,
                    System.currentTimeMillis().toString(),
                    viewModel.getUserDetailsUser(),
                    REQUEST_PENDING
                )
           viewModel.sendConsultationRequest(request2)

            Timber.e("initViews: FirebaseCloudMessage sent to ${userProfile!!.fullName}")
        }

        binding.toolbar.fabSendRequest.setOnLongClickListener{
            val message:String = "Hi i'm ${viewModel.getUserDetailsUser().fullName}, and i would like a consultation with you"
            val request: ConsultationRequestVModel = ConsultationRequestVModel(userProfile!!.firebaseUID,  System.currentTimeMillis().toString(), viewModel.getUserDetailsUser(), REQUEST_PENDING)
            viewModel.sendConsultationRequest(request, mode = MessageMode.CLOUD_MESSAGE)

            Timber.e("initViews: FirebaseCloudMessage sent to ${userProfile!!.fullName}")
            true
        }

        binding.toolbar.fabMessage.setOnClickListener {
            Navigator.intentFor(requireActivity(), MESSAGE_ACTIVITY_INTENT_FILTER)
                .addExtra(EXTRA_USER_PROFILE_FBU, userProfile!!)
                .navigate()
        }

        binding.toolbar.fabVoiceCall.setOnClickListener {
            Navigator.intentFor(requireActivity(), VOICECALL_ACTIVITY_INTENT_FILTER)
                .addExtra(EXTRA_USER_PROFILE_FBU, userProfile!!)
                .navigate()
        }

        binding.toolbar.fabVideoCall.setOnClickListener {
            Navigator.intentFor(requireActivity(), VIDEOCALL_ACTIVITY_INTENT_FILTER)
                .addExtra(EXTRA_VIDEO_CALL_TYPE, VIDEO_CALL_OUTGOING)
                .addExtra(EXTRA_VIDEO_CALL_USER_DETAILS, userProfile!!)
                .navigate()
        }

        if(userProfile == null || userProfile?.specialization.isNullOrEmpty())
            return

        binding.lSpecialization.visibility = View.VISIBLE
        inflateSpecializationViews(userProfile!!.specialization!!)
    }


    private fun inflateSpecializationViews(list:MutableList<String>){
       binding.tvSpecialization.setText(list[0])

       for(i in 1 until list.size)
           addTextView(list[i])
    }

    private fun addTextView(text:String){
        val layout:TextView = TextView(requireContext())
        layout.setTextColor(ContextCompat.getColor(requireContext(), Base_R.color.appTextColor3))
        layout.setId(View.generateViewId())
        layout.setPadding(0,0,0,8)
        layout.maxLines = 2
        layout.textSize = 20F
        layout.text = text

        val constraintSet:ConstraintSet = ConstraintSet()
        constraintSet.clone(rootView_inner)

        binding.rootViewInner.addView(layout)

        constraintSet.constrainWidth(layout.id, ConstraintSet.MATCH_CONSTRAINT)
        constraintSet.constrainHeight(layout.id, ConstraintSet.WRAP_CONTENT)

        constraintSet.connect(layout.id, ConstraintSet.START, binding.lSpecialization.id,
            ConstraintSet.END)
        constraintSet.connect(layout.id, ConstraintSet.END, binding.lSpecialization.id, ConstraintSet.START)
        constraintSet.connect(
            layout.id,
            ConstraintSet.TOP,
            ANCHOR,
            ConstraintSet.BOTTOM,
            resources.getDimensionPixelSize(Base_R.dimen.layout_size_margin7))

        binding.lSpecialization.visibility = View.VISIBLE
        constraintSet.applyTo(binding.lSpecialization)
        ANCHOR = layout.id
    }

    private fun toggleFABStatus(status:Boolean,vararg FAB:FloatingActionButton) {
        val color_black = ContextCompat.getColor(requireContext(), Base_R.color.appTextColor3)
        val color_grey = ContextCompat.getColor(requireContext(), Base_R.color.appGrey_shimmer)
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

}