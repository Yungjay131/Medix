package app.slyworks.auth_feature.registration

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.slyworks.auth_feature.databinding.FragmentRegistrationVerification0Binding
import app.slyworks.data_lib.model.models.VerificationDetails
import app.slyworks.utils_lib.LOGIN_ACTIVITY_INTENT_FILTER
import app.slyworks.utils_lib.utils.displayMessage
import app.slyworks.utils_lib.utils.plusAssign
import dev.joshuasylvanus.navigator.Navigator
import dev.joshuasylvanus.navigator.interfaces.FragmentContinuationStateful
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import timber.log.Timber
import java.util.concurrent.TimeUnit

class RegistrationVerification0Fragment : Fragment() {
    //region Vars
    private lateinit var navigator: FragmentContinuationStateful
    private lateinit var viewModel:RegistrationActivityViewModel

    private val disposables: CompositeDisposable = CompositeDisposable()

    private lateinit var binding: FragmentRegistrationVerification0Binding
    //endregion

    companion object {
        @JvmStatic
        fun newInstance(): RegistrationVerification0Fragment =
            RegistrationVerification0Fragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentRegistrationVerification0Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initData()
        initViews()
    }

    private fun initData() {
        navigator = (requireActivity() as  RegistrationActivity).navigator
        viewModel = (requireActivity() as RegistrationActivity).viewModel

        viewModel.uiStateLD.observe(viewLifecycleOwner){
            when(it){
                is RegistrationUIState.EmailVerificationSuccess ->{
                    displayMessage("verification email sent to ${it.email}",requireContext())

                    Completable.timer(1_000, TimeUnit.MILLISECONDS)
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

                is RegistrationUIState.EmailVerificationFailure ->
                    displayMessage(it.error+"\nPlease try again", requireContext())

                is RegistrationUIState.Message ->
                    displayMessage(it.message, binding.root)

                else -> {}
            }
        }
    }

    private fun initViews() {
        var selected: VerificationDetails = VerificationDetails.EMAIL

        disposables +=
        binding.sivEmail.observeChanges()
            .subscribe {
                if (it) {
                    binding.sivOtp.setCurrentStatus(false)
                    selected = VerificationDetails.EMAIL
                }
            }

        disposables +=
        binding.sivOtp.observeChanges()
            .subscribe {
                if (it) {
                    binding.sivEmail.setCurrentStatus(false)
                    selected = VerificationDetails.OTP
                }
            }

        disposables +=
        Observable.combineLatest(binding.sivEmail.observeChanges(),
            binding.sivOtp.observeChanges(),
            { isEmail: Boolean, isOTP: Boolean ->
                return@combineLatest isEmail || isOTP
            })
            .subscribeOn(AndroidSchedulers.mainThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(binding.btnProceed::setEnabled)

        binding.btnProceed.setOnClickListener {
            when(selected){
                VerificationDetails.EMAIL -> viewModel.verifyByEmail()
                VerificationDetails.OTP -> {
                    navigator.show(RegistrationOTP1Fragment.newInstance())
                        .navigate()
                }
            }
        }
    }
}

