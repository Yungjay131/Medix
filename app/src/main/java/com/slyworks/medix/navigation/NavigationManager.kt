package com.slyworks.medix.navigation

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.slyworks.constants.EXTRA_ACTIVITY
import com.slyworks.medix.utils.isLastItem


/**
 *Created by Joshua Sylvanus, 4:28 PM, 12/05/2022.
 */
object NavigationManager {
    //region Vars
    private var mNavigationItemList:MutableList<ActivityWrapper> = mutableListOf()
    private var mCurrentActivity: ActivityWrapper? = null
    //endregion


    fun onBackPressed(origin:Activity,
                      shouldFinishCurrent:Boolean = false,
                      fallbackActivity:ActivityWrapper? = null){
        /*first check if there is there is dialog*/

        /*then check for Fragment*/
        if(mCurrentActivity!!.isThereNextItem()){
            val f:FragmentWrapper = mCurrentActivity!!.getNextFragment()!!
            if(f.isThereNextItem()){
                /*in the case of Fragments that have children fragments*/
                val _f:FragmentWrapper = f.getNextFragment()!!
                inflateFragment(f,_f)
            }else {
                inflateFragment(mCurrentActivity!!, f)
            }
        }else{
            if(mNavigationItemList.isLastItem()){
                if(fallbackActivity != null){
                    inflateActivity(origin,fallbackActivity,true, shouldFinishCurrent)
                    return
                }

                /*else inflateExitDialog()*/
                inflateDialog(mCurrentActivity!!,DialogWrapper.EXIT)
                return
            }else{
                /*pop to next activity*/
                val a:ActivityWrapper = mNavigationItemList[mNavigationItemList.lastIndex - 1]

                /*just pop backstack to it*/
              /*  if(a.isRunning()){
                    mNavigationItemList.remove(mCurrentActivity)
                    mNavigationItemList = mNavigationItemList.filter { it != a } as MutableList<ActivityWrapper>
                    mNavigationItemList.add(a)
                    origin.finish()
                    mCurrentActivity = a
                    return
                }*/

                inflateActivity(origin, a, true, shouldFinishCurrent)
            }
        }
    }


    fun inflateActivity(origin: Activity,
                        activity: ActivityWrapper,
                        removeCurrentFromBackStack:Boolean = false,
                        isToBeFinished:Boolean = false,
                        extras: Bundle? = null,
                        clearBackStack:Boolean = false){
        val intent: Intent = Intent(origin, activity.getInstance())
        if(removeCurrentFromBackStack) {
            /*to avoid errors from SplashActivity, since at that point the List is empty*/
            mCurrentActivity?.let{ mNavigationItemList.removeLast() }
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        if(clearBackStack)
            mNavigationItemList.clear()
        else
            mNavigationItemList = mNavigationItemList.filter { it != activity } as MutableList


        mNavigationItemList.add(activity)

        if(extras != null)
            intent.putExtra(EXTRA_ACTIVITY, extras)

        origin.startActivity(intent)

        if(isToBeFinished)
            origin.finish()


        mCurrentActivity = activity
    }

    fun inflateFragment(fragmentHost:FragmentWrapper,
                        fragment: FragmentWrapper,
                        args:Any? = null,
                        addToBackStack:Boolean = false){
        /*for inflating a Fragment as child of another Fragment
        * eg ProfileHostFragment hosting FindDoctorsFragment and ViewProfileFragment*/
        if(addToBackStack) fragmentHost.addFragment(fragment)

        val f:Fragment = fragment.getInstance(args)

        val transaction: FragmentTransaction = fragmentHost.getFragmentManager()!!.beginTransaction()
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)

        if(f.isAdded) transaction.show(f)
        else transaction.add(fragmentHost.getFragmentContainerID(), f)

        transaction.commit()
    }

    fun inflateFragment(activity:ActivityWrapper,
                        fragment: FragmentWrapper,
                        args:Any? = null,
                        addToBackStack:Boolean = false){
        if(addToBackStack) activity.addFragment(fragment)

        val f: Fragment = fragment.getInstance(args)

        val transaction: FragmentTransaction = activity.getFragmentManager().beginTransaction()
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)

        if(f.isAdded) transaction.show(f)
        else transaction.add(activity.getFragmentContainerID(), f)

        transaction.commit()
    }

    fun inflateDialog(activity:ActivityWrapper,
                      dialog:DialogWrapper,
                      args:Any? = null){
        activity.setDialog(dialog)

        dialog.getInstance(args)
              .show(activity.getFragmentManager(), "")
    }

    fun removeCurrentDialog(){
        mCurrentActivity!!.setDialog(null)
    }



}