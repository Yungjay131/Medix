package com.slyworks.medix.ui.activities

import android.app.Activity
import android.app.Dialog
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.slyworks.constants.GOOGLE_API_SERVICES_ERROR_DIALOG_REQUEST_CODE
import com.slyworks.medix.ListenerManager
import com.slyworks.medix.utils.ActivityUtils
import com.slyworks.network.NetworkBroadcastReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.system.exitProcess


/**
 *Created by Joshua Sylvanus, 6:26 PM, 1/13/2022.
 */

open class BaseActivity : AppCompatActivity() {
    private var mBroadcastReceiver: NetworkBroadcastReceiver? = null
    private var mListenerManager: ListenerManager? = null

    protected open fun onCreate(activity: Activity){
        throw UnsupportedOperationException("please remove this method from your class")
        ActivityUtils.setCurrentActivityStatus(activity::class.simpleName!!, true)
    }

    protected open fun onDestroy(activity: Activity){
        throw UnsupportedOperationException("please remove this method from your class")
      ActivityUtils.setCurrentActivityStatus(activity::class.simpleName!!, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityUtils.incrementActivityCount()
    }

    override fun onDestroy() {
        super.onDestroy()
        ActivityUtils.decrementActivityCount()
    }



    override fun onStop() {
        super.onStop()
        ActivityUtils.setForegroundStatus(false)
    }

    protected open fun onStop(activity: Activity){
        throw UnsupportedOperationException("please remove this method from your class")

        CoroutineScope(Dispatchers.Main).launch {
            //unregisterBroadcastReceiver(activity)
            stopListenerManager()
            ActivityUtils.setForegroundStatus(false)
        }
    }



    protected open fun onStart(activity:Activity){
        throw UnsupportedOperationException("please remove this method from your class")

        /*to avoid doing too much work on main thread???*/
         CoroutineScope(Dispatchers.Main).launch {
            // initNetworkBroadcastReceiver(activity)
             initListenerManager()
             ActivityUtils.setForegroundStatus(true)
         }
    }


    private fun initListenerManager(){
        mListenerManager = ListenerManager.getInstance()
    }

    private fun stopListenerManager(){
        mListenerManager?.nullify()
        mListenerManager = null
    }

    override fun onResume() {
        super.onResume()
        handleGooglePlayServicesAvailability()
        ActivityUtils.setForegroundStatus(true)
    }

    private fun handleGooglePlayServicesAvailability(){
        if(checkGooglePlayServices()) return

        Toast.makeText(this, "Error with google services",Toast.LENGTH_LONG).show()
        exitProcess(0) //equivalent of System.exit(0)
    }

    private fun checkGooglePlayServices():Boolean{
        val googlePlayInstance = GoogleApiAvailability.getInstance()

        val areServicesAvailable:Int = googlePlayInstance.isGooglePlayServicesAvailable(this)

        if(areServicesAvailable == ConnectionResult.SUCCESS){
            return true
        }else if(googlePlayInstance.isUserResolvableError(areServicesAvailable)){
            val dialog: Dialog? = googlePlayInstance.getErrorDialog(this, areServicesAvailable, GOOGLE_API_SERVICES_ERROR_DIALOG_REQUEST_CODE)
            dialog?.show()
            return true
        }else{
            return false
        }
    }



}