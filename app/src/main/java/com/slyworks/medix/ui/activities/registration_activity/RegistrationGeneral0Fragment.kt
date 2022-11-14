package com.slyworks.medix.ui.activities.registration_activity

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.slyworks.medix.R
import com.slyworks.medix.databinding.FragmentRegistrationGeneral0Binding

class RegistrationGeneral0Fragment : Fragment() {
    //region Vars
    private lateinit var binding:FragmentRegistrationGeneral0Binding
    //endregion

    companion object {
        @JvmStatic
        fun newInstance():RegistrationGeneral0Fragment = RegistrationGeneral0Fragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
       binding = FragmentRegistrationGeneral0Binding.inflate(layoutInflater, container, false)
        return binding.root
    }
}