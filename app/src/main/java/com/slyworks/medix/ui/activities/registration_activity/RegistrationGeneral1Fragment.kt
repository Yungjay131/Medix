package com.slyworks.medix.ui.activities.registration_activity

import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jakewharton.rxbinding4.widget.textChanges
import com.slyworks.medix.databinding.FragmentRegistrationGeneral1Binding
import com.slyworks.medix.utils.ViewUtils.closeKeyboard3
import com.slyworks.medix.utils.ViewUtils.displayMessage
import com.slyworks.medix.utils.ViewUtils.displaySnackBar
import com.slyworks.medix.utils.plusAssign
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable

class RegistrationGeneral1Fragment : Fragment() {
    //region Vars
    private lateinit var binding:FragmentRegistrationGeneral1Binding
    private lateinit var registrationViewModel: RegistrationActivityViewModel

    private val disposables:CompositeDisposable = CompositeDisposable()
    //endregion

    companion object {
        @JvmStatic
        fun newInstance():RegistrationGeneral1Fragment =
            RegistrationGeneral1Fragment()
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.clear()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        registrationViewModel = (context as Registration2Activity).registrationViewModel
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentRegistrationGeneral1Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
    }

    private fun initViews(){
        disposables +=
        Observable.combineLatest(
            binding.etEmail.textChanges(),
            binding.etPassword.textChanges(),
            binding.etConfirmPassword.textChanges(),
            { email, password, confirmPassword ->
               email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty()
            })
            .subscribe(binding.btnNext::setEnabled)

        binding.btnNext.setOnClickListener {
            requireActivity().closeKeyboard3()

            val email:String = binding.etEmail.text.toString().trim()
            val password:String = binding.etPassword.text.toString().trim()
            val confirmPassword:String = binding.etConfirmPassword.text.toString().trim()
            if(!check(email,password,confirmPassword))
                return@setOnClickListener

            val u = registrationViewModel.getUserDetails().copy(email = email, password = password)
            registrationViewModel.setUserDetails(u)

            (requireActivity() as Registration2Activity).navigator
                .hideCurrent()
                .show(RegistrationGeneral2Fragment.newInstance())
                .navigate()
        }
    }

    fun check(email:String, password:String, confirmPassword:String):Boolean{
        var result = true

        if(TextUtils.isEmpty(email)){
            displayMessage("please enter your email", binding.root)
            result = false
        } else if(TextUtils.isEmpty(password)){
            displayMessage("please enter your password", binding.root)
            result = false
        } else if(TextUtils.isEmpty(confirmPassword)){
            displayMessage("please repeat the entered password", binding.root)
            result = false
        }else if(!email.contains("@")){
            displayMessage("please enter a valid email address", binding.root)
            result = false
        }else if(password != email){
            displayMessage("Passwords do not match, please check and try again", binding.root)
            result = false
        }

        return result
    }
}