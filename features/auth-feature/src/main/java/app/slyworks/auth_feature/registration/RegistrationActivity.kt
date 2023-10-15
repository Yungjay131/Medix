package app.slyworks.auth_feature.registration

import android.os.Bundle
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import app.slyworks.auth_feature.databinding.ActivityRegistrationBinding
import app.slyworks.auth_feature._di.AuthFeatureComponent
import app.slyworks.base_feature.BaseActivity
import app.slyworks.base_feature.MOnBackPressedCallback
import app.slyworks.utils_lib.*
import dev.joshuasylvanus.navigator.Navigator
import dev.joshuasylvanus.navigator.Navigator.Companion.getExtra
import dev.joshuasylvanus.navigator.interfaces.FragmentContinuationStateful


import javax.inject.Inject


class RegistrationActivity : BaseActivity() {
    //region Vars
    private val fragmentMap:Map<String, () -> Fragment > = mapOf(
        FRAGMENT_REG_0 to RegistrationGeneral0Fragment::newInstance,
        FRAGMENT_REG_1 to RegistrationGeneral1Fragment::newInstance,
        FRAGMENT_REG_2 to RegistrationGeneral2Fragment::newInstance,
        FRAGMENT_REG_PATIENT to RegistrationPatientFragment::newInstance,
        FRAGMENT_REG_DOCTOR to RegistrationDoctorFragment::newInstance,
        FRAGMENT_REG_OTP to RegistrationOTP1Fragment::newInstance,
        FRAGMENT_REG_VERIFICATION_0 to RegistrationVerification0Fragment::newInstance
    )

    lateinit var navigator: FragmentContinuationStateful

    private lateinit var binding: ActivityRegistrationBinding

    @Inject
    override lateinit var viewModel: RegistrationActivityViewModel
    //endregion

    override fun cancelOngoingOperation() {
        TODO("Not yet implemented")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        initDI()

        super.onCreate(savedInstanceState)

        binding = ActivityRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initData()
        initViews()
    }

    private fun initDI(){
       AuthFeatureComponent.getInitialBuilder()
           .build()
           .inject(this)
    }

    private fun initData(){
        this.onBackPressedDispatcher
            .addCallback(this,
                object : MOnBackPressedCallback(this){
                    override fun handleOnBackPressed() {
                        if(!navigator.popBackStack())
                            finish()
                    }
                })
    }

    private fun initViews(){
        binding.appbar.ivBacker
            .setOnClickListener {
                this.onBackPressedDispatcher.onBackPressed()
            }

        val fragKey:String = intent.getExtra<String>(KEY_FRAGMENT, FRAGMENT_REG_0)!!

        navigator = Navigator.transactionWithStateFrom(supportFragmentManager)
        navigator.into(binding.rootView.id)
           .show(fragmentMap[fragKey]!!())
           .navigate()
    }

}