package app.slyworks.base_feature.ui

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import app.slyworks.base_feature.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder


/**
 * Created by Joshua Sylvanus, 3:11 AM, 27-Jan-23.
 */
class PromptDialog(private val prompt:String,
                   private val buttonText:String,
                   private val isCancellable:Boolean,
                   private val onDismiss:(() -> Unit)? = null) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext(), theme)
            .also {
                val dialogView = onCreateView(layoutInflater, null, savedInstanceState)
                dialogView?.let { it2 -> onViewCreated(it2, savedInstanceState) }
                it.setView(dialogView)
            }
            .create()
            .also{ it.setCanceledOnTouchOutside(isCancellable) }

    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_prompt, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
    }

    private fun initViews(view:View){
        view.findViewById<TextView>(R.id.tv_message).setText(prompt)
        with(view.findViewById<Button>(R.id.btn_ok)) {
            text = buttonText
            setOnClickListener {
                this@PromptDialog.dismiss()
            }
        }
    }


    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)

        onDismiss?.invoke()
    }
}

