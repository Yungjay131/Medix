package com.slyworks.medix.ui.dialogs

import android.Manifest
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.collection.SimpleArrayMap
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.slyworks.medix.R


/**
 *Created by Joshua Sylvanus, 4:01 PM, 11/05/2022.
 */
class PermissionsRationaleDialog(private var launcher: ActivityResultLauncher<String>?,
                                 private val mPermission:String) : BaseDialogFragment(){
    //region Vars
    private lateinit var tvText:TextView
    private lateinit var tvAccept:TextView
    private lateinit var tvCancel:TextView
    //endregion

       companion object{
           private val mMap:SimpleArrayMap<String, String> = SimpleArrayMap<String, String>().apply {
               put(Manifest.permission.WRITE_EXTERNAL_STORAGE , "Medix needs this permission to access files stored on the memory of your phone for proper functioning")
               put(Manifest.permission.READ_EXTERNAL_STORAGE , "Medix needs this permission to access files stored on the memory of your phone for proper functioning")
               put(Manifest.permission.CAMERA , "Medix needs this permission to access your phone's camera to enable capturing of images")
               put(Manifest.permission.ACCESS_BACKGROUND_LOCATION , "Medix needs this permission to be able to access location updates in the background")
               put(Manifest.permission.ACCESS_FINE_LOCATION , "Medix needs this permission access to your current location for some of its services to work")
               put(Manifest.permission.ACCESS_COARSE_LOCATION , "Medix needs this permission access to your current location for some of its services to work")
               put(Manifest.permission.RECORD_AUDIO , "Medix needs this permission to record audio to make voice calls and video calls possible")
               put(Manifest.permission.READ_PHONE_STATE , "Medix needs this permission to get cellular network details necessary for the proper functioning of the app")
           }

       }

    override fun onDestroy() {
        launcher = null
        super.onDestroy()
    }

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
        return inflater.inflate(R.layout.dialog_permission_rationale2, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
       tvText = view.findViewById(R.id.tvText_permissions_rationale)
       tvAccept = view.findViewById(R.id.tvAccept_dialog_permissions_rationale)
       tvCancel = view.findViewById(R.id.tvCancel_dialog_permissions_rationale)

       tvText.text = mMap[mPermission]

       tvAccept.setOnClickListener {
           launcher?.launch(mPermission)
           this.dismiss()
       }
       tvCancel.setOnClickListener { this.dismiss() }
    }


}