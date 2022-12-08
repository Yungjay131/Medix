package app.slyworks.auth_feature.registration

import android.os.Bundle
import app.slyworks.auth_feature.databinding.ActivityRegistrationBinding
import app.slyworks.auth_feature.di.AuthFeatureComponent
import app.slyworks.base_feature.BaseActivity
import app.slyworks.base_feature.MOnBackPressedCallback
import app.slyworks.navigation_feature.Navigator
import app.slyworks.navigation_feature.interfaces.FragmentContinuationStateful
import app.slyworks.utils_lib.utils.setStatusBarVisibility
import javax.inject.Inject


class RegistrationActivity : BaseActivity() {
    //region Vars
    private lateinit var binding: ActivityRegistrationBinding

    lateinit var navigator: FragmentContinuationStateful

    @Inject
    lateinit var viewModel: RegistrationActivityViewModel
    //endregion

    override fun isValid(): Boolean = false

    override fun onDestroy() {
        super.onDestroy()

        navigator.onDestroy()
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
           .appCompatActivity(this)
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
       navigator
           .into(binding.rootView.id)
           .show(RegistrationGeneral0Fragment.newInstance())
           .navigate()
    }


}