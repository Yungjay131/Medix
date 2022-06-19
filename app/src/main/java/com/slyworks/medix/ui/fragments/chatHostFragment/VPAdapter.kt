package com.slyworks.medix.ui.fragments.chatHostFragment

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.slyworks.medix.ui.fragments.callsHistoryFragment.CallsHistoryFragment
import com.slyworks.medix.ui.fragments.chatFragment.ChatFragment


/**
 *Created by Joshua Sylvanus, 10:30 AM, 1/13/2022.
 */
class VPAdapter(private val fragmentManager:FragmentManager, lifecycle:Lifecycle) : FragmentStateAdapter(fragmentManager,lifecycle){
    companion object{
        //region Vars
        //endregion
    }
    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
       /* if(mList[position].isAdded)
            fragmentManager.beginTransaction()
                .remove(mList[position])
                .commit()

        return mList[position]*/
        return when(position){
            0 -> ChatFragment.newInstance()
            1 -> CallsHistoryFragment.newInstance()
            else -> throw IllegalArgumentException()
        }
    }

}