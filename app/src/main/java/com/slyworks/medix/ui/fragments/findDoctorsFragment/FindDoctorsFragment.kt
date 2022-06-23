package com.slyworks.medix.ui.fragments.findDoctorsFragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.snackbar.Snackbar
import com.slyworks.constants.EVENT_GET_DOCTOR_USERS
import com.slyworks.constants.EVENT_OPEN_VIEW_PROFILE_FRAGMENT
import com.slyworks.medix.AppController
import com.slyworks.medix.AppController.clearAndRemove
import com.slyworks.medix.R
import com.slyworks.medix.Subscription
import com.slyworks.medix.UsersManager
import com.slyworks.medix.navigation.FragmentWrapper
import com.slyworks.medix.navigation.NavigationManager
import com.slyworks.medix.ui.fragments.ProfileHostFragment
import com.slyworks.medix.ui.fragments.ViewProfileFragment
import com.slyworks.models.room_models.FBUserDetails

class FindDoctorsFragment : Fragment(), com.slyworks.models.models.Observer {
   //region Vars
    private lateinit var rvDoctors:RecyclerView
    private lateinit var progress:ProgressBar
    private lateinit var rootView:ConstraintLayout
    private lateinit var layout_intro:ConstraintLayout
    private lateinit var ivLayout_intro:ImageView
    private lateinit var lavLayout_intro:LottieAnimationView
    private lateinit var btnFindDoctors: Button

    private val mSubscriptionsList:MutableList<Subscription> = mutableListOf()

    private lateinit var mViewModel:FindDoctorsViewModel
    private lateinit var mAdapter:RVFindDoctorsAdapter
    //endregion
   companion object {
       @JvmStatic
       fun newInstance(): FindDoctorsFragment  = FindDoctorsFragment()

   }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view =  inflater.inflate(R.layout.fragment_find_doctors, container, false)
        initViews1(view)
        initViews2(view)
        initData()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //initViews_2(view)
    }
    private fun initData(){
        AppController.addEvent(EVENT_GET_DOCTOR_USERS)
        val subscription_1 = AppController.subscribeTo(EVENT_GET_DOCTOR_USERS, this)

        AppController.addEvent(EVENT_OPEN_VIEW_PROFILE_FRAGMENT)
        val subscription_2: Subscription = AppController.subscribeTo(EVENT_OPEN_VIEW_PROFILE_FRAGMENT, this)

        mSubscriptionsList.add(subscription_1)
        mSubscriptionsList.add(subscription_2)

        mViewModel = ViewModelProvider(this).get(FindDoctorsViewModel::class.java)
        mViewModel.mDoctorsListLiveData!!.observe(viewLifecycleOwner, Observer {
            mAdapter.addDoctors(it)
        })


    }
    private fun initViews1(view:View){
        rootView = view.findViewById(R.id.rootView)
        rvDoctors = view.findViewById(R.id.rvFindDoctors_find_doctors)
        progress = view.findViewById(R.id.progress_layout)

        layout_intro = view.findViewById(R.id.layout_intro_frag_find_doctors)
        ivLayout_intro = view.findViewById(R.id.ivFindDoctors_layout_intro)
        lavLayout_intro = view.findViewById(R.id.lavFindDoctors_layout_intro)
        btnFindDoctors = view.findViewById(R.id.btnFindDoctors_frag_find_doctors)

        /*using lottie now*/
        //ivLayout_intro.displayGif(R.drawable.find_doctors)

        btnFindDoctors.setOnClickListener {
            if(!mViewModel.getNetworkStatus()) {
                displayMessage("no network connection")
                return@setOnClickListener
            }

            toggleProgressVisibility(true)
            mViewModel.getAllDoctors()
        }

        mAdapter = RVFindDoctorsAdapter()
    }
    private fun initViews2(view:View){
        rvDoctors.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        rvDoctors.addItemDecoration(DividerItemDecoration(requireContext(),LinearLayoutManager.VERTICAL))
        rvDoctors.adapter = mAdapter
    }

    private fun toggleProgressVisibility(status:Boolean){
        progress.isVisible = status
    }
    private fun displayMessage(message:String){
        Snackbar.make(rootView, message, Snackbar.LENGTH_LONG).show();
    }
    override fun <T> notify(event: String, data: T?) {
        when(event){
            EVENT_GET_DOCTOR_USERS ->{
                val result:Pair<Boolean, MutableList<FBUserDetails>?> = data as Pair<Boolean, MutableList<FBUserDetails>?>

                toggleProgressVisibility(false)
                if(result.first) {
                    mAdapter.addDoctors(result.second!!)
                    layout_intro.visibility = View.GONE
                    rootView.visibility = View.VISIBLE
                }else
                   displayMessage("retrieving users failed please try again")

            }
            EVENT_OPEN_VIEW_PROFILE_FRAGMENT ->{
                val entity: FBUserDetails = data as FBUserDetails

                (requireParentFragment() as ProfileHostFragment)
                    .inflateFragment2(ViewProfileFragment.newInstance(entity))
            }

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mSubscriptionsList.forEach { it.clearAndRemove() }
    }
}