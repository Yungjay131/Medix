package app.slyworks.auth_feature.verification

import android.os.Bundle
import androidx.core.view.isVisible
import app.slyworks.auth_feature.IRegViewModel
import app.slyworks.auth_feature.databinding.ActivityVerificationBinding
import app.slyworks.auth_feature._di.AuthFeatureComponent
import app.slyworks.auth_feature.registration.RegistrationActivityViewModel
import app.slyworks.base_feature.BaseActivity
import app.slyworks.base_feature.MOnBackPressedCallback
import dev.joshuasylvanus.navigator.Navigator
import dev.joshuasylvanus.navigator.interfaces.FragmentContinuationStateful


import javax.inject.Inject

class VerificationActivity : IRegViewModel, BaseActivity() {
    //region Vars
    private lateinit var binding: ActivityVerificationBinding

    override lateinit var navigator: FragmentContinuationStateful

    @Inject
    override lateinit var viewModel: RegistrationActivityViewModel
    //endregion

    override fun isValid(): Boolean  = false

    override fun onDestroy() {
        super.onDestroy()

        navigator.onDestroy()
    }

    override fun onResume() {
        super.onResume()

        viewModel.subscribeToNetwork().observe(this) {
            binding.networkStatusView.isVisible = !it
        }
    }

    override fun onStop() {
        super.onStop()

        viewModel.unsubscribeToNetwork()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        initDI()

        super.onCreate(savedInstanceState)
        binding = ActivityVerificationBinding.inflate(layoutInflater)
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

        navigator = Navigator.transactionWithStateFrom(supportFragmentManager)
    }

    private fun initViews(){
        VerificationIntroDialog().show(supportFragmentManager, "")

        binding.ivBacker.setOnClickListener { this.onBackPressedDispatcher.onBackPressed() }

        navigator
            .into(binding.rootView.id)
            .show(VerificationGeneral0Fragment.newInstance())
            .navigate()
    }

    override fun toggleProgressView(status:Boolean):Unit{
        binding.progress.isVisible = status
    }
}