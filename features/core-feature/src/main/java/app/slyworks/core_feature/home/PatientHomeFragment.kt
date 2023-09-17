package app.slyworks.core_feature.home

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import app.slyworks.base_feature.custom_views.HorizontalSpacingItemDecorator
import app.slyworks.utils_lib.FBU_FIRST_NAME
import app.slyworks.utils_lib.FBU_IMAGE_URI
import app.slyworks.core_feature.main.HomeActivity
import app.slyworks.core_feature.RvHealthAreasAdapter
import app.slyworks.core_feature.databinding.FragmentHomePatientBinding
import app.slyworks.core_feature.main.activityComponent
import app.slyworks.utils_lib.utils.displayImage
import java.util.*
import javax.inject.Inject

class PatientHomeFragment : Fragment() {
    private lateinit var adapterHealthAreas: RvHealthAreasAdapter
    
    private lateinit var binding:FragmentHomePatientBinding

    @Inject
    lateinit var viewModel: HomeFragmentViewModel

    companion object {
        @JvmStatic
        fun getInstance(): PatientHomeFragment = PatientHomeFragment()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

       context.activityComponent
           .fragmentComponentBuilder()
           .build()
           .inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentHomePatientBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initData()
        initViews(view)
    }

    private fun initData(){
        viewModel.imageUriLiveData.observe(viewLifecycleOwner){
            binding.lCollapsingToolbar.ivProfileCollapsingToolbar.displayImage(it)
        }

        viewModel.observeUserProfilePic()

        (requireActivity() as HomeActivity).verifyNotificationPermission()
    }

    private fun initViews(view:View){
        binding.lCollapsingToolbar.tvUnreadMessageCount.setText(1.toString())

        binding.lCollapsingToolbar.ivProfileCollapsingToolbar.displayImage(viewModel.getUserProperty<String>(FBU_IMAGE_URI))
        binding.layoutScheduleCard.ivProfileLayoutScheduleFragHome.displayImage(viewModel.getUserProperty<String>(FBU_IMAGE_URI))

        binding.lCollapsingToolbar.ivToggleCollapsingToolbar.setOnClickListener{
            (requireActivity() as HomeActivity).toggleDrawerState()
        }

        val _name:String = viewModel.getUserProperty<String>(FBU_FIRST_NAME)
        val name: String =
            _name
            .substring(0, 1)
            .uppercase(Locale.getDefault())
            .plus(_name.substring(1, _name.length))
        binding.lCollapsingToolbar.tvUsernameCollapsingToolbar.text = name

        adapterHealthAreas = RvHealthAreasAdapter()
        binding.rvHealthAreasFragHome.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL,false)
        binding.rvHealthAreasFragHome.addItemDecoration(HorizontalSpacingItemDecorator())
        binding.rvHealthAreasFragHome.adapter = adapterHealthAreas
    }
}