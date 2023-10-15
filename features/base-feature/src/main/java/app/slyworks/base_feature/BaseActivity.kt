package app.slyworks.base_feature

import android.app.Dialog
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import app.slyworks.base_feature.ui.ProgressOverlayDialog
import app.slyworks.base_feature.ui.PromptDialog
import app.slyworks.context_provider_lib.ContextProvider
import app.slyworks.utils_lib.GOOGLE_API_SERVICES_ERROR_DIALOG_REQUEST_CODE
import app.slyworks.utils_lib.KEY_ACTIVITY_COUNT
import app.slyworks.utils_lib.utils.plusAssign
import app.slyworks.utils_lib.utils.toggleVisibility
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import de.hdodenhof.circleimageview.CircleImageView
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlin.system.exitProcess


/**
 * Created by Joshua Sylvanus, 6:26 PM, 13/01/2022.
 */

abstract class BaseActivity : AppCompatActivity(){
    private val exitAnim: Animation = AnimationUtils.loadAnimation(ContextProvider.getContext(), R.anim.network_status_exit)
    private val enterAnim: Animation = AnimationUtils.loadAnimation(ContextProvider.getContext(), R.anim.network_status_enter)

    private lateinit var googlePlayInstance: GoogleApiAvailability

    private val disposable: CompositeDisposable = CompositeDisposable()

    private var basePromptDialog: PromptDialog? = null
    private var baseProgressOverlayDialog: ProgressOverlayDialog? = null

    abstract val viewModel: BaseViewModel?

    init{
        exitAnim.setAnimationListener( object : Animation.AnimationListener {
            override fun onAnimationStart(p0: Animation?) {}
            override fun onAnimationRepeat(p0: Animation?) {}
            override fun onAnimationEnd(p0: Animation?) {
                findViewById<ConstraintLayout>(R.id.l_network_status)?.toggleVisibility(false)
            }
        })

        enterAnim.setAnimationListener( object : Animation.AnimationListener {
            override fun onAnimationEnd(p0: Animation?) {}
            override fun onAnimationRepeat(p0: Animation?) {}
            override fun onAnimationStart(p0: Animation?) {
                findViewById<ConstraintLayout>(R.id.l_network_status)?.toggleVisibility(true)
            }
        })
    }

    abstract fun cancelOngoingOperation():Unit

    private fun stopObservingNetworkChanges():Unit = disposable.clear()

    private fun startObservingNetworkChanges(){
        val networkStatusView: ConstraintLayout =
            findViewById(R.id.l_network_status) ?: return
        val iv: ImageView = networkStatusView.findViewById(R.id.iv_status)
        val iv2: CircleImageView = networkStatusView.findViewById(R.id.iv_status2)
        val tv: TextView = networkStatusView.findViewById(R.id.tv_status)

        disposable +=
            viewModel!!.networkRegister
                .subscribeToNetworkUpdates()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    if(it && networkStatusView.isVisible){
                        networkStatusView.setBackgroundColor(
                            ContextCompat.getColor(
                                ContextProvider.getContext(),
                                app.slyworks.ui_base_lib.R.color.app_green
                            )
                        )

                        iv.toggleVisibility(false)
                        iv2.toggleVisibility(true)
                        tv.setText("online")

                        networkStatusView.startAnimation(exitAnim)
                    }
                    else if(!it && !networkStatusView.isVisible){
                        networkStatusView.setBackgroundColor(
                            ContextCompat.getColor(
                                ContextProvider.getContext(),
                                app.slyworks.ui_base_lib.R.color.app_pink
                            )
                        )

                        iv.toggleVisibility(true)
                        iv2.toggleVisibility(false)
                        tv.setText("no internet connection")

                        networkStatusView.startAnimation(enterAnim)
                    }
                }
    }

    /**
     * this method is to ensure that there is only one MessageDialog shown at a time
     * to avoid situations i noticed that there were multiple MessageDialogs shown at once
     * and the [BasePromptDialog] allows for the dialog to be cancelled when the Activity
     * is going out of view
     * */
    fun showPromptDialogFromBaseActivity(message:String,
                                          prompt:String = "ok",
                                          isCancellable:Boolean = false,
                                          onDismiss: (() -> Unit)? = null){
        basePromptDialog?.dismiss()
        basePromptDialog = PromptDialog(message, prompt, isCancellable, onDismiss)
        basePromptDialog!!.show(supportFragmentManager,"")
    }


    fun toggleProgressOverlayDialog(status:Boolean, prompt:String? = null){
        if(status){
            baseProgressOverlayDialog = ProgressOverlayDialog(prompt)
            baseProgressOverlayDialog!!.show(supportFragmentManager, ProgressOverlayDialog::class.simpleName)
        }else{
            baseProgressOverlayDialog?.dismissAllowingStateLoss()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.dispose()
        ActivityHelper.decrementActivityCount()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(savedInstanceState != null) {
            ActivityHelper.setActivityCount(savedInstanceState.getInt(KEY_ACTIVITY_COUNT) - 1)
        }

        ActivityHelper.incrementActivityCount()
        googlePlayInstance = GoogleApiAvailability.getInstance()
    }

    override fun onResume() {
        super.onResume()

        ActivityHelper.setForegroundStatus(true, this@BaseActivity::class.simpleName!!)

        startObservingNetworkChanges()

        handleGooglePlayServicesAvailability()
    }

    override fun onStop() {
        super.onStop()

        ActivityHelper.setForegroundStatus(false, this@BaseActivity::class.simpleName!!)

        stopObservingNetworkChanges()
    }

    /* to handle cases of Process Recreation where ActivityHelper#getActivityCount() would return 0
    * instead of the actual number because it was recreated as well */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putInt(KEY_ACTIVITY_COUNT, ActivityHelper.getActivityCount())
    }

    override fun finish(){
        super.finish()
        overridePendingTransition(
            R.anim.activity_enter_slide_in,
            R.anim.activity_exit_fade_out)
    }

    private fun handleGooglePlayServicesAvailability(){
        val areServicesAvailable: Int = googlePlayInstance.isGooglePlayServicesAvailable(this)
        if (googlePlayInstance.isUserResolvableError(areServicesAvailable)) {
            val dialog: Dialog? = googlePlayInstance.getErrorDialog(
                this,
                areServicesAvailable,
                GOOGLE_API_SERVICES_ERROR_DIALOG_REQUEST_CODE)
            dialog?.show()
        } else if(areServicesAvailable != ConnectionResult.SUCCESS) {
          Toast.makeText(this, "Error with Google Play Services", Toast.LENGTH_LONG).show()
          exitProcess(0)
        }
    }

}