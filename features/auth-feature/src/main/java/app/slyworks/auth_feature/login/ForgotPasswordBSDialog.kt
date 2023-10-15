package app.slyworks.auth_feature.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import app.slyworks.auth_feature.R
import app.slyworks.auth_feature.databinding.BottomsheetForgotPasswordBinding
import app.slyworks.base_feature.BaseBottomSheetDialogFragment
import app.slyworks.utils_lib.utils.displayMessage
import app.slyworks.utils_lib.utils.properText


/**
 * Created by Joshua Sylvanus, 7:11 PM, 1/4/2022.
 */

class ForgotPasswordBSDialog(private val onCancelFunc: () -> Unit,
                             private val onSubmitFunc: (String) -> Unit)
    : BaseBottomSheetDialogFragment() {

    private lateinit var binding: BottomsheetForgotPasswordBinding

    companion object {
        @JvmStatic
        fun newInstance(onCancelFunc:() -> Unit,
                        onSubmitFunc:(String) -> Unit): ForgotPasswordBSDialog{
            return ForgotPasswordBSDialog(onCancelFunc, onSubmitFunc)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = BottomsheetForgotPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    private fun initViews(){
        binding.ivcancelBSFP.setOnClickListener {
            onCancelFunc.invoke()
        }

        binding.btnBSFPResend.setOnClickListener {
            val email:String = binding.etBSFPEmail.properText
            if(email.isEmpty()){
                displayMessage("please enter email used during registration", requireContext())
                return@setOnClickListener
            }

            onSubmitFunc.invoke(email)
        }
    }

}