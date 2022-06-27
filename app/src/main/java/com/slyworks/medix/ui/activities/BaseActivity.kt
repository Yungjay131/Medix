package com.slyworks.medix.ui.activities

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.slyworks.constants.GOOGLE_API_SERVICES_ERROR_DIALOG_REQUEST_CODE
import com.slyworks.medix.managers.ListenerManager
import com.slyworks.medix.ui.activities.loginActivity.LoginActivity
import com.slyworks.medix.ui.activities.onBoardingActivity.OnBoardingActivity
import com.slyworks.medix.ui.activities.registrationActivity.RegistrationActivity
import com.slyworks.medix.ui.activities.registrationActivity.RegistrationDoctorActivity
import com.slyworks.medix.ui.activities.registrationActivity.RegistrationPatientActivity
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

    private lateinit var mCurrentActivityTag:String
    protected open fun onCreate(activity: Activity){
        throw UnsupportedOperationException("please remove this method from your class")
    }

    protected open fun onDestroy(activity: Activity){
        throw UnsupportedOperationException("please remove this method from your class")
    }

    override fun onDestroy() {
        super.onDestroy()
        mListenerManager = null
        ActivityUtils.decrementActivityCount()
    }

    private fun isCurrentActivityValid():Boolean{
        /*not for login,registration,onboarding and splash activity*/
        val condition1 = this::class.simpleName == SplashActivity::class.simpleName
        val condition2 = this::class.simpleName == OnBoardingActivity::class.simpleName
        val condition3 = this::class.simpleName == LoginActivity::class.simpleName
        val condition4 = this::class.simpleName == RegistrationActivity::class.simpleName
        val condition5 = this::class.simpleName == RegistrationPatientActivity::class.simpleName
        val condition6 = this::class.simpleName == RegistrationDoctorActivity::class.simpleName
        return !(condition1 || condition2 || condition3 || condition4 || condition5 || condition6)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityUtils.incrementActivityCount()
    }


    override fun onStart() {
        super.onStart()

        ActivityUtils.setForegroundStatus(true, this::class.simpleName!!)

        if(isCurrentActivityValid())
          initListenerManager()
    }

    override fun onStop() {
        super.onStop()
        /*
        * i noticed that onStop() is called for the exiting activity after onStart()
        * has been called for the new activity, thereby cancelling the new activity's ListenerManager
        * instance and setting the ForegroundStatus wrongly*/
       /* if(this::class.simpleName != mCurrentActivityTag)
            return*/

        ActivityUtils.setForegroundStatus(false, this::class.simpleName!!)

        if(isCurrentActivityValid())
           stopListenerManager()
    }

    protected open fun onStop(activity: Activity){
        throw UnsupportedOperationException("please remove this method from your class")
    }

    protected open fun onStart(activity:Activity){
        throw UnsupportedOperationException("please remove this method from your class")
    }

    private fun initListenerManager(){
        if(mListenerManager == null)
            mListenerManager = ListenerManager()

        mListenerManager!!.start()
    }

    private fun stopListenerManager() {
        mListenerManager!!.stop()
    }


    override fun onResume() {
        super.onResume()
        /*to avoid doing too much work on the main thread*/
        CoroutineScope(Dispatchers.Main).launch {
          handleGooglePlayServicesAvailability()
        }
    }

    private fun handleGooglePlayServicesAvailability(){
        if(checkGooglePlayServices())
            return

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