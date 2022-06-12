package com.slyworks.medix.utils

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.slyworks.medix.App
import com.slyworks.medix.ui.dialogs.PermissionsRationaleDialog
import com.slyworks.models.models.Outcome
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.reactivex.rxjava3.subjects.PublishSubject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.stream.Collector


/**
 *Created by Joshua Sylvanus, 2:48 PM, 12/16/2021.
 */
class PermissionManager
private constructor(private val activity: Activity,
                    private val permissions:List<String>){

    //region Vars
    private var mPermissionsLauncher:ActivityResultLauncher<String>
    private var mO:PublishSubject<Boolean> = PublishSubject.create()
    private var mCount:Int = 0
    //endregion

    companion object{
        private val mPermissionList:MutableList<String> = mutableListOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.CAMERA,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.MODIFY_AUDIO_SETTINGS,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.VIBRATE,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION,
            Manifest.permission.FOREGROUND_SERVICE,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION,
        )

        fun of(activity: Activity,
               vararg permissions:String):PermissionManager{
            if(permissions.isEmpty())
                throw IllegalArgumentException("vararg param for permissions is empty")

            return PermissionManager(activity, permissions.toList())
        }
    }

    init {
        mPermissionsLauncher =
            (activity as AppCompatActivity).registerForActivityResult(
                ActivityResultContracts.RequestPermission(),
                object : ActivityResultCallback<Boolean> {
                    override fun onActivityResult(result: Boolean?) {
                            mO.onNext(result ?: false)

                            if(mCount + 1 < permissions.size)
                                requestPermissions(permissions[++mCount])
                            else
                                mO.onComplete()
                    }
                })
    }


    private fun requestPermissions(p:String){
            when{
                isPermissionGranted(p) -> {
                    if(mCount + 1 >= permissions.size){
                        mO.onComplete()
                        return
                    }

                    requestPermissions(permissions[++mCount])
                }
                else ->{
                    if(Build.VERSION.SDK_INT > Build.VERSION_CODES.M){
                        if(activity.shouldShowRequestPermissionRationale(p)){
                            PermissionsRationaleDialog(mPermissionsLauncher, p)
                                .show((activity as AppCompatActivity).supportFragmentManager, "")
                        }else{
                            mPermissionsLauncher.launch(p)
                        }
                    }else{
                        mPermissionsLauncher.launch(p)
                    }
                }
            }
    }

    fun requestPermissions(): Single<Boolean>{
        requestPermissions(permissions[0])

        return mO.flatMap {
                 Observable.just(it)
               }
               .toList()
               .map {
                   !it.contains(false)
               }
    }

    private fun isPermissionGranted(permission:String):Boolean  =
        (ContextCompat.checkSelfPermission(App.getContext(), permission)
                == PackageManager.PERMISSION_GRANTED)


}