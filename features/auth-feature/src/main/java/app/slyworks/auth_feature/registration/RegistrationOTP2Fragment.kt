package app.slyworks.auth_feature.registration

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import app.slyworks.auth_feature.databinding.FragmentRegistrationOtp2Binding
import app.slyworks.utils_lib.LOGIN_ACTIVITY_INTENT_FILTER
import app.slyworks.utils_lib.utils.closeKeyboard

import app.slyworks.utils_lib.utils.plusAssign
import app.slyworks.utils_lib.utils.displayMessage
import com.jakewharton.rxbinding4.widget.textChanges
import dev.joshuasylvanus.navigator.Navigator
import dev.joshuasylvanus.navigator.interfaces.FragmentContinuationStateful
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit

class RegistrationOTP2Fragment : Fragment() {
    //region Vars
    private val disposables = CompositeDisposable()

    private lateinit var navigator:FragmentContinuationStateful
    private lateinit var viewModel: RegistrationActivityViewModel

    private lateinit var binding: FragmentRegistrationOtp2Binding
    //endregion

    companion object {
        @JvmStatic
        fun newInstance(): RegistrationOTP2Fragment = RegistrationOTP2Fragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentRegistrationOtp2Binding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initData()
        initViews()
    }

    @SuppressLint("CheckResult")
    private fun initData(){
        navigator = (requireActivity() as RegistrationActivity).navigator
        viewModel = (requireActivity() as RegistrationActivity).viewModel

        viewModel.uiStateLD.observe(viewLifecycleOwner){
            when(it){
                is RegistrationUIState.OTPVerificationResent -> {
                    displayMessage("OTP resent", requireContext())

                    binding.etOTP1.setText("")
                    binding.etOTP2.setText("")
                    binding.etOTP3.setText("")
                    binding.etOTP4.setText("")
                    binding.etOTP5.setText("")
                    binding.etOTP6.setText("")
                    viewModel.initOTPTimeoutCountdown()
                }

                is RegistrationUIState.OTPVerificationSuccess ->{
                    displayMessage("verification success.\nLogin to continue", requireContext())

                    /* delay for 2 seconds then navigate to LoginActivity */
                    Completable.timer(2_000, TimeUnit.MILLISECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                              Navigator.intentFor(requireContext(), LOGIN_ACTIVITY_INTENT_FILTER)
                                  .newAndClearTask()
                                  .navigate()
                        },
                        {
                            Timber.e(it)

                            Navigator.intentFor(requireContext(), LOGIN_ACTIVITY_INTENT_FILTER)
                                .newAndClearTask()
                                .navigate()
                        })
                }

                is RegistrationUIState.OTPVerificationFailure ->
                    displayMessage(it.error, requireContext())

                is RegistrationUIState.Message ->
                    displayMessage(it.message, requireContext())
            }
        }

        viewModel.uiOTPStateLD.observe(viewLifecycleOwner){
            when(it){
                is RegistrationUIState.OTPCountDown ->
                    binding.tvCounter.setText(it.toString())

                is RegistrationUIState.OTPCountDownFinished -> {}
            }
        }

        viewModel.initOTPTimeoutCountdown()
    }

    private fun initViews(){
        binding.etOTP2.setOnKeyListener { _:View, keyCode:Int, _ ->
            if(keyCode == KeyEvent.KEYCODE_DEL){
                binding.etOTP2.setText("")
                binding.etOTP1.requestFocus()
                return@setOnKeyListener true
            }

            return@setOnKeyListener false
        }

        binding.etOTP3.setOnKeyListener { _:View, keyCode:Int, _ ->
            if(keyCode == KeyEvent.KEYCODE_DEL){
                binding.etOTP3.setText("")
                binding.etOTP2.requestFocus()
                return@setOnKeyListener true
            }

            return@setOnKeyListener false
        }

        binding.etOTP4.setOnKeyListener { _:View, keyCode:Int, _ ->
            if(keyCode == KeyEvent.KEYCODE_DEL){
                binding.etOTP4.setText("")
                binding.etOTP3.requestFocus()
                return@setOnKeyListener true
            }

            return@setOnKeyListener false
        }

        binding.etOTP5.setOnKeyListener { _:View, keyCode:Int, _ ->
            if(keyCode == KeyEvent.KEYCODE_DEL){
                binding.etOTP5.setText("")
                binding.etOTP4.requestFocus()
                return@setOnKeyListener true
            }

            return@setOnKeyListener false
        }

        binding.etOTP6.setOnKeyListener { _:View, keyCode:Int, _ ->
            if(keyCode == KeyEvent.KEYCODE_DEL){
                binding.etOTP6.setText("")
                binding.etOTP5.requestFocus()
                return@setOnKeyListener true
            }

            return@setOnKeyListener false
        }


        disposables +=
        binding.etOTP1.textChanges()
            .subscribe {
               if(it.length == 1)
                   binding.etOTP2.requestFocus()
            }
        disposables +=
        binding.etOTP2.textChanges()
            .subscribe {
               if(it.length == 1)
                   binding.etOTP3.requestFocus()
            }

        disposables +=
        binding.etOTP3.textChanges()
            .subscribe {
               if(it.length == 1)
                   binding.etOTP4.requestFocus()
            }

        disposables +=
        binding.etOTP4.textChanges()
            .subscribe {
               if(it.length == 1)
                   binding.etOTP5.requestFocus()
            }

        disposables +=
        binding.etOTP5.textChanges()
            .subscribe {
               if(it.length == 1)
                   binding.etOTP6.requestFocus()
            }

        disposables +=
        Observable.combineLatest(binding.etOTP1.textChanges(),
                                 binding.etOTP2.textChanges(),
                                 binding.etOTP3.textChanges(),
                                 binding.etOTP4.textChanges(),
                                 binding.etOTP5.textChanges(),
                                 binding.etOTP6.textChanges(),
            { t1,t2,t3,t4,t5,t6 ->
                t1.length == 1 &&
                t2.length == 1 &&
                t3.length == 1 &&
                t4.length == 1 &&
                t5.length == 1 &&
                t6.length == 1
            })
            .subscribe(binding.btnNext::setEnabled)

        binding.etOTP6.setOnEditorActionListener(
            TextView.OnEditorActionListener { p0, p1, p2 ->
                if(p0!!.id == binding.etOTP6.id){
                    requireActivity().closeKeyboard()

                    val otp:String =
                        "${binding.etOTP1.text}${binding.etOTP2.text}" +
                                "${binding.etOTP3.text}${binding.etOTP4.text}"
                    viewModel.receiveSMSCodeForOTP(otp)

                    return@OnEditorActionListener true
                }

                return@OnEditorActionListener false
            })



        binding.btnNext.setOnClickListener {
            requireActivity().closeKeyboard()

            val otp:String =
            "${binding.etOTP1.text}${binding.etOTP2.text}" +
            "${binding.etOTP3.text}${binding.etOTP4.text}" +
            "${binding.etOTP5.text}${binding.etOTP6.text}"
            viewModel.receiveSMSCodeForOTP(otp)
        }

        binding.tvResendOtp.setOnClickListener{
            viewModel.resendOTP()
        }

    }
}