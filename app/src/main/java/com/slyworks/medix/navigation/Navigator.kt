package com.slyworks.medix.navigation

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction


/**
 *Created by Joshua Sylvanus, 10:50 AM, 21/06/2022.
 */
inline fun<reified T> Navigator.ActivityContinuation.addExtra(key:String, extra:T): Navigator.ActivityContinuation {
    with(this as Navigator.ActivityContinuationImpl){
        when(T::class){
            String::class -> intent.putExtra(key, extra as String)
            Int::class -> intent.putExtra(key, extra as Int)
            Double::class-> intent.putExtra(key, extra as Double)
            Bundle::class-> intent.putExtra(key, extra as Bundle)
            Parcelable::class -> intent.putExtra(key, extra as Parcelable)
            ByteArray::class -> intent.putExtra(key, extra as ByteArray)
            else -> throw UnsupportedOperationException("this data type is not supported")
        }
    }
    return this
}

class Navigator
private constructor() {
    interface ActivityContinuation{
        fun newAndClearTask():ActivityContinuation
        fun previousIsTop():ActivityContinuation
        fun finishCaller():ActivityContinuation
        fun navigate()
    }

    interface FragmentContinuation{
        fun into(@IdRes containerID: Int):FragmentContinuation
        fun hideCurrent():FragmentContinuation
        fun show(f:Fragment, currentTag: String?):FragmentContinuation
        fun show(f:Fragment):FragmentContinuation
        fun after(alsoFunc: () -> Unit):FragmentContinuation
        fun navigate()
    }

    data class ActivityContinuationImpl(val intent: Intent,
                                        private var activity:Activity?) : ActivityContinuation{
        private var shouldFinishCaller:Boolean = false

        override fun newAndClearTask(): ActivityContinuation {
            this.intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            return this
        }

        override fun previousIsTop(): ActivityContinuation {
            //this.intent.flags  = this.intent.flags or Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP
            this.intent.setFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP)
            return this
        }

        fun singleTop(): ActivityContinuation {
            this.intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            return this
        }

        override fun finishCaller(): ActivityContinuation {
           shouldFinishCaller = true
            return this
        }

        override fun navigate(){
            activity!!.startActivity(this.intent)
            if(shouldFinishCaller)
                activity!!.finish()
            activity = null
        }
    }

    data class FragmentContinuationImpl(private var _fragmentManager:FragmentManager?)
        :FragmentContinuation{
        private var fragmentManager:FragmentManager = _fragmentManager!!

        private var _transaction:FragmentTransaction? = null
        private var transaction:FragmentTransaction
        private var containerID: Int = 0

        init {
            _transaction = fragmentManager.beginTransaction()

            transaction = _transaction!!
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        }

        override fun into(@IdRes containerID:Int):FragmentContinuation{
            if(this.containerID != 0)
                throw UnsupportedOperationException("ContainerID has already been set")

            this.containerID = containerID
            return this
        }


        fun replace(f:Fragment):FragmentContinuation{
            transaction.addToBackStack("${f::class.simpleName}")
            transaction.replace(containerID, f)
            return this
        }

        override fun hideCurrent():FragmentContinuation{
            if(fragmentManager.backStackEntryCount > 0)
                transaction.hide(fragmentManager.findFragmentById(containerID)!!)

            return this
        }

        override fun show(f:Fragment, currentTag:String?):FragmentContinuation{
            if(fragmentManager.findFragmentByTag(f::class.simpleName) != null){
                /*its been added before*/
                transaction.hide(fragmentManager.findFragmentByTag(currentTag)!!)
                transaction.show(fragmentManager.findFragmentByTag(f::class.simpleName)!!)
            }else{
                if(currentTag != null)
                /*hide currently visible Fragment*/
                    transaction.hide(fragmentManager.findFragmentByTag(currentTag)!!)

                transaction.addToBackStack("${f::class.simpleName}")
                transaction.add(containerID, f, "${f::class.simpleName}")
            }

            return this
        }

        override fun show(f:Fragment):FragmentContinuation{
            if(fragmentManager.findFragmentByTag(f::class.simpleName) != null){
                /*its been added before*/
                transaction.hide(fragmentManager.findFragmentById(containerID)!!)
                transaction.show(fragmentManager.findFragmentByTag(f::class.simpleName)!!)
            }else{
                if(fragmentManager.findFragmentById(containerID) != null)
                    /*hide currently visible Fragment*/
                    transaction.hide(fragmentManager.findFragmentById(containerID)!!)

                transaction.addToBackStack("${f::class.simpleName}")
                transaction.add(containerID, f, "${f::class.simpleName}")
            }

            return this
        }

        override fun after(alsoFunc:() -> Unit):FragmentContinuation{
            alsoFunc()
            return this
        }

        override fun navigate(){
            transaction.commit()

            _transaction = null
            _fragmentManager = null
        }
    }

    companion object{
        inline fun <reified T: Activity> intentFor(from: Context): ActivityContinuation {
            return ActivityContinuationImpl(Intent(from, T::class.java), from as AppCompatActivity)
        }

        fun intentFor(from:Context, clazz: Class<out AppCompatActivity>):ActivityContinuation{
            return ActivityContinuationImpl(Intent(from, clazz), from as AppCompatActivity)
        }

        inline fun <reified T> Intent.getExtra(key:String):T?{
            return when(T::class){
                String::class -> this.getStringExtra(key) as? T
                Int::class -> this.getIntExtra(key, -1) as? T
                Double::class-> this.getDoubleExtra(key, -1.0) as? T
                Bundle::class-> this.getBundleExtra(key) as? T
                ByteArray::class -> this.getByteExtra(key, Byte.MIN_VALUE) as? T
                else -> throw IllegalArgumentException("type of T is not supported")
            }
        }

        fun <T : Parcelable?> Intent.getParcelable(key:String):T {
            return this.getParcelableExtra<T>(key) as T
        }

        fun transactionFrom(fragmentManager:FragmentManager):FragmentContinuation{
            return FragmentContinuationImpl(fragmentManager)
        }
    }
}

