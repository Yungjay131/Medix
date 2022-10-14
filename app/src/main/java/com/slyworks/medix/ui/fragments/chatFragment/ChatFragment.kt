package com.slyworks.medix.ui.fragments.chatFragment

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.slyworks.constants.EVENT_OPEN_MESSAGE_ACTIVITY
import com.slyworks.constants.EVENT_OPEN_MESSAGE_ACTIVITY_2
import com.slyworks.constants.EXTRA_USER_PROFILE_FBU
import com.slyworks.constants.PATIENT
import com.slyworks.controller.AppController
import com.slyworks.controller.AppController.Companion.clearAndRemove
import com.slyworks.controller.Observer
import com.slyworks.controller.Subscription
import com.slyworks.medix.R
import com.slyworks.medix.ui.activities.main_activity.MainActivity
import com.slyworks.medix.ui.activities.main_activity.activityComponent
import com.slyworks.medix.ui.activities.message_activity.MessageActivity
import com.slyworks.medix.ui.fragments.ProfileHostFragment
import com.slyworks.medix.ui.fragments.homeFragment.DoctorHomeFragment
import com.slyworks.models.room_models.FBUserDetails
import com.slyworks.models.room_models.Person

import com.slyworks.navigation.addExtra
import javax.inject.Inject

class ChatFragment : Fragment(), Observer {
    //region Vars
    private val TAG: String? = ChatFragment::class.simpleName

    private lateinit var layout_chat_empty:ConstraintLayout
    private lateinit var rvChats:RecyclerView
    private lateinit var fabStartChat: FloatingActionButton
    private lateinit var rootView:CoordinatorLayout
    private lateinit var progress:ProgressBar
    private lateinit var layout_error:ConstraintLayout
    private lateinit var tvRetry:TextView
    private lateinit var btnRetry:Button
    private lateinit var progress_retry:ProgressBar

    private lateinit var mAdapter2:RVChatAdapter2

    @Inject
    lateinit var mViewModel:ChatFragmentViewModel

    private var mSubscriptionsList:MutableList<Subscription> = mutableListOf()

    //endregion
    companion object {
        @JvmStatic
        fun newInstance(): ChatFragment {
            return ChatFragment()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        context.activityComponent
            .fragmentComponentBuilder()
            .setFragment(this)
            .build()
            .inject(this)
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_chat, container, false)
        initViews1(view)
        initViews2(view)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initData()
    }

    private fun initViews1(view:View){
        layout_chat_empty = view.findViewById(R.id.layout_no_messages_content_chat)
        rvChats = view.findViewById(R.id.rvChats_frag_chat)
        fabStartChat = view.findViewById(R.id.fabStatChat_frag_chat)
        rootView = view.findViewById(R.id.rootView)
        progress = view.findViewById(R.id.progress_layout)
        layout_error = view.findViewById(R.id.layout_error)
        tvRetry = view.findViewById(R.id.tvRetry_content_chat)
        btnRetry = view.findViewById(R.id.btnRetry_content_chat)
        progress_retry = view.findViewById(R.id.progrss_retry)

        fabStartChat.setOnClickListener {
            val f:Fragment
            if(mViewModel.getUserDetailsUtils().accountType == PATIENT)
                    f = ProfileHostFragment.getInstance()
            else
                    f = DoctorHomeFragment.getInstance()

            (requireActivity() as MainActivity)
                .inflateFragment(f)
        }

        btnRetry.setOnClickListener { getData(1) }
    }

    private fun initViews2(view:View){
        mAdapter2 = RVChatAdapter2()
        rvChats.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        rvChats.addItemDecoration(DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL))
        rvChats.adapter = mAdapter2
    }
    private fun initData(){
        val subscription = AppController.subscribeTo(EVENT_OPEN_MESSAGE_ACTIVITY, this)
        val subscription2 = AppController.subscribeTo(EVENT_OPEN_MESSAGE_ACTIVITY_2, this)

        mSubscriptionsList.add(subscription)
        mSubscriptionsList.add(subscription2)

        mViewModel = ViewModelProvider(this).get(ChatFragmentViewModel::class.java)
        mViewModel.successStateLiveData.observe(viewLifecycleOwner){
            mAdapter2.submitList(it)
        }
        mViewModel.intoStateLiveData.observe(viewLifecycleOwner){
            toggleLayoutIntroStatus(it)
        }
        mViewModel.errorStateLiveData.observe(viewLifecycleOwner){
            if(it){
              val text = mViewModel.errorDataLiveData.value!!
              toggleLayoutErrorStatus(it, text)
              return@observe
            }

            toggleLayoutErrorStatus(it)
        }
        mViewModel.progressStateLiveData.observe(viewLifecycleOwner){
            progress.isVisible = it
            if(!it && progress_retry.isVisible)
              progress_retry.visibility = View.GONE
        }

        getData(2)
    }

    private fun getData(from:Int){
        when(from){
            1 -> progress_retry.isVisible = true
            2 -> progress.isVisible = true
        }

        mViewModel.getChats()
    }

    private fun toggleLayoutErrorStatus(status:Boolean,
                                        text:String = "oops, something went wrong on our end, please try again"){
        layout_error.isVisible = status
        tvRetry.text = text
    }
    private fun toggleLayoutIntroStatus(status:Boolean){
        layout_chat_empty.isVisible = status
    }

    private fun displayMessage(message:String){
        Snackbar.make(rootView, message, Snackbar.LENGTH_LONG).show();
    }

    override fun <T> notify(event: String, data: T?) {
        when(event){
            EVENT_OPEN_MESSAGE_ACTIVITY ->{}
            EVENT_OPEN_MESSAGE_ACTIVITY_2 ->{
                /*TODO:this is an incomplete FBUserDetails object*/
                val result: Person = data as Person
                val entity: FBUserDetails = FBUserDetails(
                    accountType = result.userAccountType,
                    firebaseUID = result.firebaseUID,
                    fullName = result.fullName,
                    imageUri = result.imageUri)

                Navigator.intentFor<MessageActivity>(requireActivity())
                    .addExtra<Parcelable>(EXTRA_USER_PROFILE_FBU, entity)
                    .navigate()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mSubscriptionsList.forEach { it.clearAndRemove() }
    }
}