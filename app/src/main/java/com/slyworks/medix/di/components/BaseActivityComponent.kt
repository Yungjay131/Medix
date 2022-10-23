package com.slyworks.medix.di.components

import android.app.Activity
import com.slyworks.di.BaseActivityScope
import com.slyworks.medix.di.modules.BaseActivityModule
import dagger.Subcomponent


/**
 *Created by Joshua Sylvanus, 8:08 PM, 15/10/2022.
 */

@BaseActivityScope
@Subcomponent(modules = [
    BaseActivityModule::class
])
abstract class BaseActivityComponent {
    @JvmField
    var baseActivityComponent:BaseActivityComponent? = null

    fun isThereCachedBaseActivityComponent():Boolean = baseActivityComponent == null
    fun getBaseActivityComponent():BaseActivityComponent = baseActivityComponent!!
    fun cacheBaseActivityComponent(bac:BaseActivityComponent){
        baseActivityComponent = bac;
    }

    abstract fun inject(activity: Activity)

    @Subcomponent.Builder
    interface Builder{
        fun build():BaseActivityComponent
    }
}