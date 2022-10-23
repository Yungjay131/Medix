package com.slyworks.medix.ui.activities.registration_activity

import android.Manifest
import android.animation.ObjectAnimator
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.text.TextWatcher
import android.text.method.KeyListener
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textview.MaterialTextView
import com.slyworks.constants.COORDINATOR
import com.slyworks.medix.*
import com.slyworks.medix.ui.dialogs.ChangePhotoDialog
import com.slyworks.medix.ui.activities.BaseActivity
import com.slyworks.medix.ui.activities.login_activity.LoginActivity
import com.slyworks.medix.ui.custom_views.NetworkStatusView
import com.slyworks.medix.ui.dialogs.TermsAndConditionsBSDialog
import com.slyworks.medix.utils.*
import com.slyworks.medix.utils.ViewUtils.displayImage
import com.slyworks.medix.utils.ViewUtils.setChildViewsStatus
import com.slyworks.models.models.AccountType
import com.slyworks.models.models.Gender
import com.slyworks.models.models.Outcome
import com.slyworks.models.models.TempUserDetails
import com.slyworks.utils.ContentResolverStore
import de.hdodenhof.circleimageview.CircleImageView
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

const val EXTRA_IS_ACTIVITY_RECREATED = "com.slyworks.medix.ui.EXTRA_IS_ACTIVITY_RECREATED"
class RegistrationDoctorActivity : BaseActivity() {
    //region Vars
    private lateinit var ivProfile: CircleImageView
    private lateinit var etFirstName: EditText
    private lateinit var etLastName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var rbMale: RadioButton
    private lateinit var rbFemale: RadioButton
    private lateinit var etAge: EditText
    private lateinit var container_specialization: ConstraintLayout
    private lateinit var btnAddSpecialization: Button
    private lateinit var tvTandC: MaterialTextView
    private lateinit var cbAgree: CheckBox
    private lateinit var btnSignUp: Button

    private lateinit var viewAnchor: View
    private lateinit var progress:ProgressBar

    private lateinit var rootView:CoordinatorLayout

    private var networkStatusView: NetworkStatusView? = null

    private var mCreatedLayoutsList:MutableList<View> = mutableListOf()
    private val FALLBACK_ID:Int = R.id._view_horizontal_1
    private var mLastCreatedViewId:Int = FALLBACK_ID

    private var mIsThereCreatedLayout:Boolean = false

    private var disposables:CompositeDisposable = CompositeDisposable()

    private lateinit var mImageUri: Uri
    private var mHasImageBeenSelected = false

    @Inject
    lateinit var mViewModel:RegistrationDoctorActivityViewModel

    private var mEditTextMap:MutableMap<EditText, TextWatcher> = mutableMapOf()

    private lateinit var  etFirstNameWatcher:TextWatcher
    private lateinit var  etLastNameWatcher:TextWatcher
    private lateinit var  etEmailWatcher:TextWatcher
    private lateinit var etPasswordWatcher :TextWatcher
    private lateinit var etConfirmPasswordWatcher : TextWatcher
    //endregion

    override fun isValid(): Boolean = false

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(EXTRA_IS_ACTIVITY_RECREATED, true)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        ContentResolverStore.nullifyContentResolver()

        disposables.clear()

        etFirstName.removeTextChangedListener(etFirstNameWatcher)
        etFirstName.removeTextChangedListener(etLastNameWatcher)
        etEmail.removeTextChangedListener(etEmailWatcher)
        etPassword.removeTextChangedListener(etPasswordWatcher)
        etConfirmPassword.removeTextChangedListener(etConfirmPasswordWatcher)

        for(i in 0 until mEditTextMap.size){
            with(mEditTextMap){
                keys.forEach {
                    it.removeTextChangedListener(get(it))
                    remove(it)
                }
            }
        }

        super.onDestroy()
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

