package com.slyworks.medix.ui.fragments.chatFragment

import android.content.Context
import android.content.Intent
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
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.slyworks.constants.EVENT_OPEN_MESSAGE_ACTIVITY
import com.slyworks.constants.EVENT_OPEN_MESSAGE_ACTIVITY_2
import com.slyworks.constants.EXTRA_USER_PROFILE_FBU
import com.slyworks.constants.PATIENT
import com.slyworks.medix.AppController
import com.slyworks.medix.AppController.clearAndRemove
import com.slyworks.medix.R
import com.slyworks.medix.Subscription
import com.slyworks.medix.UserDetailsUtils
import com.slyworks.medix.navigation.ActivityWrapper
import com.slyworks.medix.navigation.NavigationManager
import com.slyworks.medix.ui.activities.mainActivity.MainActivity
import com.slyworks.medix.ui.activities.messageActivity.MessageActivity
import com.slyworks.medix.ui.fragments.callsHistoryFragment.CallsHistoryFragment
import com.slyworks.medix.ui.fragments.findDoctorsFragment.FindDoctorsFragment
import com.slyworks.medix.ui.fragments.homeFragment.DoctorHomeFragment
import com.slyworks.medix.utils.*
import com.slyworks.models.room_models.FBUserDetails
import com.slyworks.models.room_models.Person


class ChatFragment : Fragment(), com.slyworks.models.models.Observer {
    //region Vars
    private val TAG: String? = ChatFragment::class.simpleName

    private lateinit var srlMain:SwipeRefreshLayout

    private lateinit var layout_chat_empty:ConstraintLayout
    private lateinit var rvChats:RecyclerView
    private lateinit var fabStartChat: FloatingActionButton
    private lateinit var rootView:CoordinatorLayout
    private lateinit var progress:ProgressBar
    private lateinit var layout_error:ConstraintLayout
    private lateinit var tvRetry:TextView
    private lateinit var btnRetry:Button
    private lateinit var progress_retry:ProgressBar

    private lateinit var mAdapter:RVChatAdapter
    private lateinit var mAdapter2:RVChatAdapter2
    private lateinit var mViewModel:ChatFragmentViewModel

    private var mSubscriptionsList:MutableList<Subscription> = mutableListOf()

    private var mIsBeingLoaded:Boolean = false
    private val MIN_CHECK_TIME:Long = 5_000
    private var mLastCheckTime:Long = System.currentTimeMillis()
    //endregion
    companion object {
        @JvmStatic
        fun newInstance(): ChatFragment {
            return ChatFragment()
        }
    }

    override fun onStart() {
        super.onStart()

    }

    override fun onStop() {
        super.onStop()
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
        srlMain = view.findViewById(R.id.srlChatFragment)

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
            if(UserDetailsUtils.user!!.accountType == PATIENT)
                    f = FindDoctorsFragment.newInstance()
            else
                    f = DoctorHomeFragment.getInstance()

            (requireActivity() as MainActivity)
                .inflateFragment(f)
        }

        srlMain.setOnRefreshListener { getData(0) }

        btnRetry.setOnClickListener { getData(1) }

    }

    private fun initViews2(view:View){
        mAdapter2 = RVChatAdapter2()
        rvChats.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        rvChats.addItemDecoration(DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL))
        rvChats.adapter = mAdapter2
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
                    toggleLayoutIntroStatus(false)
                    toggleLayoutErrorStatus(false)

                    //noinspection unchecked
                    val data:MutableList<Person> = it.getValue() as MutableList<Person>
                    mAdapter2.submitList(data)
                }
                it.isFailure  ->{
                    toggleLayoutErrorStatus(false)
                    toggleLayoutIntroStatus(true)
                    val message:String? = it.getAdditionalInfo() as? String
                    message?.let{it2 -> showMessage(it2, rootView) }
                }
                it.isError ->{
                    toggleLayoutIntroStatus(false)
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

        getData(2)
    }

    private fun getData(from:Int){
        val condition = System.currentTimeMillis() - mLastCheckTime > MIN_CHECK_TIME
        if(mIsBeingLoaded || !condition)
            return

        mIsBeingLoaded = true
        mLastCheckTime = System.currentTimeMillis()
        when(from){
            0 -> srlMain.isRefreshing = true
            1 -> progress_retry.isVisible = true
            2 -> progress.isVisible = true
        }

        mViewModel.getChats()
    }

    private fun toggleLayoutErrorStatus(status:Boolean){
        layout_error.isVisible = status
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
                    imageUri = result.senderImageUri
                )

                startActivity(
                    Intent(requireActivity(),MessageActivity::class.java)
                        .apply {
                            putExtra(EXTRA_USER_PROFILE_FBU, entity)
                        }
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mSubscriptionsList.forEach { it.clearAndRemove() }
    }
}