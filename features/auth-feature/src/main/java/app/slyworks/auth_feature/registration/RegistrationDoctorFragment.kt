package app.slyworks.auth_feature.registration

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.slyworks.auth_feature.databinding.FragmentRegistrationDoctorBinding

class RegistrationDoctorFragment : Fragment() {
    //region Vars
    private lateinit var binding: FragmentRegistrationDoctorBinding
    private lateinit var viewModel: RegistrationActivityViewModel
    //endregion

    companion object {
        @JvmStatic
        fun newInstance(): RegistrationDoctorFragment = RegistrationDoctorFragment()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        viewModel = (context as RegistrationActivity).viewModel
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentRegistrationDoctorBinding.inflate(inflater, container, false)
        return binding.root
    }
}