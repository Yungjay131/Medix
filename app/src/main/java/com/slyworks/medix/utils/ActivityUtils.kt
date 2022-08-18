package com.slyworks.medix.utils

import androidx.appcompat.app.AppCompatActivity
import com.slyworks.medix.ui.activities.main_activity.MainActivity


/**
 *Created by Joshua Sylvanus, 10:08 PM, 18/05/2022.
 */
object ActivityUtils {
    //region Vars
    private var mForegroundStatus:Boolean = true
    private var mCount:Int = 0
    private var mCurrentActivityTag:String = ""
    //endregion

    fun from(simpleName:String):Class<out AppCompatActivity>{
        return when(simpleName){
            MainActivity::class.simpleName -> MainActivity::class.java
            else -> throw IllegalArgumentException("fix the \"from\" method")
        }
    }

    fun setForegroundStatus(status:Boolean, tag:String){
        if(status){
            mCurrentActivityTag = tag
            mForegroundStatus = true
        }else{
         if(tag == mCurrentActivityTag)
            mForegroundStatus = status
        }
    }

    fun isThereActivityInForeground():Boolean =  mForegroundStatus

    fun isLastActivity():Boolean = mCount == 1
    fun incrementActivityCount() = mCount++
    fun decrementActivityCount() = mCount--

}

