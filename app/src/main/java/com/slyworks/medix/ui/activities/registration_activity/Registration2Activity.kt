package com.slyworks.medix.ui.activities.registration_activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import app.slyworks.navigator.Navigator
import app.slyworks.navigator.interfaces.FragmentContinuationStateful
import com.slyworks.medix.databinding.ActivityRegistration2Binding
import javax.inject.Inject

class Registration2Activity : AppCompatActivity() {
    //region Vars
    private lateinit var binding:ActivityRegistration2Binding

    lateinit var navigator:FragmentContinuationStateful

    @Inject
    lateinit var registrationViewModel:RegistrationActivityViewModel
    //endregion

    companion object{
        @JvmStatic
        private var isDaggerInitialized:Boolean = false
    }

    override fun onDestroy() {
        super.onDestroy()

        if(isFinishing())
            isDaggerInitialized = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        if(!isDaggerInitialized){
            isDaggerInitialized = true
        }

        super.onCreate(savedInstanceState)
         binding = ActivityRegistration2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        initData()
        initViews()
    }

    private fun initData(){
        navigator = Navigator.transactionWithStateFrom(supportFragmentManager)
    }
    private fun initViews(){
        navigator
            .into(binding.fragmentContainer.id)
            .show(RegistrationGeneral1Fragment.newInstance())
            .navigate()
    }
}