package com.slyworks.medix.utils

import android.content.Context
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.slyworks.constants.INCOMING_CALL_NOTIFICATION
import com.slyworks.constants.INPUT_ERROR
import com.slyworks.medix.App


/**
 *Created by Joshua Sylvanus, 8:58 PM, 09/05/2022.
 */

/**singleton class to control App wide to control app wide vibration*/
object VibrationManager {

    fun stopVibration(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            val v:VibratorManager = App.getContext().getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            v.cancel()
        }else{
            /*deprecated in Api 26*/
            val v:Vibrator = App.getContext().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            v.cancel()
        }
    }

    /**
     * type = INPUT_ERROR or INCOMING_CALL_NOTIFICATION*/
    fun vibrate(type:Int){
        var vibrationPattern:LongArray = longArrayOf()
        when(type){
           INPUT_ERROR -> {
                vibrationPattern = longArrayOf(0, 100, 200, 100)
           }
           INCOMING_CALL_NOTIFICATION ->{
               vibrationPattern = longArrayOf(1, 100, 500, 100)
           }
        }

        vibrate(vibrationPattern)
    }

    /*vibrate for 500secs*/
    private fun vibrate(vibrationPattern:LongArray){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            val v:VibratorManager = App.getContext().getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            val effect:VibrationEffect = VibrationEffect.createWaveform(vibrationPattern, -1) /*.createOneShot(time, VibrationEffect.DEFAULT_AMPLITUDE)*/
            val vibe:CombinedVibration = CombinedVibration.createParallel(effect)
            v.vibrate(vibe)
        }else if(Build.VERSION.SDK_INT < Build.VERSION_CODES.S && Build.VERSION.SDK_INT > Build.VERSION_CODES.O){
            val v:Vibrator = App.getContext().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            val effect:VibrationEffect = VibrationEffect.createWaveform(vibrationPattern, -1)
            v.vibrate(effect)
        }else{
            /*deprecated in Api 26*/
            val v:Vibrator = App.getContext().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            v.vibrate(vibrationPattern, -1)
        }

    }

}