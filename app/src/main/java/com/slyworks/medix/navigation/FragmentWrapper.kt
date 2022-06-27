package com.slyworks.medix.navigation

import android.os.Parcelable
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.slyworks.medix.R
import com.slyworks.medix.ui.fragments.callsHistoryFragment.CallsHistoryFragment
import com.slyworks.medix.ui.fragments.ProfileHostFragment
import com.slyworks.medix.ui.fragments.ViewProfileFragment
import com.slyworks.medix.ui.fragments.chatFragment.ChatFragment
import com.slyworks.medix.ui.fragments.chatHostFragment.ChatHostFragment
import com.slyworks.medix.ui.fragments.findDoctorsFragment.FindDoctorsFragment
import com.slyworks.medix.ui.fragments.homeFragment.PatientHomeFragment
import com.slyworks.medix.utils.isLastItem
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

/**
 *Created by Joshua Sylvanus, 5:34 PM, 1/27/2022.
 */
@Parcelize
enum class FragmentWrapper:Parcelable {
    HOME {
        override fun getInstance(args:Any?): Fragment {
            return PatientHomeFragment.getInstance()
        }
    },
    FIND_DOCTORS {
        override fun getInstance(args:Any?): Fragment {
            return FindDoctorsFragment.getInstance()
        }
    },
    CHAT_HOST {
        override fun getInstance(args:Any?): Fragment {
            return ChatHostFragment.getInstance()
        }
    },
    CHAT {
        override fun getInstance(args:Any?): Fragment {
            return ChatFragment.newInstance()
        }
    },
    CALLS_HISTORY {
        override fun getInstance(args:Any?): Fragment {
            return CallsHistoryFragment.newInstance()
        }
    },
    PROFILE_HOST {
        //region Vars
        @IgnoredOnParcel
        private var mFragmentManager: FragmentManager? = null
        @IgnoredOnParcel
        private var mFragmentList: MutableList<FragmentWrapper> = mutableListOf()
        //endregion

        override fun getInstance(args:Any?): Fragment {
            return ProfileHostFragment.getInstance()
        }

        override fun setFragmentManager(fragmentManager: FragmentManager?) {
            mFragmentManager = fragmentManager
        }

        override fun getFragmentManager(): FragmentManager  = mFragmentManager!!

        override fun getFragmentContainerID(): Int {
            return R.id.fragment_container_profile_host
        }

        override fun addFragment(fragment: FragmentWrapper) {
            mFragmentList.filter { it != fragment }
            mFragmentList.add(fragment)
        }

        override fun getNextFragment(): FragmentWrapper? {
            val f: FragmentWrapper? = mFragmentList.lastOrNull()
            if (f != null)
                mFragmentList.removeLast()

            return f
        }

        override fun isThereNextItem(): Boolean {
            return mFragmentList.isLastItem()
        }
    },
    VIEW_PROFILE {
        override fun getInstance(args:Any?): Fragment {
            return ViewProfileFragment.newInstance(args!!)
        }
    };

    abstract fun getInstance(args:Any? = null): Fragment

    open fun setFragmentManager(fragmentManager: FragmentManager?){}
    open fun getFragmentManager(): FragmentManager? = null
    open fun getFragmentContainerID(): Int = 0
    open fun addFragment(fragment: FragmentWrapper){}
    open fun isThereNextItem():Boolean = false
    open fun getNextFragment():FragmentWrapper? = null

}