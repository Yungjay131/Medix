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
import com.slyworks.medix.ui.fragments.findDoctorsFragment.FindDoctorsFragment

class ProfileHostFragment : Fragment() {
    companion object {
        @JvmStatic
        fun getInstance():ProfileHostFragment {
            return ProfileHostFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_profile_host, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        inflateFragment2(FindDoctorsFragment.newInstance())
    }

    fun inflateFragment2(f:Fragment){
        val transaction:FragmentTransaction = childFragmentManager.beginTransaction()
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)

        if(f.isAdded) transaction.show(f)
        else transaction.replace(R.id.fragment_container_profile_host, f, "${f::class.simpleName}")

        transaction.commit()
    }



}