package com.slyworks.medix.navigation

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.appcompat.app.AppCompatActivity


/**
 *Created by Joshua Sylvanus, 10:50 AM, 21/06/2022.
 */
class Navigator {
    data class NavContinuation(val intent: Intent,
                               private var activity:Activity?){
        private var shouldFinishCaller:Boolean = false

        fun newAndClearTask(): NavContinuation {
            this.intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            return this
        }

        fun previousIsTop(): NavContinuation {
            this.intent.setFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP)
            return this
        }

        fun singleTop(): NavContinuation {
            this.intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            return this
        }

        inline fun<reified T> addExtra(key:String,extra:T): NavContinuation {
            when(T::class){
                String::class -> this.intent.putExtra(key, extra as String)
                Int::class -> this.intent.putExtra(key, extra as Int)
                Double::class-> this.intent.putExtra(key, extra as Double)
                Bundle::class-> this.intent.putExtra(key, extra as Bundle)
                Parcelable::class -> this.intent.putExtra(key, extra as Parcelable)
                ByteArray::class -> this.intent.putExtra(key, extra as ByteArray)
            }

            return this
        }

        fun finishCaller(): NavContinuation {
           shouldFinishCaller = true
            return this
        }

        fun navigate(){
            activity!!.startActivity(this.intent)
            if(shouldFinishCaller)
                activity!!.finish()
            activity = null
        }
    }

    companion object{
        @SuppressLint("StaticFieldLeak")
        var continuation: NavContinuation? = null

        inline fun <reified T: Activity> intentFor(from: Context): NavContinuation {
            continuation = NavContinuation(Intent(from, T::class.java), from as AppCompatActivity)
            return continuation!!
        }

        inline fun <reified T> Intent.getExtra(key:String):T{
            return when(T::class){
                String::class -> this.extras!!.getString(key) as T
                Int::class -> this.extras!!.getInt(key) as T
                Double::class-> this.extras!!.getDouble(key) as T
                Bundle::class-> this.extras!!.getBundle(key) as T
                ByteArray::class -> this.extras!!.getByteArray(key) as T
                else -> throw IllegalArgumentException("type of T is not supported")
            }
        }

        fun <T : Parcelable?> Intent.getParcelable(key:String):T {
            return this.extras!!.getParcelable<T>(key) as T
        }

    }
}