package app.slyworks.auth_feature.registration

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import app.slyworks.auth_feature.databinding.FragmentRegistrationGeneral0Binding
import app.slyworks.data_lib.model.models.AccountType
import app.slyworks.utils_lib.utils.displayMessage
import app.slyworks.utils_lib.utils.plusAssign
import dev.joshuasylvanus.navigator.interfaces.FragmentContinuationStateful
import io.reactivex.rxjava3.disposables.CompositeDisposable

class RegistrationGeneral0Fragment : Fragment() {
    //region Vars
    private var accountType: AccountType = AccountType.NOT_SET

    private val disposables:CompositeDisposable = CompositeDisposable()

    private lateinit var navigator: FragmentContinuationStateful
    private lateinit var viewModel: RegistrationActivityViewModel

    private lateinit var binding: FragmentRegistrationGeneral0Binding
    //endregion

    companion object {
        @JvmStatic
        fun newInstance(): RegistrationGeneral0Fragment = RegistrationGeneral0Fragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
       binding = FragmentRegistrationGeneral0Binding.inflate(layoutInflater, container, false)
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
    }

    private fun initViews(){
        // val animationLogo = AnimationUtils.loadAnimation(this, R.anim.registration_logo_anim)

      /*  binding.ivLogo.alpha = 0F
        val logoAnimator: ValueAnimator = ValueAnimator.ofFloat(0f,1f)
        logoAnimator.duration = 1_500
        logoAnimator.interpolator = LinearInterpolator()
        logoAnimator.addUpdateListener {
            val animatorValue:Float = it.animatedValue as Float

            binding.ivLogo.alpha = animatorValue
            binding.ivLogo.scaleX = animatorValue
            binding.ivLogo.scaleY = animatorValue
        }
        logoAnimator.start()
        */

        val animationText: Animation =
            AnimationUtils.loadAnimation(
                requireContext(),
                app.slyworks.base_feature.R.anim.regisrtration_text_anim)
        binding.tvText.startAnimation(animationText)

        disposables +=
        binding.sivPatient.observeChanges()
            .subscribe {
                if(it){
                   binding.sivDoctor.setCurrentStatus(false)

                   accountType = AccountType.PATIENT

                   viewModel.setAccountType(AccountType.PATIENT)
                }
            }

        disposables +=
        binding.sivDoctor.observeChanges()
            .subscribe {
                if(it){
                    binding.sivPatient.setCurrentStatus(false)

                    accountType = AccountType.DOCTOR

                    viewModel.setAccountType(AccountType.DOCTOR)
                }
            }

        binding.btnNext.setOnClickListener {
            if(!check())
                return@setOnClickListener

            navigator.show(RegistrationGeneral1Fragment.newInstance())
                .navigate()
        }
    }

    private fun check():Boolean {
        var status:Boolean = true
        if(accountType == AccountType.NOT_SET){
            displayMessage("please select an account type to continue", binding.root)
            status = false
        }

        return status
    }
}