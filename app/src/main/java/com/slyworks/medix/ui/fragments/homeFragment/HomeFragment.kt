package com.slyworks.medix.ui.fragments.homeFragment

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.slyworks.constants.KEY_UNREAD_MESSAGE_COUNT
import com.slyworks.medix.ui.activities.mainActivity.MainActivity
import com.slyworks.medix.R
import com.slyworks.medix.UserDetailsUtils
import com.slyworks.models.models.AccountType
import com.slyworks.medix.navigation.ActivityWrapper
import com.slyworks.medix.navigation.FragmentWrapper
import com.slyworks.medix.navigation.NavigationManager
import com.slyworks.medix.ui.custom_views.HorizontalSpacingItemDecorator
import com.slyworks.medix.utils.PreferenceManager
import com.slyworks.medix.utils.ViewUtils.displayImage
import de.hdodenhof.circleimageview.CircleImageView
import java.util.*

class HomeFragment : Fragment() {
    //region Vars
    private lateinit var ivToggle: ImageView
    private lateinit var ivNotification: CircleImageView
    private lateinit var ivProfile: CircleImageView
    private lateinit var tvUserName: TextView

    private lateinit var layout_unread_message_count:ConstraintLayout
    private lateinit var tvUnreadMessageCount:TextView

    private lateinit var layout_findDoctors:CardView
    private lateinit var ivProfile_find_doctors:CircleImageView
    private lateinit var ivCancel_layout_findDoctors:ImageView

    private lateinit var tvQuickAccess:TextView
    private lateinit var tvTrendingMedicalFields:TextView
    private lateinit var tvRecentConversations:TextView
    private lateinit var tvConsultations:TextView

    private lateinit var rvQuickAccess:RecyclerView
    private lateinit var rvTrendingMedicalFields:RecyclerView
    private lateinit var rvRecentConversations:RecyclerView
    private lateinit var rvConsultations:RecyclerView

    private lateinit var mAdapterQuickAccess:RVQuickAccessAdapter
    private lateinit var mAdapterTrendingMedicalFields:RVTrendingMedicalFieldsAdapter
    private lateinit var mAdapterRecentConversations:RVRecentConversationsAdapter
    private lateinit var mAdapterConsultations:RVConsultationsAdapter

