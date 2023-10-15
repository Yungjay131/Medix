package app.slyworks.auth_feature.registration

import android.os.Bundle
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.VisibleForTesting
import app.slyworks.auth_feature.databinding.FragmentRegistrationGeneral1Binding
import app.slyworks.utils_lib.utils.closeKeyboard
import app.slyworks.utils_lib.utils.displayMessage
import app.slyworks.utils_lib.utils.properText
import dev.joshuasylvanus.navigator.interfaces.FragmentContinuationStateful
import java.lang.reflect.Modifier

class RegistrationGeneral1Fragment : Fragment() {
    //region Vars
    private lateinit var navigator: FragmentContinuationStateful
    private lateinit var viewModel: RegistrationActivityViewModel

    private lateinit var binding: FragmentRegistrationGeneral1Binding
    //endregion

    companion object {
        @JvmStatic
        fun newInstance(): RegistrationGeneral1Fragment =
            RegistrationGeneral1Fragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentRegistrationGeneral1Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initData()
        initViews()
    }

    private fun initData(){
       navigator = (requireActivity() as RegistrationActivity).navigator
       viewModel = (requireActivity() as RegistrationActivity).viewModel
    }

    private fun initViews(){
        binding.etConfirmPassword.setOnEditorActionListener { textView, i, keyEvent ->
            requireActivity().closeKeyboard()

            val email:String = binding.etEmail.properText
            val password:String = binding.etPassword.properText
            val confirmPassword:String = binding.etConfirmPassword.properText
            if(!check(email,password,confirmPassword))
                return@setOnEditorActionListener true

            viewModel.setEmailAndPassword(email, password)

            navigator.show(RegistrationGeneral2Fragment.newInstance())
                .navigate()

            return@setOnEditorActionListener true
        }

        binding.btnNext.setOnClickListener {
            requireActivity().closeKeyboard()

            val email:String = binding.etEmail.properText
            val password:String = binding.etPassword.properText
            val confirmPassword:String = binding.etConfirmPassword.properText
            if(!check(email,password,confirmPassword))
                return@setOnClickListener

            viewModel.setEmailAndPassword(email, password)

            navigator.show(RegistrationGeneral2Fragment.newInstance())
                .navigate()
        }
    }

    @VisibleForTesting(otherwise = Modifier.PRIVATE)
    internal fun check(email:String, password:String, confirmPassword:String):Boolean{
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
        }else if(password.length < 8){
            displayMessage("Password should be a minimum of 8 characters", binding.root)
            result = false
        }else if(!password.contains("[A-Z]".toRegex())){
            displayMessage("Password should contain at least 1 uppercase letter", binding.root)
            result = false
        }else if(!password.contains("[0-9]".toRegex())){
            displayMessage("Password should contain at least 1 number", binding.root)
            result = false
        }else if(!password.contains("[@#\$%^&+=.]".toRegex())){
            displayMessage("Password should contain at least 1 special character(&,%,#,@,$, e.t.c)", binding.root)
            result = false
        }else if(!TextUtils.equals(password, confirmPassword)){
            displayMessage("Passwords do not match, please check and try again", binding.root)
            result = false
        }

        return result
    }
}