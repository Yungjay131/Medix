package com.slyworks.medix.ui.fragments

import com.google.android.material.appbar.AppBarLayout
import kotlin.math.abs

abstract class AppBarStateChangeListener : AppBarLayout.OnOffsetChangedListener {
    enum class AppBarState{ EXPANDED, COLLAPSED, IDLE; }

    private var mCurrentAppBarState:AppBarState = AppBarState.IDLE

    abstract fun onStateChanged(appBarLayout: AppBarLayout, state: AppBarState)
    override fun onOffsetChanged(appBarLayout: AppBarLayout?, verticalOffset: Int) {
        when{
            verticalOffset == 0 -> {
                if(mCurrentAppBarState !=  AppBarState.EXPANDED)
                    onStateChanged(appBarLayout!!, AppBarState.EXPANDED)

                mCurrentAppBarState = AppBarState.EXPANDED
            }
            abs(verticalOffset) >= appBarLayout!!.totalScrollRange ->{
                if(mCurrentAppBarState != AppBarState.COLLAPSED)
                    onStateChanged(appBarLayout, AppBarState.COLLAPSED)

                mCurrentAppBarState = AppBarState.COLLAPSED
            }
            else -> {
                if(mCurrentAppBarState != AppBarState.IDLE)
                    onStateChanged(appBarLayout, AppBarState.IDLE)

                mCurrentAppBarState = AppBarState.IDLE
            }
        }
    }


}