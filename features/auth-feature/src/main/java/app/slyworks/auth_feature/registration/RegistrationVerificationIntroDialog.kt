package app.slyworks.auth_feature.registration

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import app.slyworks.auth_feature.databinding.DialogRegistrationVerificationIntroBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class RegistrationVerificationIntroDialog() : DialogFragment() {
    //region Vars
    private lateinit var binding:DialogRegistrationVerificationIntroBinding
    //endregion

    override fun isCancelable(): Boolean  = false

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext(), theme).apply {
            val dialogView = onCreateView(LayoutInflater.from(requireContext()),null, savedInstanceState)
            dialogView?.let {
                onViewCreated(it,savedInstanceState)
            }
            setView(dialogView)
        }.create()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DialogRegistrationVerificationIntroBinding.inflate(requireActivity().layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
    }

    private fun initViews(view:View){
       binding.btnContinue.setOnClickListener {
           dismiss()
       }
    }
}
