package app.slyworks.auth_feature.registration

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import app.slyworks.auth_feature.databinding.FragmentRegistrationOtp1Binding
import app.slyworks.utils_lib.utils.closeKeyboard
import app.slyworks.utils_lib.utils.displayMessage
import app.slyworks.utils_lib.utils.plusAssign
import app.slyworks.utils_lib.utils.properText
import com.jakewharton.rxbinding4.widget.textChanges
import dev.joshuasylvanus.navigator.interfaces.FragmentContinuationStateful
import io.reactivex.rxjava3.disposables.CompositeDisposable

class RegistrationOTP1Fragment : Fragment() {
    //region Vars
    private val disposables:CompositeDisposable = CompositeDisposable()

    private lateinit var navigator:FragmentContinuationStateful
    private lateinit var viewModel: RegistrationActivityViewModel

    private lateinit var binding: FragmentRegistrationOtp1Binding
    //endregion

    companion object {
        @JvmStatic
        fun newInstance(): RegistrationOTP1Fragment = RegistrationOTP1Fragment()
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
        navigator = (requireActivity() as RegistrationActivity).navigator
        viewModel = (requireActivity() as RegistrationActivity).viewModel

        viewModel.uiStateLD.observe(viewLifecycleOwner){
          when(it){
              is RegistrationUIState.OTPVerificationStarted ->
                  navigator.show(RegistrationOTP2Fragment.newInstance())
                      .navigate()

              is RegistrationUIState.Message ->
                  displayMessage(it.message, requireContext())
          }

        }

    }

    private fun initViews(){
        disposables +=
        binding.etPhoneNumber.textChanges()
            .map{ it.length == 14 }
            .subscribe(binding.btnNext::setEnabled)

        binding.etPhoneNumber.setOnEditorActionListener { textView, i, keyEvent ->
            requireActivity().closeKeyboard()

            val phoneNumber: String = binding.etPhoneNumber.properText
            viewModel.verifyViaOTP(phoneNumber, requireActivity())

            return@setOnEditorActionListener true
        }

        binding.btnNext.setOnClickListener{
            requireActivity().closeKeyboard()

            val phoneNumber:String = binding.etPhoneNumber.properText
            viewModel.verifyViaOTP(phoneNumber, requireActivity())

        }
    }
}