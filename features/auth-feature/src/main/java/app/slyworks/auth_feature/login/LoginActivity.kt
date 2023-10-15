package app.slyworks.auth_feature.login

import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import app.slyworks.auth_feature.R
import app.slyworks.auth_feature._di.AuthFeatureComponent
import app.slyworks.auth_feature.databinding.ActivityLoginBinding
import app.slyworks.base_feature.BaseActivity
import app.slyworks.base_feature.BaseViewModel
import app.slyworks.base_feature.MOnBackPressedCallback

import app.slyworks.utils_lib.*
import app.slyworks.utils_lib.utils.*
import dev.joshuasylvanus.navigator.Navigator
import dev.joshuasylvanus.navigator.Navigator.Companion.getExtra
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.disposables.Disposable
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class LoginActivity : BaseActivity() {
    //region Vars
    private lateinit var mediaPlayer: MediaPlayer

    private var forgotPasswordBSDialog: ForgotPasswordBSDialog? = null

    private lateinit var binding: ActivityLoginBinding

    @Inject
    override lateinit var viewModel: LoginActivityViewModel
    //endregion

    override fun cancelOngoingOperation() {
        showPromptDialogFromBaseActivity(CANCEL_OPERATION_PROMPT)
        {
            (viewModel as BaseViewModel).cancelOngoingOperation()
            toggleProgressOverlayDialog(false)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(KEY_LOGIN_EMAIL, binding.etLoginEmail.properText)
        outState.putString(KEY_LOGIN_PASSWORD, binding.etLoginPassword.properText)
        outState.putBoolean(EXTRA_IS_ACTIVITY_RECREATED, true)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
    }

    override fun onResume() {
        super.onResume()
        closeKeyboard()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        initDI()

        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initData()
        initViews(savedInstanceState)
    }

    private fun initDI(){
       /* AuthFeatureComponent.getInitialBuilder()
            .build()
            .inject(this)
    */
    }

    @SuppressLint("CheckResult")
    private fun initData(){
        this.onBackPressedDispatcher
            .addCallback(this, MOnBackPressedCallback(this))

        val destinationIntentFilter:String =
            intent.getExtra<String>(EXTRA_LOGIN_DESTINATION, MAIN_ACTIVITY_INTENT_FILTER)!!
        val originalBundle:Bundle? = intent.getExtra<Bundle>(EXTRA_INITIAL_EXTRA)

        mediaPlayer = MediaPlayer.create(this, app.slyworks.base_feature.R.raw.positive_button_sound)

        viewModel.uiStateLD.observe(this){
            when(it){
                is LoginUIState.LoadingStarted ->
                    toggleProgressOverlayDialog(true)


                is LoginUIState.LoadingStopped ->
                    toggleProgressOverlayDialog(false)

                is LoginUIState.LoadingForgotPasswordStarted ->
                    toggleProgressOverlayDialog(true,  "sending password reset email")

                is LoginUIState.LoadingForgotPasswordStopped ->
                    toggleProgressOverlayDialog(false)

                is LoginUIState.ResetPasswordEmailSuccess ->
                    displayMessage("a password reset email has been sent, please check your inbox", binding.root)

                is LoginUIState.ResetPasswordEmailFailure ->
                    displayMessage("oh oh something went  wrong on our end, please try again later", binding.root)

                is LoginUIState.LoginSuccess -> {
                    mediaPlayer.start()
                    mediaPlayer.setLooping(false)

                    Completable.timer(2_000, TimeUnit.MILLISECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            mediaPlayer.stop()
                            mediaPlayer.release()

                            Navigator.intentFor(this, destinationIntentFilter)
                                .also { it2 ->
                                    if(originalBundle != null){
                                        it2.addExtra(EXTRA_ACTIVITY, originalBundle)
                                    }
                                }
                                .newAndClearTask()
                                .navigate()
                        },
                        {
                            Timber.e(it)

                            Navigator.intentFor(this, destinationIntentFilter)
                                .also { it2 ->
                                    if(originalBundle != null){
                                        it2.addExtra(EXTRA_ACTIVITY, originalBundle)
                                    }
                                }
                                .newAndClearTask()
                                .navigate()
                        })
                }

                is LoginUIState.AccountNotVerified ->
                    Navigator.intentFor(this, VERIFICATION_ACTIVITY_INTENT_FILTER)
                        .addExtra(KEY_EMAIL, binding.etLoginEmail.properText)
                        .addExtra(KEY_PASS, binding.etLoginPassword.properText)
                        .navigate()

                is LoginUIState.Message ->
                    displayMessage(it.message, binding.root)
            }
        }

    }

    private fun initViews(savedInstanceState: Bundle?){
        if(savedInstanceState != null){
            val email:String  = savedInstanceState.getString(KEY_LOGIN_EMAIL)!!
            val password:String = savedInstanceState.getString(KEY_LOGIN_PASSWORD)!!

            binding.etLoginEmail.setText(email)
            binding.etLoginPassword.setText(password)
        }

        binding.etLoginPassword.setOnEditorActionListener { v, actionId, event ->
            closeKeyboard()

            val email:String = binding.etLoginEmail.properText
            val password:String = binding.etLoginPassword.properText
            if(!check(email, password))
                return@setOnEditorActionListener false

            viewModel.login(email,password)
            return@setOnEditorActionListener true
        }

        binding.lAppBar.ivBacker.setOnClickListener{
            onBackPressedDispatcher.onBackPressed()
        }

        binding.tvLoginRegister2.setOnClickListener {
            Navigator.intentFor(this@LoginActivity, REGISTRATION_ACTIVITY_INTENT_FILTER)
                .navigate()
        }

        binding.tvLoginForgotPassword.setOnClickListener {
            val onCancelFunc:() -> Unit = {
                forgotPasswordBSDialog?.dismiss()
                forgotPasswordBSDialog = null
            }

            forgotPasswordBSDialog = ForgotPasswordBSDialog.newInstance(
                onCancelFunc = onCancelFunc,
                onSubmitFunc = viewModel::handleForgotPassword
            )
            forgotPasswordBSDialog!!.show(supportFragmentManager, "")
        }

        binding.btnLoginLogin.setOnClickListener {
            closeKeyboard()

            val email:String = binding.etLoginEmail.properText
            val password:String = binding.etLoginPassword.properText
            if(!check(email, password))
                return@setOnClickListener

            viewModel.login(email,password)
        }

    }

    private fun check(email:String, password:String):Boolean{
        var status = true

        if(TextUtils.isEmpty(email)){
            displayMessage("please enter your email", binding.root)
            status = false
        } else if(TextUtils.isEmpty(password)){
            displayMessage("please enter your password", binding.root)
            status = false
        } else if(!email.contains("@")){
            displayMessage("please enter a valid email address", binding.root)
            status = false
        }

        if(!status)
            viewModel.vibrate(INPUT_ERROR)

        return status
    }
}