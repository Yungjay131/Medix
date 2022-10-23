package com.slyworks.medix.ui.activities.otp_activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.slyworks.medix.databinding.ActivityOtpactivityBinding

class OTPActivity : AppCompatActivity() {
    //region Vars
    private lateinit var binding:ActivityOtpactivityBinding
    //endregion

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtpactivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

}