    private fun closeKeyboard() =
        this.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)


    override fun onCreate(savedInstanceState: Bundle?) {
        initDI()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration_doctor)

        initData()
        initPermissions()
        initViews()

        if(savedInstanceState != null)
            initViews2()
    }

    private fun initDI(){
        application.appComponent
            .activityComponentBuilder()
            .setActivity(this)
            .build()
            .inject(this)
    }

    private fun initData(){
        ContentResolverStore.setContentResolver(contentResolver)

        this.onBackPressedDispatcher
            .addCallback(this, MOnBackPressedCallback(this))

        mViewModel.profileImageUriLiveData.observe(this){
            it!!
            mViewModel.ivProfileUriVal = it
            setProfileImage(it)
        }
        mViewModel.registrationStatusLiveDetails.observe(this){
            toggleLoadingStatus(false)
            when{
                 it.isSuccess -> {
                     lifecycleScope.launch {
                         displayMessage("registration successful, please verify your email and login ")
                         delay(1_000)

                         val intent = Intent(this@RegistrationDoctorActivity, LoginActivity::class.java)
                         intent.setFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP)
                         startActivity(intent)
                         finish()
                     }
                 }
                 it.isFailure ||
                 it.isError -> displayMessage("oops! Something went wrong on our end, please try again")
             }
        }
    }

    private fun initPermissions(){
        /*has to be done in onCreate()*/
        mViewModel.permissionManager
            .initialize(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA)
    }

    private fun initViews2(){
        mViewModel.ivProfileUriVal?.let {
           ivProfile.displayImage(mViewModel.ivProfileUriVal!!)
        }

        etFirstName.setText(mViewModel.etFirstNameVal)
        etLastName.setText(mViewModel.etLastNameVal)
        etEmail.setText(mViewModel.etEmailVal)
        etPassword.setText(mViewModel.etPasswordVal)
        etConfirmPassword.setText(mViewModel.etConfirmPasswordVal)
        rbMale.setChecked(mViewModel.rbMaleVal)
        rbFemale.setChecked(mViewModel.rbFemaleVal)
        cbAgree.setChecked(mViewModel.cbAgreeVal)

        if(mViewModel.specializationList.isNotEmpty()){
            mViewModel.specializationList.forEach {
                addLayout(it.value)
                mViewModel.specializationList.remove(it.key)
            }
        }
    }

    private fun initViews(){
        rootView = findViewById(R.id.rootView)

        ivProfile = findViewById(R.id.ivProfile_doctor_reg_one)
        etFirstName = findViewById(R.id.etFirstlName_doctor_reg_one)
        etLastName = findViewById(R.id.etLastName_doctor_reg_one)
        etEmail = findViewById(R.id.etEmail_doctor)
        etPassword = findViewById(R.id.etPassword_doctor)
        etConfirmPassword = findViewById(R.id.etConfirmPassword_doctor)
        rbMale = findViewById(R.id.rbMale_doctor)
        rbFemale = findViewById(R.id.rbFemale_doctor)
        etAge = findViewById(R.id.etAge_doctor)
        container_specialization = findViewById(R.id.container_specialization_doctor_reg_one)
        btnAddSpecialization = findViewById(R.id.btnAddSpecialization)
        tvTandC = findViewById(R.id.tvTandC_doctor_reg_one)
        cbAgree = findViewById(R.id.cbTandC)

        progress = findViewById(R.id.progress_layout)
        viewAnchor = findViewById(R.id._view_horizontal_1)

        btnSignUp = findViewById(R.id.btnSignUp_doctor_reg)


        cbAgree.setOnCheckedChangeListener{ _:CompoundButton, b:Boolean ->
            btnSignUp.isEnabled = b
            mViewModel.cbAgreeVal = b
        }

        rbMale.setOnCheckedChangeListener { _, b ->
            if(b && rbFemale.isChecked)
                rbFemale.isChecked = false

            mViewModel.rbMaleVal = b
        }

        rbFemale.setOnCheckedChangeListener { _, b ->
            if(b && rbMale.isChecked)
                rbMale.isChecked = false

            mViewModel.rbFemaleVal = b
        }

        ivProfile.setOnClickListener {
            disposables +=
                mViewModel.permissionManager
                    .requestPermissions()
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { o: Outcome ->
                        when{
                            o.isSuccess -> {
                                ChangePhotoDialog.getInstance().apply {
                                    mViewModel.handleProfileImageUri(this.getObservable())
                                    }
                                    .show(supportFragmentManager, "")
                            }
                            o.isFailure -> {
                                val l:List<String> = o.getTypedValue<List<String>>()
                                showMessage("Medix requires these permissions to work well", rootView)
                            }
                        }
                    }

        }

        btnAddSpecialization.setOnClickListener{
            if (mCreatedLayoutsList.isNotEmpty() ){
                val textView:EditText = mCreatedLayoutsList.last()
                    .findViewById(R.id.etText_layout_text)

                if(TextUtils.isEmpty(textView.text)){
                    displayMessage("please fill existing text fields before adding another")
                    return@setOnClickListener
                }

                addLayout()
                return@setOnClickListener
            }

            //assuming no layout has been added before now
            addLayout()
        }

        tvTandC.setOnClickListener {
          TermsAndConditionsBSDialog.getInstance()
              .show(supportFragmentManager, "")
        }


        btnSignUp.setOnClickListener {
            if(!mViewModel.getNetworkStatus()){
                displayMessage("Please check your connection and try again")
                return@setOnClickListener
            }

            if(!check())
                return@setOnClickListener

            toggleLoadingStatus(true)

            mViewModel.register(parseUserDetails())
        }

        btnSignUp.setOnLongClickListener{
            simulateFillingDetails();
            true
        }

        etFirstNameWatcher = TextWatcherImpl {
            mViewModel.etFirstNameVal = it
        }
        etLastNameWatcher = TextWatcherImpl {
            mViewModel.etLastNameVal = it
        }
        etEmailWatcher = TextWatcherImpl {
            mViewModel.etEmailVal = it
        }
        etPasswordWatcher = TextWatcherImpl {
            mViewModel.etPasswordVal = it
        }
        etConfirmPasswordWatcher = TextWatcherImpl {
            mViewModel.etConfirmPasswordVal = it
        }
    }

    private fun simulateFillingDetails(){
        etFirstName.setText("Josh")
        etLastName.setText("Sylvanus")
        etEmail.setText("talk2joshuasylvanus@gmail.com")
        etPassword.setText("password")
        etConfirmPassword.setText("password")
        etAge.setText("19")
        rbMale.setChecked(true)
        cbAgree.setChecked(true)
    }

    private fun parseUserDetails(): TempUserDetails {
        val accountType = AccountType.DOCTOR
        val firstName = etFirstName.text.toString().trim()
        val lastName = etLastName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val sex: Gender = if(rbMale.isChecked) Gender.MALE else Gender.FEMALE
        val age = etAge.text.toString().trim()
        val password = etPassword.text.toString().trim()
        var firebaseUID:String? = null
        var agoraUID:String? = null
        val imageUri_init:Uri = mImageUri
        val imageUri:String? = null

        val _specialization:MutableList<String> = parseUserSpecialization()
        val specializaiton:MutableList<String>? = if(_specialization.isNotEmpty()) _specialization else null

        return TempUserDetails(
            accountType,
            firstName,
            lastName,
            email,
            sex,
            age,
            password,
            null,
            null,
            null,
            imageUri_init,
            null,
            null,
            specializaiton
        )
    }

    private fun parseUserSpecialization():MutableList<String>{
        val list = mutableListOf<String>()
        if(mCreatedLayoutsList.isNotEmpty()){
            mCreatedLayoutsList.forEach { layout ->
                list.add(
                    layout.findViewById<EditText>(R.id.etText_layout_text)
                        .text.toString().trim() )
            }
        }

        return mutableListOf<String>().plus(list) as MutableList<String>
    }

    private fun check():Boolean{
        var status = true

        val firstName = etFirstName.text.toString().trim()
        val lastName = etLastName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val confirmPassword = etConfirmPassword.text.toString().trim()

        if(TextUtils.isEmpty(firstName)){
            displayMessage("please enter a first name")
            status = false
        }
        else if(TextUtils.isEmpty(lastName)){
            displayMessage("please enter a last name")
            status = false
        }
        else if(TextUtils.isEmpty(email)){
            displayMessage("please enter an email address")
            status = false
        }
        else if(TextUtils.isEmpty(password)){
            displayMessage("please enter a password")
            status = false
        }
        else if(TextUtils.isEmpty(confirmPassword)){
            displayMessage("please re-enter your password")
            status = false
        }
        else if(!email.contains("@") || !email.contains(".com")){
            displayMessage("please use a valid email address")
            status = false
        }
        else if(password.length < 8){
            displayMessage("password length should be at least 8 characters")
            status = false
        }
        else if(password != confirmPassword){
            displayMessage("passwords do not match")
            status = false
        }
        else if(!rbMale.isChecked && !rbFemale.isChecked){
            displayMessage("please select a gender")
            status = false
        }else if(!TextUtils.isDigitsOnly(etAge.text.toString()) ){
            displayMessage("please enter a valid number")
            status = false
        }
        else if(etAge.text.toString().toInt() < 18){
            displayMessage("you should be at least 18 years old to use this service")
            status = false
        }
        else if(!mHasImageBeenSelected){
            displayMessage("please a profile image is required")
            status = false
        }
        else if(!cbAgree.isChecked){
            displayMessage("please agree to the Terms and Conditions to use this platform")
            status = false
        }else if(mIsThereCreatedLayout){
            mCreatedLayoutsList.forEach { view ->
                val editText:EditText = view.findViewById(R.id.etText_layout_text)
                if(TextUtils.isEmpty(editText.text)){
                    displayMessage("please fill in all added history texts or remove")
                    status = false
                }
            }
        }

        return status
    }


    private fun addLayout(onRecreateEtText:String = "") {
        val inflater: LayoutInflater = LayoutInflater.from(this)

        val layout: View = inflater.inflate(R.layout.layout_text, container_specialization, false)
        layout.setId(View.generateViewId())

        val constraintSet: ConstraintSet = ConstraintSet()
        constraintSet.clone(container_specialization)

        val layoutParams: ViewGroup.LayoutParams = layout.layoutParams
        layoutParams.height = 55

        val editText:EditText = layout.findViewById(R.id.etText_layout_text)
        editText.setText(onRecreateEtText)

        val ivCancel: ImageView = layout.findViewById(R.id.ivCancel)
        ivCancel.setOnClickListener { removeCreatedView(layout) }

        container_specialization.addView(layout)

        constraintSet.connect(layout.id, ConstraintSet.START, container_specialization.id, ConstraintSet.START)
        constraintSet.connect(layout.id, ConstraintSet.END, container_specialization.id, ConstraintSet.END)
        constraintSet.connect(
            layout.id,
            ConstraintSet.TOP,
            getAppropriateId(),
            ConstraintSet.BOTTOM,
            resources.getDimensionPixelSize(R.dimen.layout_size_margin4)
        )

        constraintSet.constrainWidth(layout.id, ConstraintSet.MATCH_CONSTRAINT)
        constraintSet.constrainHeight(layout.id, ConstraintSet.WRAP_CONTENT)

        constraintSet.connect(btnAddSpecialization.id, ConstraintSet.START, container_specialization.id, ConstraintSet.START)
        constraintSet.connect(btnAddSpecialization.id, ConstraintSet.END, container_specialization.id, ConstraintSet.END)
        constraintSet.connect(
            btnAddSpecialization.id,
            ConstraintSet.TOP,
            layout.id,
            ConstraintSet.BOTTOM,
            resources.getDimensionPixelSize(R.dimen.layout_size_margin2)
        )

        constraintSet.applyTo(container_specialization)
        layout.alpha = 0F
        val objectAnimator:ObjectAnimator = ObjectAnimator.ofFloat(
            layout,
            "alpha",
            0F,
            1F)
        objectAnimator.duration = 1_500
        objectAnimator.start()

        mIsThereCreatedLayout = true

        setAppropriateId(layout.id)

        saveToViewModel(layout)
        saveView(layout)

        redrawViews()
    }

    private fun saveToViewModel(layout:View){
        val etText:EditText = layout.findViewById(R.id.etText_layout_text)

        mViewModel.addLayout(layout.id)

        val t:TextWatcher = TextWatcherImpl{
            mViewModel.updateValue(layout.id, it)
        }

        etText.addTextChangedListener(t)
        mEditTextMap.put(etText, t)
    }

    private fun saveView(layout:View) = mCreatedLayoutsList.add(layout)

    private fun removeFromViewModel(layout:View){
        val etText:EditText = layout.findViewById(R.id.etText_layout_text)

        val t:TextWatcher = mEditTextMap.get(etText) ?: return
        etText.removeTextChangedListener(t)

        mViewModel.removeLayout(etText.id)
    }

    private fun removeCreatedView(layout:View){
        removeFromViewModel(layout)

        container_specialization.removeView(layout)
        mCreatedLayoutsList.remove(layout)

        if(mCreatedLayoutsList.isEmpty())
            mIsThereCreatedLayout = false

        setAppropriateId(mCreatedLayoutsList.lastOrNull()?.id ?: FALLBACK_ID)
        realignButton()
        redrawViews()
    }

    private fun realignButton(){
        val constraintSet: ConstraintSet = ConstraintSet()
        constraintSet.clone(container_specialization)

        constraintSet.connect(btnAddSpecialization.id, ConstraintSet.START, container_specialization.id, ConstraintSet.START)
        constraintSet.connect(btnAddSpecialization.id, ConstraintSet.END, container_specialization.id, ConstraintSet.END)
        constraintSet.connect(
            btnAddSpecialization.id,
            ConstraintSet.TOP,
            getAppropriateId(),
            ConstraintSet.BOTTOM,
            resources.getDimensionPixelSize(R.dimen.layout_size_margin2)
        )

        constraintSet.applyTo(container_specialization)
    }

    private fun redrawViews(){
        if(mCreatedLayoutsList.isEmpty()) return

        mCreatedLayoutsList.forEach {constraintLayout ->
            val editText:EditText = constraintLayout.findViewById(R.id.etText_layout_text)
            val imageView:ImageView = constraintLayout.findViewById(R.id.ivCancel)

            toggleEditState(editText, false)
            imageView.visibility = View.INVISIBLE
            if(constraintLayout == mCreatedLayoutsList.last()){
                toggleEditState(editText, true)
                imageView.visibility = View.VISIBLE
            }
        }

    }

    private fun toggleEditState(editText: EditText, status:Boolean){
        if (status) {
            editText.keyListener = editText.tag as KeyListener
        }else{
            if(editText.keyListener == null) return

            editText.tag = editText.keyListener
            editText.keyListener = null
        }
    }

    private fun setAppropriateId(id:Int){
        mLastCreatedViewId = id
    }

    private fun getAppropriateId():Int = mLastCreatedViewId


    private fun setProfileImage(uri:Uri){
        ivProfile.displayImage(uri)

        mImageUri = uri
        mHasImageBeenSelected = true

        mViewModel.setProfileImageURI(uri)
    }
    private fun toggleLoadingStatus(status: Boolean){
            progress.isVisible = status
            rootView.setChildViewsStatus(!status)
    }

    private fun displayMessage(message:String){
        Snackbar.make(findViewById(R.id.rootView), message, Snackbar.LENGTH_SHORT).show();
    }




}