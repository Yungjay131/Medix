package app.slyworks.core_feature.chat

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.*
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.widget.ViewPager2
import app.slyworks.core_feature.main.HomeActivity
import app.slyworks.core_feature.R
import app.slyworks.core_feature.VPAdapter
import app.slyworks.core_feature.databinding.FragmentChatHostBinding
import app.slyworks.core_feature.main.activityComponent
import app.slyworks.utils_lib.utils.displayImage
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import de.hdodenhof.circleimageview.CircleImageView
import javax.inject.Inject


class ChatHostFragment : Fragment() {
    private lateinit var vpAdapter: VPAdapter

    private lateinit var binding:FragmentChatHostBinding

    @Inject
    lateinit var viewModel: ChatHostFragmentViewModel

    companion object {
        val tabTitles:MutableList<String> = mutableListOf("Chats", "Call History")

        @JvmStatic
        fun getInstance(): ChatHostFragment = ChatHostFragment()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        context.activityComponent
            .fragmentComponentBuilder()
            .build()
            .inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentChatHostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
    }

    private fun initViews(view:View){
        vpAdapter = VPAdapter(childFragmentManager, this.lifecycle)
        binding.vpHostFragChatHost.adapter = vpAdapter
        TabLayoutMediator(binding.lCollapsingToolbar.tabLayoutFragChatHost, binding.vpHostFragChatHost,
            object: TabLayoutMediator.TabConfigurationStrategy {
                override fun onConfigureTab(tab: TabLayout.Tab, position: Int) {
                    tab.setText(tabTitles[position])
                }
            }).attach()

        binding.lCollapsingToolbar.ivToggleCollapsingToolbar.setOnClickListener { (requireActivity() as HomeActivity).toggleDrawerState() }

        binding.lCollapsingToolbar.ivProfileCollapsingToolbar.displayImage(viewModel.getUserDetailsUser().imageUri)
    }

    fun inflateFragment(f:Fragment){
        val transaction = childFragmentManager.beginTransaction()
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)

        if(f.isAdded) transaction.show(f)
        else transaction.replace(R.id.fragment_container_chat_host, f, "${f::class.simpleName}")

        transaction.commit()

        binding.layoutVpFragChatHost.visibility = View.GONE
    }
}