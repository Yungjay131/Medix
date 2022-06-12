package com.slyworks.medix.ui.fragments.chatFragment

import android.os.Bundle
import android.util.Log
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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.slyworks.constants.EVENT_OPEN_MESSAGE_ACTIVITY
import com.slyworks.constants.EVENT_OPEN_MESSAGE_ACTIVITY_2
import com.slyworks.constants.EXTRA_USER_PROFILE_FBU
import com.slyworks.medix.AppController
import com.slyworks.medix.AppController.clearAndRemove
import com.slyworks.medix.R
import com.slyworks.medix.Subscription
import com.slyworks.medix.navigation.ActivityWrapper
import com.slyworks.medix.navigation.NavigationManager
import com.slyworks.medix.utils.*
import com.slyworks.models.room_models.FBUserDetails
import com.slyworks.models.room_models.Person


class ChatFragment : Fragment(), com.slyworks.models.models.Observer {
    //region Vars
    private val TAG: String? = ChatFragment::class.simpleName

    private lateinit var srlMain:SwipeRefreshLayout

    private lateinit var layout_chat_empty:ConstraintLayout
    //private lateinit var ivChat:ImageView
    //private lateinit var shimmerFrameLayout: ShimmerFrameLayout
    private lateinit var rvChats:RecyclerView
    private lateinit var fabStartChat:ExtendedFloatingActionButton
    private lateinit var rootView:CoordinatorLayout
    private lateinit var progress:ConstraintLayout
    private lateinit var layout_error:ConstraintLayout
    private lateinit var tvRetry:TextView
    private lateinit var btnRetry:Button
    private lateinit var progress_retry:ProgressBar

    private lateinit var mAdapter:RVChatAdapter
    private lateinit var mAdapter2:RVChatAdapter2
    private lateinit var mViewModel:ChatFragmentViewModel

    private var mSubscriptionsList:MutableList<Subscription> = mutableListOf()

    private var mIsBeingLoaded:Boolean = true
    private val MIN_CHECK_TIME:Long = 5_000
    private var mLastCheckTime:Long = System.currentTimeMillis()
    //endregion
    companion object {
        @JvmStatic
        fun newInstance(): ChatFragment {
            return ChatFragment()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_chat, container, false)
        initData()
        initViews1(view)
        initViews2(view)
        return view
    }

    private fun initViews1(view:View){
        srlMain = view.findViewById(R.id.srlChatFragment)

        layout_chat_empty = view.findViewById(R.id.layout_no_messages_content_chat)
        //shimmerFrameLayout = view.findViewById(R.id.layout_shimmer_content_chat)
        //ivChat = view.findViewById(R.id.ivChat_frag_chat)
        rvChats = view.findViewById(R.id.rvChats_frag_chat)
        fabStartChat = view.findViewById(R.id.fabStatChat_frag_chat)
        rootView = view.findViewById(R.id.rootView)
        progress = view.findViewById(R.id.progress)
        layout_error = view.findViewById(R.id.layout_error)
        tvRetry = view.findViewById(R.id.tvRetry_content_chat)
        btnRetry = view.findViewById(R.id.btnRetry_content_chat)
        progress_retry = view.findViewById(R.id.progrss_retry)

            /*TODO:abstract, looks 'dirty'*/
        srlMain.setOnRefreshListener {
            if(!mIsBeingLoaded && ((System.currentTimeMillis() - mLastCheckTime) > MIN_CHECK_TIME)){
                srlMain.isRefreshing = true
                mIsBeingLoaded = true
                mViewModel.getChats()
            }
        }

        btnRetry.setOnClickListener {
            progress_retry.visibility = View.VISIBLE
            mIsBeingLoaded = true
            mViewModel.getChats()
        }

        progress.visibility = View.VISIBLE
    }

    private fun initViews2(view:View){
        mAdapter2 = RVChatAdapter2()
        rvChats.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        rvChats.addItemDecoration(DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL))
        rvChats.adapter = mAdapter
    }
    private fun initData(){
        AppController.addEvent(EVENT_OPEN_MESSAGE_ACTIVITY)
        AppController.addEvent(EVENT_OPEN_MESSAGE_ACTIVITY_2)
        val subscription = AppController.subscribeTo(EVENT_OPEN_MESSAGE_ACTIVITY, this)
        val subscription2 = AppController.subscribeTo(EVENT_OPEN_MESSAGE_ACTIVITY_2, this)
        mSubscriptionsList.add(subscription)
        mSubscriptionsList.add(subscription2)

        mViewModel = ViewModelProvider(this).get(ChatFragmentViewModel::class.java)
        mViewModel.getPersonsListLiveData().observe(viewLifecycleOwner){
            Log.e(TAG, "initData#getPersonsListLiveData: running on ${Thread.currentThread().name}")
            when{
                it.isSuccess ->{
                    toggleLayoutErrorStatus(false)
                    val data:MutableList<com.slyworks.models.room_models.Person> = it.getValue() as MutableList<com.slyworks.models.room_models.Person>
                    if(data.isNullOrEmpty()){
                        toggleLayoutIntroStatus(true)
                        return@observe
                    }

                    mAdapter2.submitList(data)
                }
                it.isFailure || it.isError ->{
                    toggleLayoutErrorStatus(true)
                    val message:String? = it.getAdditionalInfo() as? String
                    message?.let{it2 -> showMessage(it2, rootView) }
                }

            }

            srlMain.isRefreshing = false
            progress.visibility = View.GONE
            progress_retry.visibility = View.GONE
            mLastCheckTime = System.currentTimeMillis()
            mIsBeingLoaded = false
        }

        mViewModel.getChats()
    }

    private fun toggleLayoutErrorStatus(status:Boolean){
        layout_error.isVisible = status
    }
    private fun toggleLayoutIntroStatus(status:Boolean){
        layout_chat_empty.isVisible = status
       /* if(status){
            Glide.with(requireContext())
                .asGif()
                .dontTransform()
                .load(R.drawable.chat_empty)
                .into(ivChat)
        }*/
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
                    imageUri = result.senderImageUri
                )

                NavigationManager.inflateActivity(
                    requireActivity(),
                    activity = ActivityWrapper.MESSAGE,
                    extras =  Bundle().apply{
                    putParcelable(EXTRA_USER_PROFILE_FBU, entity)
                })
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mSubscriptionsList.forEach { it.clearAndRemove() }
    }
}