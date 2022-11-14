package com.slyworks.medix.ui.activities.requests_activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.slyworks.medix.R
import com.slyworks.medix.appComponent
import com.slyworks.medix.databinding.ActivityRequestsBinding
import com.slyworks.medix.ui.activities.BaseActivity
import com.slyworks.medix.utils.MOnBackPressedCallback
import javax.inject.Inject

class RequestsActivity : BaseActivity() {
    //region Vars
    private lateinit var binding:ActivityRequestsBinding

    /*@Inject
    lateinit var viewModel:RequestsActivityViewModel*/
    //endregion

    override fun onCreate(savedInstanceState: Bundle?) {
        initDI()

        super.onCreate(savedInstanceState)

        binding = ActivityRequestsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initData()
        initViews()
    }

    private fun initDI(){
       /* application.appComponent
            .activityComponentBuilder()
            .setActivity(this)
            .build()
            .inject(this)*/
    }

    private fun initData(){
        this.onBackPressedDispatcher
            .addCallback(this, MOnBackPressedCallback(this))
    }

    private fun initViews(){}
}