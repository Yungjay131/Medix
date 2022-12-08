package app.slyworks.auth_feature.registration

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.slyworks.auth_feature.databinding.FragmentRegistrationOtp1Binding
import app.slyworks.utils_lib.utils.displayMessage

class RegistrationOTP1Fragment : Fragment() {
    //region Vars
    private lateinit var binding: FragmentRegistrationOtp1Binding
    private lateinit var viewModel: RegistrationActivityViewModel
    //endregion

    companion object {
        @JvmStatic
        fun newInstance(): RegistrationOTP1Fragment = RegistrationOTP1Fragment()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        viewModel = (context as RegistrationActivity).viewModel
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentRegistrationOtp1Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initData()
        initViews()
    }

    private fun initData(){
        viewModel.progressLiveData.observe(viewLifecycleOwner){}
        viewModel.messageLiveData.observe(viewLifecycleOwner){ displayMessage(it, binding.root) }
        viewModel.beginOTPVerificationLiveData.observe(viewLifecycleOwner){_ ->
            (requireActivity() as RegistrationActivity).navigator
                .hideCurrent()
                .show(RegistrationOTP2Fragment.newInstance())
                .navigate()
        }
    }

    private fun initViews(){}
}