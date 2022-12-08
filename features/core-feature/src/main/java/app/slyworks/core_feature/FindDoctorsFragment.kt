package app.slyworks.core_feature

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
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.slyworks.constants_lib.EVENT_GET_DOCTOR_USERS
import app.slyworks.constants_lib.EVENT_OPEN_VIEW_PROFILE_FRAGMENT
import app.slyworks.controller_lib.AppController
import app.slyworks.controller_lib.Subscription
import app.slyworks.controller_lib.clearAndRemove
import app.slyworks.core_feature.view_profile.ViewProfileFragment
import app.slyworks.data_lib.models.FBUserDetailsVModel
import app.slyworks.utils_lib.utils.addMultiple
import app.slyworks.utils_lib.utils.displayMessage
import com.airbnb.lottie.LottieAnimationView
import javax.inject.Inject

class FindDoctorsFragment : Fragment(), app.slyworks.controller_lib.Observer {
   //region Vars
    private lateinit var rvDoctors:RecyclerView
    private lateinit var progress:ProgressBar
    private lateinit var rootView:ConstraintLayout
    private lateinit var layout_intro:ConstraintLayout
    private lateinit var ivLayout_intro:ImageView
    private lateinit var lavLayout_intro:LottieAnimationView
    private lateinit var btnFindDoctors: Button

    private val subscriptionsList:MutableList<Subscription> = mutableListOf()

    private lateinit var adapter: RVFindDoctorsAdapter

    @Inject
    lateinit var viewModel: FindDoctorsFragmentViewModel

    //endregion

   companion object {
       @JvmStatic
       fun getInstance(): FindDoctorsFragment = FindDoctorsFragment()
   }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_find_doctors, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        initData()
    }

    private fun initData(){
        val subscription1 = AppController.subscribeTo(EVENT_GET_DOCTOR_USERS, this)
        val subscription2: Subscription = AppController.subscribeTo(EVENT_OPEN_VIEW_PROFILE_FRAGMENT, this)

        subscriptionsList.addMultiple(subscription1, subscription2)

        viewModel.doctorsListLiveData.observe(viewLifecycleOwner, Observer {
            adapter.addDoctors(it)
        })
    }

    private fun initViews(view:View){
        rootView = view.findViewById(R.id.rootView)
        rvDoctors = view.findViewById(R.id.rvFindDoctors_find_doctors)
        progress = view.findViewById(R.id.progress_layout)

        layout_intro = view.findViewById(R.id.layout_intro_frag_find_doctors)
        ivLayout_intro = view.findViewById(R.id.ivFindDoctors_layout_intro)
        lavLayout_intro = view.findViewById(R.id.lavFindDoctors_layout_intro)
        btnFindDoctors = view.findViewById(R.id.btnFindDoctors_frag_find_doctors)

        btnFindDoctors.setOnClickListener {
            if(!viewModel.getNetworkStatus()) {
                displayMessage("no network connection", rootView)
                return@setOnClickListener
            }

            toggleProgressVisibility(true)
            viewModel.getAllDoctors()
        }

        adapter = RVFindDoctorsAdapter()

        rvDoctors.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        rvDoctors.addItemDecoration(DividerItemDecoration(requireContext(),LinearLayoutManager.VERTICAL))
        rvDoctors.adapter = adapter
    }

    private fun toggleProgressVisibility(status:Boolean){ progress.isVisible = status }

    override fun <T> notify(event: String, data: T?) {
        when(event){
            EVENT_GET_DOCTOR_USERS ->{
                val result:Pair<Boolean, MutableList<FBUserDetailsVModel>?> = data as Pair<Boolean, MutableList<FBUserDetailsVModel>?>

                toggleProgressVisibility(false)
                if(result.first) {
                    adapter.addDoctors(result.second!!)
                    layout_intro.visibility = View.GONE
                    rootView.visibility = View.VISIBLE
                }else
                   displayMessage("retrieving users failed please try again", rootView)

            }
            EVENT_OPEN_VIEW_PROFILE_FRAGMENT ->{
                val entity: FBUserDetailsVModel = data as FBUserDetailsVModel

                (requireParentFragment() as ProfileHostFragment)
                    .inflateFragment2(ViewProfileFragment.newInstance(entity))
            }

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        subscriptionsList.forEach { it.clearAndRemove() }
    }
}