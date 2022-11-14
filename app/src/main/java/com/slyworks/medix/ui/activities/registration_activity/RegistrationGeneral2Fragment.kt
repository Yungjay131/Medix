 package com.slyworks.medix.ui.activities.registration_activity

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.slyworks.medix.R
import com.slyworks.medix.databinding.FragmentRegistrationGeneral2Binding

 class RegistrationGeneral2Fragment : Fragment() {
    //region Vars
    private lateinit var binding: FragmentRegistrationGeneral2Binding
    private lateinit var registrationViewModel:RegistrationActivityViewModel
    //endregion

    companion object {
        @JvmStatic
        fun newInstance(): RegistrationGeneral2Fragment = RegistrationGeneral2Fragment()
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_registration_general2, container, false)
    }


}