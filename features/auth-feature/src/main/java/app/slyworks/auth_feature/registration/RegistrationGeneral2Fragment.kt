 package app.slyworks.auth_feature.registration

import android.Manifest
import android.app.DatePickerDialog
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import app.slyworks.auth_feature.databinding.FragmentRegistrationGeneral2Binding
import app.slyworks.base_feature.PermissionManager
import app.slyworks.base_feature.PermissionStatus
import app.slyworks.base_feature.ui.ChangePhotoDialog
import app.slyworks.data_lib.model.models.AccountType
import app.slyworks.data_lib.model.models.Gender
import app.slyworks.utils_lib.Outcome
import app.slyworks.utils_lib.utils.*
import dev.joshuasylvanus.navigator.interfaces.FragmentContinuationStateful
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

 class RegistrationGeneral2Fragment : Fragment() {
    //region Vars
     private var dob:String = ""

     private var imageUri:Uri? = null
     private var gender: Gender = Gender.NOT_SET

    private val disposables = CompositeDisposable()

     private lateinit var navigator: FragmentContinuationStateful
     private lateinit var viewModel: RegistrationActivityViewModel

     private lateinit var binding: FragmentRegistrationGeneral2Binding

    @Inject
    lateinit var permissionManager:PermissionManager
   //endregion

    companion object {
        @JvmStatic
        fun newInstance(): RegistrationGeneral2Fragment = RegistrationGeneral2Fragment()
    }

     override fun onAttach(context: Context) {
         super.onAttach(context)

         //TODO:inject PermissionManager here
     }

     override fun onDestroy() {
         super.onDestroy()
         disposables.clear()
     }

     override fun onCreate(savedInstanceState: Bundle?) {
         super.onCreate(savedInstanceState)

         initPermissions()
     }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentRegistrationGeneral2Binding.inflate(layoutInflater, container, false)
        return binding.root
    }

     override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
         super.onViewCreated(view, savedInstanceState)

         initData()
         initViews()
     }

     private fun initPermissions(){
         /*has to be done in onCreate()*/
         permissionManager.initialize(
                 this,
                 Manifest.permission.WRITE_EXTERNAL_STORAGE,
                 Manifest.permission.CAMERA)
     }

     private fun initData(){
         navigator = (requireActivity() as RegistrationActivity).navigator
         viewModel = (requireActivity() as RegistrationActivity).viewModel

         disposables +=
         permissionManager
             .getPermissionsObservable()
             .subscribeOn(AndroidSchedulers.mainThread())
             .observeOn(AndroidSchedulers.mainThread())
             .subscribe{ o: Outcome ->
                 when{
                     o.isSuccess -> {
                         val dialog:ChangePhotoDialog = ChangePhotoDialog.newInstance()

                         disposables +=
                         dialog.getObservable()
                             .subscribeOn(Schedulers.io())
                             .observeOn(AndroidSchedulers.mainThread())
                             .subscribe {
                                 this@RegistrationGeneral2Fragment.imageUri = it

                                 binding.ivProfile.displayImage(it)
                             }

                         dialog.show(requireActivity().supportFragmentManager, "")
                     }

                     o.isFailure -> {
                         val l:List<PermissionStatus> = o.getTypedValue<List<PermissionStatus>>()
                         displayMessage("Medix requires these permissions to work", binding.root)
                     }
                 }
             }
     }

     private fun initViews(){
         val genders:MutableList<String> = mutableListOf("Male", "Female")
         val genderSpinnerAdapter:ArrayAdapter<String> =
             ArrayAdapter<String>(
                 requireContext(),
                 android.R.layout.simple_spinner_dropdown_item,
                 genders
             )
         genderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
         binding.spinnerGender.setAdapter(genderSpinnerAdapter)
         binding.spinnerGender.setOnItemSelectedListener(
             object : AdapterView.OnItemSelectedListener{
                 override fun onNothingSelected(p0: AdapterView<*>?) {}
                 override fun onItemSelected(p0: AdapterView<*>?, view: View?, position: Int, p3: Long) {
                   if(position == 0)
                       gender = Gender.MALE
                   else if(position == 1)
                       gender = Gender.FEMALE
                 }

             })

         binding.etLastName.setOnEditorActionListener { textView, i, keyEvent ->
             requireActivity().closeKeyboard()

             binding.datePicker.requestFocus()
             return@setOnEditorActionListener true
         }

         binding.datePicker.setOnClickListener{
             val calendar: Calendar = SimpleDateFormat.getDateInstance().calendar
             val year:Int = calendar.get(Calendar.YEAR)
             val month:Int = calendar.get(Calendar.MONTH)
             val day:Int = calendar.get(Calendar.DAY_OF_MONTH)

             val datePickerDialog = DatePickerDialog(requireContext(),
                 DatePickerDialog.OnDateSetListener { datePicker, yr, mnth, dayOfMonth ->
                     dob = "${day}/${mnth + 1}/${yr}"
                     binding.datePicker.setText(dob)
                 }, year, month, day)

             datePickerDialog.updateDate(2002, 1, 1)
             datePickerDialog.show()
         }

         binding.ivProfile.setOnClickListener {
             permissionManager.requestPermissions()
         }

         binding.btnNext.setOnClickListener {
             requireActivity().closeKeyboard()

             val firstName:String = binding.etFirstName.properText
             val lastName:String = binding.etLastName.properText
             if(!check(firstName, lastName))
                 return@setOnClickListener


             viewModel.setNameDOBAndSex(firstName, lastName, dob, gender)
             val f:Fragment =
                 if(viewModel.getAccountType() == AccountType.PATIENT)
                     RegistrationPatientFragment.newInstance()
                 else
                     RegistrationDoctorFragment.newInstance()

             navigator.show(f)
                 .navigate()
         }
     }

     private fun check(firstName:String, lastName:String):Boolean {
         var status = true

         if(TextUtils.isEmpty(firstName)){
             displayMessage("please enter a first name", binding.root)
             status = false
         } else if(TextUtils.isEmpty(lastName)){
             displayMessage("please enter a last name", binding.root)
             status = false
         } else if(imageUri == null){
             displayMessage("a profile image is required", binding.root)
             status = false
         } else if(dob == ""){
             displayMessage("please enter your date of birth", binding.root)
             status = false
         } else if(gender == Gender.NOT_SET){
             displayMessage("please select your gender", binding.root)
             status = false
         }

         return status
     }

}