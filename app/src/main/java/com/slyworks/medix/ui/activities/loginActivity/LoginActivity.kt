package com.slyworks.medix.ui.activities.loginActivity

import android.app.PendingIntent.getActivity
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.text.TextUtils
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.viewModels
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textview.MaterialTextView
import com.jakewharton.rxbinding4.InitialValueObservable
import com.jakewharton.rxbinding4.widget.textChanges
import com.slyworks.constants.*
import com.slyworks.medix.*
import com.slyworks.medix.ui.activities.BaseActivity
import com.slyworks.medix.ui.activities.mainActivity.MainActivity
import com.slyworks.medix.ui.activities.registrationActivity.EXTRA_IS_ACTIVITY_RECREATED
import com.slyworks.medix.ui.activities.registrationActivity.RegistrationActivity
import com.slyworks.medix.ui.activities.registrationActivity.TextWatcherImpl
import com.slyworks.medix.ui.custom_views.NetworkStatusView
import com.slyworks.medix.ui.dialogs.ForgotPasswordBSDialog
import com.slyworks.medix.utils.*
import com.slyworks.medix.utils.ViewUtils.setChildViewsStatus
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LoginActivity : BaseActivity() {
    //region Vars
    private lateinit var etEmail:EditText
    private lateinit var etPassword: EditText

    private lateinit var tvRegister:MaterialTextView
    private lateinit var tvForgotPassword:MaterialTextView

    private lateinit var btnLogin: Button
    private lateinit var progress:ProgressBar
    private lateinit var rootView:CoordinatorLayout

    private var networkStatusView:NetworkStatusView? = null

    private var etEmailTextWatcher:TextWatcherImpl? = null
    private var etPasswordTextWatcher:TextWatcherImpl? = null

    private val mSubscriptions:CompositeDisposable = CompositeDisposable()

    private var mDestination:Class<*> = MainActivity::class.java

    private var mMediaPlayer: MediaPlayer? = null

    private var forgotPasswordBSDialog:ForgotPasswordBSDialog? = null

    private val mViewModel: LoginActivityViewModel by viewModels()
    //endregion


    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(EXTRA_IS_ACTIVITY_RECREATED, true)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()
        mMediaPlayer?.release()

        etEmail.removeTextChangedListener(etEmailTextWatcher)
        etEmailTextWatcher = null

        etPassword.removeTextChangedListener(etPasswordTextWatcher)
        etPasswordTextWatcher = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        initData()
        initViews()
        initRx()
        initMediaPlayer()

        if(savedInstanceState != null)
            initViews2()
    }

    private fun initData(){
        if(intent.getBundleExtra(EXTRA_ACTIVITY) != null)
            mDestination = ActivityUtils.from(intent.getStringExtra(EXTRA_LOGIN_DESTINATION)!!)

        this.onBackPressedDispatcher
            .addCallback(this, MOnBackPressedCallback(this))

        mViewModel.passwordResetLiveData.observe(this){
            if(it)
                displayMessage("a password reset email has been sent, please check your inbox")
            else
                displayMessage("oh oh something went  wrong on our end, please try again later")
        }

        mViewModel.loginStatusLiveData.observe(this){
            toggleLoadingStatus(false)
            when{
                it.isSuccess ->{
                    setMediaPlayerStatus()

                    val intent = Intent(this@LoginActivity, mDestination)
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                    finish()
                }
                it.isFailure ||
                it.isError -> displayMessage(it.getValue() as String)
            }
        }
    }

    private fun initMediaPlayer(){
        if(mMediaPlayer == null){
            mMediaPlayer = MediaPlayer.create(App.getContext(),
                R.raw.positive_button_sound)
        }

    }

    override fun onStart() {
        super.onStart()

        mViewModel.subscribeToNetwork().observe(this) {
            if(networkStatusView == null)
                networkStatusView = NetworkStatusView.from(rootView, COORDINATOR)

            networkStatusView!!.setVisibilityStatus(it)
        }
    }

    override fun onStop() {
        super.onStop()

        mViewModel.unsubscribeToNetwork()
    }


    override fun onResume() {
        super.onResume()
        closeKeyboard()
    }

    private fun closeKeyboard3(){
        val inputManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.hideSoftInputFromWindow(currentFocus?.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS)
    }
    private fun closeKeyboard2(){
        //to show soft keyboard
        val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }
    private fun closeKeyboard(){
        this.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
    }

    private fun initViews(){
        rootView = findViewById(R.id.rootView)
        etEmail = findViewById(R.id.etLoginEmail)
        etPassword = findViewById(R.id.etLoginPassword)
        tvRegister = findViewById(R.id.tvLoginRegister_2)
        tvForgotPassword = findViewById(R.id.tvLoginForgotPassword)
        btnLogin = findViewById(R.id.btnLoginLogin)

        tvRegister.setOnClickListener {
            val intent = Intent(this@LoginActivity, RegistrationActivity::class.java)
            startActivity(intent)
        }

        tvForgotPassword.setOnClickListener {   initBottomSheetForgotPassword() }

        progress = findViewById(R.id.progress_layout)

        btnLogin.setOnClickListener {
            login(etEmail.text.toString().trim(),
                etPassword.text.toString().trim())
        }

        etEmailTextWatcher = TextWatcherImpl {
            mViewModel.emailVal = it
        }

        etPasswordTextWatcher = TextWatcherImpl {
            mViewModel.passwordVal = it
        }

        etEmail.addTextChangedListener(etEmailTextWatcher!!)
        etPassword.addTextChangedListener(etPasswordTextWatcher!!)

        etPassword.setOnEditorActionListener(
            object: TextView.OnEditorActionListener{
            override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
                if(v!!.id == etPassword.id){
                    closeKeyboard3()
                      //btnLogin.requestFocus()
                     login(etEmail.text.toString().trim(),
                           etPassword.text.toString().trim())
                    return true
                }

                return false
            }
        })
    }

    private fun initViews2(){
        etEmail.setText(mViewModel.emailVal)
        etPassword.setText(mViewModel.passwordVal)
    }

    private fun initRx(){
        val etEmailObservable:InitialValueObservable<CharSequence> = etEmail.textChanges()
        val etPasswordObservable:InitialValueObservable<CharSequence> = etPassword.textChanges()

        val etObservables: Observable<Boolean> =
            Observable
                .combineLatest(
                    etEmailObservable,
                    etPasswordObservable
                ) { eo1, eo2 ->
                    if (eo1.toString().isNotEmpty() && eo2.toString().isNotEmpty())
                        return@combineLatest true

                    return@combineLatest false
                }

        val d1:Disposable = etObservables
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { btnLogin.isEnabled = it }

        mSubscriptions.add(d1)
    }

    private fun initBottomSheetForgotPassword(){
       forgotPasswordBSDialog = ForgotPasswordBSDialog
                .getInstance(::_initBottomSheetForgotPassword)

        forgotPasswordBSDialog!!.show(supportFragmentManager, "")
    }

    private fun _initBottomSheetForgotPassword(view:View?){
        view ?: return
        val etEmailBSFP:EditText = view.findViewById(R.id.etBSFPEmail)
        val btnResendBSFP:Button = view.findViewById(R.id.btnBSFPResend)
        val ivCancel: ImageView = view.findViewById(R.id.ivcancelBSFP)
        val progressBSFP:CircularProgressIndicator = view.findViewById(R.id.progress_BSFP)

        ivCancel.setOnClickListener {
            forgotPasswordBSDialog?.dismiss()
            forgotPasswordBSDialog = null
        }

        btnResendBSFP.setOnClickListener {
            progressBSFP.visibility = View.VISIBLE

            val email = etEmailBSFP.text.toString().trim()

            if(!mViewModel.getNetworkStatus()){
                Toast.makeText(this@LoginActivity, "Please check your connection and try again", Toast.LENGTH_LONG)
                    .show()
                return@setOnClickListener
            }

            mViewModel.handleForgotPassword(email)
        }
    }


    private fun  setMediaPlayerStatus(){
        if (mMediaPlayer == null){
            initMediaPlayer()
        }

               mMediaPlayer?.let {
                   it.start()
                   it.setLooping(false)
               }

              lifecycleScope.launch {
                  delay(2_000)
                  mMediaPlayer!!.stop()
                  mMediaPlayer!!.release()
              }
    }

    private fun login(email:String, password:String){
        if(!mViewModel.getNetworkStatus()){
            displayMessage("Please check your connection and try again")
           return
        }

        if(!check(email, password)){
            VibrationManager.vibrate(INPUT_ERROR)
            return
        }

        toggleLoadingStatus(true)
        
        mViewModel.login(email,password)
    }


    private fun check(email:String, password:String):Boolean{
        var status = true

        if(TextUtils.isEmpty(email)){
            displayMessage("please enter your email")
            status = false
        } else if(TextUtils.isEmpty(password)){
            displayMessage("please enter your password")
            status = false
        } else if(!email.contains("@")){
            displayMessage("please enter a valid email address")
            status = false
        }

        return status
    }

    private fun toggleLoadingStatus(status: Boolean){
        progress.isVisible = status
        rootView.setChildViewsStatus(!status)
    }

    private fun displayMessage(view:View, message: String){
        Snackbar.make(view, message, Snackbar.LENGTH_LONG)
            .show()
    }
    private fun displayMessage(message:String){
        Snackbar.make(findViewById(R.id.rootView), message, Snackbar.LENGTH_LONG).apply {
            if(networkStatusView != null)
               anchorView = networkStatusView
        }
        .show();
    }
}