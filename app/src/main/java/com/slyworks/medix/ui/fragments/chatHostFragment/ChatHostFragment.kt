package com.slyworks.medix.ui.fragments.chatHostFragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.*
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.slyworks.constants.KEY_UNREAD_MESSAGE_COUNT
import com.slyworks.medix.ui.activities.mainActivity.MainActivity
import com.slyworks.medix.R
import com.slyworks.medix.UserDetailsUtils
import com.slyworks.medix.utils.PreferenceManager
import com.slyworks.medix.utils.ViewUtils.displayImage
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class ChatHostFragment : Fragment() {
    //region Vars
    private lateinit var layout_vp:CoordinatorLayout
    private lateinit var ivToggle:CircleImageView
    private lateinit var ivProfile:CircleImageView
    private lateinit var vpChatHostFragment: ViewPager2
    private lateinit var tabLayoutChatHostFragment:TabLayout

    private lateinit var containerSecondary:FragmentContainerView

    private lateinit var vpAdapter: VPAdapter
    //endregion
    companion object {
        //region Vars
        val mTabTitles:MutableList<String> = mutableListOf("Chats", "Call History")
        @JvmStatic
        fun getInstance(): ChatHostFragment {
            return ChatHostFragment()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        CoroutineScope(Dispatchers.IO).launch {
            PreferenceManager.set(KEY_UNREAD_MESSAGE_COUNT, 0)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view:View = inflater.inflate(R.layout.fragment_chat_host, container, false)
        initViews_1(view)
        initViews_2(view)
        return view
    }

    private fun initViews_1(view:View){
        layout_vp = view.findViewById(R.id.layout_vp_frag_chat_host)
        ivToggle = view.findViewById(R.id.ivToggle_collapsing_toolbar)
        ivProfile = view.findViewById(R.id.ivProfile_collapsing_toolbar)
        vpChatHostFragment = view.findViewById(R.id.vpHost_frag_chat_host)
        tabLayoutChatHostFragment = view.findViewById(R.id.tabLayout_frag_chat_host)

        containerSecondary = view.findViewById(R.id.fragment_container_chat_host)

        ivToggle.setOnClickListener {
            (requireActivity() as MainActivity).toggleDrawerState()
        }

        ivProfile.displayImage(UserDetailsUtils.user!!.imageUri)
    }
    private fun initViews_2(view:View){
        val lifecycle:Lifecycle = this.lifecycle
        vpAdapter = VPAdapter(childFragmentManager, lifecycle)
        vpChatHostFragment.adapter = vpAdapter
        TabLayoutMediator(tabLayoutChatHostFragment, vpChatHostFragment,
            object: TabLayoutMediator.TabConfigurationStrategy {
                override fun onConfigureTab(tab: TabLayout.Tab, position: Int) {
                    tab.text = mTabTitles[position]
                }
            }).attach()
    }

    fun inflateFragment(f:Fragment){
        val transaction = childFragmentManager.beginTransaction()
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)

        if(f.isAdded) transaction.show(f)
        else transaction.replace(R.id.fragment_container_chat_host, f, "${f::class.simpleName}")

        transaction.commit()

        layout_vp.visibility = View.GONE
    }
}