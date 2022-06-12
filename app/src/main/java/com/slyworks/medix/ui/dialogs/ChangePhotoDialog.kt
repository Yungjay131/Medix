package com.slyworks.medix.ui.dialogs

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textview.MaterialTextView
import com.slyworks.constants.CAMERA_FILE_REQUEST_CODE
import com.slyworks.constants.EVENT_SELECT_PROFILE_IMAGE
import com.slyworks.constants.IMAGE_FILE_REQUEST_CODE
import com.slyworks.medix.AppController
import com.slyworks.medix.BuildConfig
import com.slyworks.medix.R
import com.slyworks.medix.utils.*
import com.slyworks.models.models.Outcome
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.jvm.Throws


/**
 *Created by Joshua Sylvanus, 6:53 AM, 12/20/2021.
 */
class ChangePhotoDialog(): BaseDialogFragment() {
    //region Vars
    private val TAG: String? = ChangePhotoDialog::class.simpleName

    private lateinit var tvSelectFromGallery:MaterialTextView
    private lateinit var tvTakePhotoUsingCamera:MaterialTextView

    private lateinit var mCurrentPhotoPath: String

    private var mO:PublishSubject<Uri?> = PublishSubject.create()

    private val takePhotoResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult(),
        object:ActivityResultCallback<ActivityResult>{
            override fun onActivityResult(result: ActivityResult) {
                when(result.resultCode) {
                    Activity.RESULT_OK -> {
                        mO.onNext(Uri.fromFile(File(mCurrentPhotoPath)))
                        mO.onComplete()
                        //AppController.notifyObservers(EVENT_SELECT_PROFILE_IMAGE, selectedImageUri)
                    }
                    else -> {
                        mO.onNext(null)
                        mO.onComplete()

                        //AppController.notifyObservers(EVENT_SELECT_PROFILE_IMAGE, null)
                    }
                }

                dismiss()
            }
        })

    private val selectPhotoResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult(),
        object:ActivityResultCallback<ActivityResult>{
            override fun onActivityResult(result: ActivityResult) {
                when(result.resultCode) {
                    Activity.RESULT_OK -> {
                        mO.onNext(result.data?.data)
                        mO.onComplete()
                        //AppController.notifyObservers(EVENT_SELECT_PROFILE_IMAGE, selectedImageUri)
                    }
                    else -> {
                        mO.onNext(null)
                        mO.onComplete()
                        //AppController.notifyObservers(EVENT_SELECT_PROFILE_IMAGE, null)
                    }
                }

                dismiss()
            }
        })
    //endregion

    companion object{
        @JvmStatic
        fun getInstance(): ChangePhotoDialog = ChangePhotoDialog()

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
        val view = inflater.inflate(R.layout.dialog_change_photo, container, false)
        initViews(view)
        return view
    }

    private fun initViews(view: View) {
        tvSelectFromGallery = view.findViewById(R.id.tvSelectGallery_dialog_photo)
        tvTakePhotoUsingCamera = view.findViewById(R.id.tvTakePhoto_dialog_photo)

        tvSelectFromGallery.setOnClickListener { selectPhotoFromMemory() }
        tvTakePhotoUsingCamera.setOnClickListener { takePhoto() }
    }

    private fun selectPhotoFromMemory(){
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.setType("image/*")
        selectPhotoResultLauncher.launch(intent)
    }

    private fun takePhoto():Unit{
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if(intent.resolveActivity(requireActivity().packageManager) == null){
            Log.e(TAG, "snapPhoto: no app to handle camera request" )
            showToast("no camera app installed, please install one and try again")
            return
        }

        var photoFile:File? = null
        try {
            photoFile = createImageFile()
        }catch (ioe:IOException){
            Log.e(TAG, "takePhoto: ${ioe.message}" )
            dismiss()
        }

        //continue only if file was successfully created
        if(photoFile == null){
            Log.e(TAG, "takePhoto: photoFile wasn't created" )
            showToast("photoFile wasn't created")
            return
        }

        val photoUri:Uri =
            FileProvider.getUriForFile(
            requireActivity(),
            BuildConfig.APPLICATION_ID + ".provider",
            photoFile )

        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
        takePhotoResultLauncher.launch(intent)
    }

    @Throws(IOException::class)
    private fun createImageFile():File?{
        //creating an image file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_Hhmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_${timeStamp}_"

        val storageDir = requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(
            imageFileName,
            ".jpg",
            storageDir
        )

        mCurrentPhotoPath = image.absolutePath
        return image
    }

    fun getObservable():Observable<Uri?> = mO.hide()

}