    private lateinit var mType: AccountType
    private lateinit var mParentActivity: MainActivity
    private lateinit var mViewModel: HomeFragmentViewModel
    //endregion
    companion object {
        @JvmStatic
        fun newInstance(): HomeFragment {
           return HomeFragment()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mParentActivity = context as MainActivity
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        getAccountType()
    }

    private fun getAccountType(){
        if(UserDetailsUtils.user!!.accountType == "PATIENT") mType = AccountType.PATIENT
        else mType = AccountType.DOCTOR
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initData()
        initViews2()
    }

    private fun initData(){
        mViewModel = ViewModelProvider(this).get(HomeFragmentViewModel::class.java)
        mViewModel.observeUserProfilePic().observe(viewLifecycleOwner){
            ivProfile.displayImage(it)
        }
    }

    private fun initViews2(){
        val unreadMessageCount:Int = PreferenceManager.get(KEY_UNREAD_MESSAGE_COUNT, 1)
        if(unreadMessageCount > 0){
            layout_unread_message_count.visibility = View.VISIBLE
            tvUnreadMessageCount.text = unreadMessageCount.toString()
        }else{
            layout_unread_message_count.visibility = View.GONE
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        initViews(view)
        return view
    }

    private fun initViews(view:View){
       ivToggle = view.findViewById(R.id.ivToggle_collapsing_toolbar)
       ivNotification = view.findViewById(R.id.ivNotifications_collapsing_toolbar)
       ivProfile = view.findViewById(R.id.ivProfile_collapsing_toolbar)
       tvUserName = view.findViewById(R.id.tvUsername_collapsing_toolbar)

        layout_findDoctors = view.findViewById(R.id.find_doctors)
        ivProfile_find_doctors = view.findViewById(R.id.ivProfile_find_doctors)
        ivCancel_layout_findDoctors = view.findViewById(R.id.ivCancel_FindDoctors)

        tvQuickAccess = view.findViewById(R.id.tvQuickAccess)
        tvTrendingMedicalFields = view.findViewById(R.id.tvTrendingFields_frag_home_patient)
        tvRecentConversations = view.findViewById(R.id.tvRecentConversations_frag_home_patient)
        tvConsultations = view.findViewById(R.id.tvConsultations_frag_home_patient)

        rvQuickAccess = view.findViewById(R.id.rvQuickAccess_frag_home_patient)
        rvTrendingMedicalFields = view.findViewById(R.id.rvTrendingFields_frag_home_patient)
        rvRecentConversations = view.findViewById(R.id.rvRecentConversation_frag_home_patient)
        rvConsultations = view.findViewById(R.id.rvConsultations)

        layout_unread_message_count = view.findViewById(R.id.layout_unread_message_count)
        tvUnreadMessageCount = view.findViewById(R.id.tvUnreadMessageCount)

        ivToggle.setOnClickListener { mParentActivity.apply { this.toggleDrawerState() } }

        ivProfile.displayImage(UserDetailsUtils.user!!.imageUri)

        mAdapterTrendingMedicalFields = RVTrendingMedicalFieldsAdapter()
        rvTrendingMedicalFields.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.HORIZONTAL,false)
        rvTrendingMedicalFields.addItemDecoration(HorizontalSpacingItemDecorator())
        rvTrendingMedicalFields.adapter = mAdapterTrendingMedicalFields

        mAdapterRecentConversations = RVRecentConversationsAdapter()
        rvRecentConversations.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        rvRecentConversations.addItemDecoration(DividerItemDecoration(requireContext(),LinearLayoutManager.VERTICAL))
        rvRecentConversations.adapter = mAdapterRecentConversations

        mAdapterConsultations = RVConsultationsAdapter()
        rvConsultations.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        rvConsultations.addItemDecoration(DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL))
        rvConsultations.adapter = mAdapterConsultations

        rvConsultations.visibility = View.GONE
        rvRecentConversations.visibility = View.GONE
        if(mType == AccountType.PATIENT)
            initViewsPatient()
        else
            initViewsDoctor()
    }
    private fun initViewsPatient(){
        layout_findDoctors.visibility = View.VISIBLE
        ivProfile_find_doctors.displayImage(UserDetailsUtils.user!!.imageUri)
        ivCancel_layout_findDoctors.setOnClickListener { layout_findDoctors.visibility = View.GONE }

        layout_findDoctors.setOnClickListener {
            mParentActivity.updateActiveItem(FragmentWrapper.FIND_DOCTORS)
            NavigationManager.inflateFragment(ActivityWrapper.MAIN, FragmentWrapper.FIND_DOCTORS, true)
        }

        val name: String = UserDetailsUtils.user!!.firstName
            .substring(0, 1)
            .uppercase(Locale.getDefault())
            .plus(
                UserDetailsUtils.user!!.firstName
                    .substring(1, UserDetailsUtils.user!!.firstName.length))

        tvUserName.text = name

        mAdapterQuickAccess = RVQuickAccessAdapter(AccountType.PATIENT)
        rvQuickAccess.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rvQuickAccess.addItemDecoration(HorizontalSpacingItemDecorator())
        rvQuickAccess.adapter = mAdapterQuickAccess

    }
    private fun initViewsDoctor(){
        layout_findDoctors.visibility  = View.GONE

        val name:String = "Dr. "+ UserDetailsUtils.user!!.firstName
            .substring(0,1)
            .uppercase(Locale.getDefault())
            .plus(
                UserDetailsUtils.user!!.firstName
                    .substring(1, UserDetailsUtils.user!!.firstName.length))
        tvUserName.text = name

        mAdapterQuickAccess = RVQuickAccessAdapter(AccountType.DOCTOR)
        rvQuickAccess.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rvQuickAccess.addItemDecoration(HorizontalSpacingItemDecorator())
        rvQuickAccess.adapter = mAdapterQuickAccess
    }


}