package com.slyworks.medix.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.FragmentTransaction
import com.slyworks.medix.R
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
        initData()
        inflateFragment2(FindDoctorsFragment.getInstance())
    }

    fun initData(){
        requireActivity().onBackPressedDispatcher
            .addCallback(viewLifecycleOwner){
             if(childFragmentManager.backStackEntryCount > 1)
                 childFragmentManager.popBackStack()
             else {
                 /*let the ParentActivity handle it*/
                 isEnabled = false
                 requireActivity().onBackPressed()
             }
        }
    }
    fun inflateFragment2(f:Fragment){
        val transaction:FragmentTransaction = childFragmentManager.beginTransaction()
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)

        /*since there are only 2 fragments for this Fragment, if backStackEntryCount > 0,
        hide the first, which should always be FindDoctorFragment*/
        if(childFragmentManager.backStackEntryCount > 0)
            transaction.hide(childFragmentManager.fragments.first())

        transaction.addToBackStack("${f::class.simpleName}")
        transaction.add(R.id.fragment_container_profile_host, f, "${f::class.simpleName}")

        transaction.commit()
    }



}