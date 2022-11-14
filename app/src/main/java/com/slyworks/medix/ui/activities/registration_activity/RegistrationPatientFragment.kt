package com.slyworks.medix.ui.activities.registration_activity

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.slyworks.medix.R

class RegistrationPatientFragment : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance():RegistrationPatientFragment = RegistrationPatientFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_registration_patient, container, false)
    }


}