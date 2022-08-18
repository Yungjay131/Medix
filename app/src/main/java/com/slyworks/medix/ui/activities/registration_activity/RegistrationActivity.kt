package com.slyworks.medix.ui.activities.registration_activity

import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.slyworks.constants.GENERAL
import com.slyworks.medix.R
import com.slyworks.medix.appComponent
import com.slyworks.medix.ui.activities.BaseActivity
import com.slyworks.medix.ui.custom_views.NetworkStatusView
import com.slyworks.medix.utils.MOnBackPressedCallback
import javax.inject.Inject


class RegistrationActivity : BaseActivity() {
    //region Vars
    private lateinit var btnPatient:Button
    private lateinit var btnDoctor:Button
    private lateinit var ivLogo:ImageView
    private lateinit var tvLogoText:TextView
    private lateinit var rootView:ConstraintLayout

    private var networkStatusView:NetworkStatusView? = null

    @Inject
    lateinit var mViewModel:RegistrationActivityViewModel
    //endregion

    override fun onStart() {
        super.onStart()

        mViewModel.subscribeToNetwork().observe(this) {
            if(networkStatusView == null)
                networkStatusView = NetworkStatusView.from(rootView, GENERAL)

            networkStatusView!!.setVisibilityStatus(it)
        }
    }

    override fun onStop() {
        super.onStop()

        mViewModel.unsubscribeToNetwork()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        initDI()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        initData()
        initViews()
    }

    private fun initDI(){
        application.appComponent
            .activityComponentBuilder()
            .setActivity(this)
            .build()
            .inject(this)
    }

    private fun initData(){
        this.onBackPressedDispatcher
            .addCallback(this, MOnBackPressedCallback(this))
    }

    private fun initViews(){
        rootView = findViewById(R.id.rootView)

        btnPatient = findViewById(R.id.btnPatient_activity_register)
        btnDoctor = findViewById(R.id.btnDoctor_activity_register)
        ivLogo = findViewById(R.id.ivLogo_activity_register)
        tvLogoText = findViewById(R.id.tvText_activity_register)

       // val animationLogo = AnimationUtils.loadAnimation(this, R.anim.registration_logo_anim)

        ivLogo.alpha = 0F
        val logoAnimator:ValueAnimator = ValueAnimator.ofFloat(0f,1f)
        logoAnimator.duration = 1_500
        logoAnimator.interpolator = LinearInterpolator()
        logoAnimator.addUpdateListener {
            val animatorValue:Float = it.animatedValue as Float

            ivLogo.alpha = animatorValue
            ivLogo.scaleX = animatorValue
            ivLogo.scaleY = animatorValue
        }
        logoAnimator.start()

        val animationText = AnimationUtils.loadAnimation(this, R.anim.regisrtration_text_anim)
        animationText.startOffset = 500

        tvLogoText.startAnimation(animationText)

        btnPatient.setOnClickListener{
            val intent = Intent(this@RegistrationActivity, RegistrationPatientActivity::class.java )
            startActivity(intent)
        }
        btnDoctor.setOnClickListener{
            val intent = Intent(this@RegistrationActivity, RegistrationDoctorActivity::class.java)
            startActivity(intent)
        }

    }
}