package com.slyworks.medix.helpers

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
import com.slyworks.medix.utils.plusAssign
import com.slyworks.models.models.Outcome
import com.slyworks.utils.PreferenceManager
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.subjects.PublishSubject


/**
 *Created by Joshua Sylvanus, 2:48 PM, 12/16/2021.
 */
class PermissionManager {

    //region Vars
    private var activity: Activity? = null
    private lateinit var permissions:List<String>
    private lateinit var mPermissionsLauncher:ActivityResultLauncher<String>
    private var mO:PublishSubject<Boolean> = PublishSubject.create()
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

       /* fun of(activity: Activity, vararg permissions:String): PermissionManager {
            if(permissions.isEmpty())
                throw IllegalArgumentException("vararg param for permissions is empty")

            return PermissionManager(activity, permissions.toList())
        }*/
    }

    fun initialize(activity: Activity,
                   vararg permissions:String){
        this.activity = activity
        this.permissions = permissions.toList()

        mPermissionsLauncher =
            (activity as AppCompatActivity).registerForActivityResult(
                ActivityResultContracts.RequestPermission()) { result -> mO.onNext(result ?: false) }

    }

    private fun isPermissionGranted(permission:String):Boolean =
        (ContextCompat.checkSelfPermission(App.getContext(), permission)
                == PackageManager.PERMISSION_GRANTED)

    private fun isPermissionDenied(permission:String):Boolean
    = (ContextCompat.checkSelfPermission(App.getContext(), permission)
            == PackageManager.PERMISSION_DENIED)

    fun requestPermissions(): Observable<Outcome>{
     val subscriptions:CompositeDisposable = CompositeDisposable()

     return Observable.fromIterable(permissions)
              .concatMap {
                Observable.create<Outcome> { emitter ->
                    when{
                        isPermissionGranted(it) ->{
                            emitter.onNext(Outcome.SUCCESS(true))
                            emitter.onComplete()
                        }
                        else ->{
                            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                                if (activity!!.shouldShowRequestPermissionRationale(it)) {
                                    val dialog = PermissionsRationaleDialog(mPermissionsLauncher, it)

                                    subscriptions +=
                                        Observable.merge(
                                            /*in case the dialog is cancelled and mO didn't get a value to emit*/
                                            dialog.getObservable(),
                                            mO)
                                            .subscribe { it2:Boolean ->
                                            val o = if(it2) Outcome.SUCCESS(it) else Outcome.FAILURE(false, it)
                                            emitter.onNext(o)
                                            emitter.onComplete()
                                        }

                                    dialog.show((activity as AppCompatActivity).supportFragmentManager, "")
                                }else {
                                    if(isPermissionDenied(it)){
                                        /* means the permission was requested and denied */
                                        emitter.onNext(Outcome.FAILURE(false, it))
                                        emitter.onComplete()
                                    }else{
                                        mPermissionsLauncher.launch(it)
                                        subscriptions +=
                                            mO.subscribe { it3:Boolean ->
                                                val o = if(it3) Outcome.SUCCESS(it3) else Outcome.FAILURE(false, it)
                                                emitter.onNext(o)
                                                emitter.onComplete()
                                            }
                                    }
                                }
                            }else{
                                mPermissionsLauncher.launch(it)
                                subscriptions +=
                                mO.subscribe { it4:Boolean ->
                                    val o = if(it4) Outcome.SUCCESS(it4) else Outcome.FAILURE(false, it)
                                    emitter.onNext(o)
                                    emitter.onComplete()
                                }
                            }
                        }
                    }
                }
                .doOnComplete {
                    subscriptions.clear()
                }
            }
            .toList()
            .toObservable()
            .map {
                val l:MutableList<Outcome> = mutableListOf()
                it.forEach { it5:Outcome ->
                    if(it5.isFailure)
                      l.add(it5)
                }

                if(l.isNotEmpty())
                    return@map Outcome.FAILURE(value = l)

                return@map Outcome.SUCCESS(true)
            }
            .doOnDispose {
                activity = null
            }
     }
}



