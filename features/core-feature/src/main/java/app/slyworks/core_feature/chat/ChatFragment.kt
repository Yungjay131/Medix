package app.slyworks.core_feature.chat

import android.content.Context
import android.os.Bundle
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
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.slyworks.constants_lib.*
import app.slyworks.controller_lib.AppController
import app.slyworks.controller_lib.Observer
import app.slyworks.controller_lib.Subscription
import app.slyworks.core_feature.main.MainActivity
import app.slyworks.core_feature.R
import app.slyworks.core_feature.databinding.FragmentChatBinding
import app.slyworks.data_lib.vmodels.FBUserDetailsVModel
import app.slyworks.data_lib.vmodels.MessageVModel
import app.slyworks.data_lib.vmodels.PersonVModel

import app.slyworks.utils_lib.utils.addMultiple
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dev.joshuasylvanus.navigator.Navigator

class ChatFragment : Fragment(), Observer {
    //region Vars
    private lateinit var layout_chat_empty:ConstraintLayout
    private lateinit var rvChats:RecyclerView
    private lateinit var fabStartChat: FloatingActionButton
    private lateinit var rootView:CoordinatorLayout
    private lateinit var progress:ProgressBar
    private lateinit var layout_error:ConstraintLayout
    private lateinit var tvRetry:TextView
    private lateinit var btnRetry:Button
    private lateinit var progress_retry:ProgressBar

    private lateinit var personToMessagesMap:Map<PersonVModel, MutableList<MessageVModel>>
    private lateinit var adapter2: RVChatAdapter2

    private lateinit var binding: FragmentChatBinding

    private lateinit var viewModel: ChatHostFragmentViewModel

    private var subscriptionsList:MutableList<Subscription> = mutableListOf()

    //endregion
    companion object {
        @JvmStatic
        fun newInstance(): ChatFragment {
            return ChatFragment()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        viewModel = (parentFragment as ChatHostFragment).viewModel
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initData()
        initViews()
    }

    private fun initData(){
        val subscription = AppController.subscribeTo(EVENT_OPEN_MESSAGE_ACTIVITY, this)
        val subscription2 = AppController.subscribeTo(EVENT_OPEN_MESSAGE_ACTIVITY_2, this)

        subscriptionsList.addMultiple(subscription, subscription2)

        viewModel.successStateLiveData.observe(viewLifecycleOwner){
            personToMessagesMap = it
            adapter2.submitList(it.keys.toList())
        }

        viewModel.intoStateLiveData.observe(viewLifecycleOwner){
            toggleLayoutIntroStatus(it)
        }

        viewModel.errorStateLiveData.observe(viewLifecycleOwner){
            if(it){
              val text = viewModel.errorDataLiveData.value!!
              toggleLayoutErrorStatus(it, text)
              return@observe
            }

            toggleLayoutErrorStatus(it)
        }

        viewModel.progressStateLiveData.observe(viewLifecycleOwner){
            binding.progress.progressLayout.isVisible = it
            if(!it && progress_retry.isVisible)
              progress_retry.visibility = View.GONE
        }

        getData(2)
    }

    private fun getData(from:Int){
        when(from){
            1 -> progress_retry.isVisible = true
            2 -> binding.progress.progressLayout.isVisible = true
        }

        viewModel.getChats()
    }

    private fun initViews(){
        binding.fabStatChatFragChat.setOnClickListener {
            val f:String
            if(viewModel.getUserAccountType() == PATIENT)
                f = FRAGMENT_PROFILE_HOST
            else
                f = FRAGMENT_DOCTOR_HOME

            (requireActivity() as MainActivity)
                .inflateFragment(f)
        }

        adapter2 = RVChatAdapter2()
        binding.contentChat.rvChats.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.contentChat.rvChats.addItemDecoration(DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL))
        binding.contentChat.rvChats.adapter = adapter2

        binding.contentChat.btnRetry.setOnClickListener { getData(1) }
    }

    private fun toggleLayoutErrorStatus(status:Boolean,
                                        text:String = "oops, something went wrong on our end, please try again"){
        binding.contentChat.layoutError.isVisible = status
        binding.contentChat.tvRetry.text = text
    }

    private fun toggleLayoutIntroStatus(status:Boolean){
        binding.contentChat.layoutNoMessagesContentChat.isVisible = status
    }

    override fun <T> notify(event: String, data: T?) {
        when(event){
            EVENT_OPEN_MESSAGE_ACTIVITY -> {}
            EVENT_OPEN_MESSAGE_ACTIVITY_2 -> {
                /*TODO:this is an incomplete FBUserDetails object*/
                val result: PersonVModel = data as PersonVModel

                val entity: FBUserDetailsVModel = FBUserDetailsVModel(
                    accountType = result.userAccountType,
                    firebaseUID = result.firebaseUID,
                    fullName = result.fullName,
                    imageUri = result.imageUri)

                Navigator.intentFor(requireActivity(), MESSAGE_ACTIVITY_INTENT_FILTER)
                    .addExtra(EXTRA_USER_PROFILE_FBU, entity)
                    .navigate()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        subscriptionsList.forEach { it.clearAndRemove() }
    }
}