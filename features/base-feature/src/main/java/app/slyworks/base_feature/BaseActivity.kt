package app.slyworks.base_feature

import android.app.Dialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import app.slyworks.base_feature.custom_views.NetworkStatusView
import app.slyworks.constants_lib.GOOGLE_API_SERVICES_ERROR_DIALOG_REQUEST_CODE
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.system.exitProcess


/**
 *Created by Joshua Sylvanus, 6:26 PM, 1/13/2022.
 */

open class BaseActivity : AppCompatActivity(), IValidForListening {
    //region Vars
    /*@Inject
    @JvmField
    var connectionStatusManager:ConnectionStatusManager? = null*/

    //lateinit var networkStatusView:NetworkStatusView

    @Inject
    @JvmField
    var listenerManager: ListenerManager? = null
    //endregion

    companion object{
     // private var bac:BaseActivityComponent? = null
    }

    override fun onDestroy() {
        super.onDestroy()
        listenerManager = null
        ActivityUtils.decrementActivityCount()
    }

   /* private fun isCurrentActivityValid():Boolean{
        *//* not for login, registration, onboarding and splash activity *//*
        val condition1 = this::class.simpleName == SplashActivity::class.simpleName
        val condition2 = this::class.simpleName == app.slyworks.auth_feature.OnBoardingActivity::class.simpleName
        val condition3 = this::class.simpleName == app.slyworks.auth_feature.LoginActivity::class.simpleName
        val condition4 = this::class.simpleName == app.slyworks.auth_feature.RegistrationActivity::class.simpleName
        val condition5 = this::class.simpleName == app.slyworks.auth_feature.RegistrationPatientActivity::class.simpleName
        val condition6 = this::class.simpleName == app.slyworks.auth_feature.RegistrationDoctorActivity::class.simpleName
        return !(condition1 || condition2 || condition3 || condition4 || condition5 || condition6)
    }*/

    override fun isValid(): Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        if(isValid())
            initDI()

        super.onCreate(savedInstanceState)

        initNetworkStatusView()

        ActivityUtils.incrementActivityCount()
    }

    private fun initDI(){
       /* if(bac == null)
            bac = application.appComponent
                .baseActivityComponentBuilder()
                .build()

        App.getListenerManager()
           .ifPresentOrElse({this.listenerManager = it}, { bac!!.inject(this) })

        App.cacheListenerManager(this.listenerManager!!)*/
    }

    private fun initNetworkStatusView(){ }

    override fun onStart() {
        super.onStart()

        ActivityUtils.setForegroundStatus(true, this@BaseActivity::class.simpleName!!)

        if(isValid())
          listenerManager!!.start()
    }

    override fun onStop() {
        super.onStop()

        ActivityUtils.setForegroundStatus(false, this@BaseActivity::class.simpleName!!)

        if(isValid())
            listenerManager!!.stop()
    }

    override fun onResume() {
        super.onResume()
        /*to avoid doing too much work on the main thread */
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