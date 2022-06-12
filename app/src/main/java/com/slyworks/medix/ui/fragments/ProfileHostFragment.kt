package com.slyworks.medix.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentTransaction
import com.slyworks.medix.R
import com.slyworks.medix.navigation.FragmentWrapper
import com.slyworks.medix.navigation.NavigationManager

class ProfileHostFragment : Fragment() {
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         */
        @JvmStatic
        fun newInstance():ProfileHostFragment {
            return ProfileHostFragment()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initData()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        NavigationManager.inflateFragment(FragmentWrapper.PROFILE_HOST,
                                           FragmentWrapper.FIND_DOCTORS,
                                           addToBackStack = true)
        return inflater.inflate(R.layout.fragment_profile_host, container, false)
    }

    private fun initData(){
        FragmentWrapper.PROFILE_HOST.setFragmentManager(childFragmentManager)
    }

    fun inflateFragment2(f:Fragment){
        val transaction:FragmentTransaction = childFragmentManager.beginTransaction()
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)

        if(f.isAdded) transaction.show(f)
        else transaction.replace(R.id.fragment_container_profile_host, f)

        transaction.commit()
    }



}