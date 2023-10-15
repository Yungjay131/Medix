package app.slyworks.base_feature.ui

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import app.slyworks.base_feature.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textview.MaterialTextView


/**
 * Created by Joshua Sylvanus, 3:32 PM, 23-Sep-2023.
 */
class ProgressOverlayDialog(private var loadingText:String? = null) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext(), theme)
            .also {
                val dialogView = onCreateView(layoutInflater, null, savedInstanceState)
                dialogView?.let { it2 -> onViewCreated(it2, savedInstanceState) }
                it.setView(dialogView)
            }
            .create()
            .also{ it.setCanceledOnTouchOutside(false) }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_progress_overlay, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
    }

    private fun initViews(view:View){
        if(loadingText == null)
            return

        view.findViewById<MaterialTextView>(R.id.tv_loading)
            .setText(loadingText)
    }
}