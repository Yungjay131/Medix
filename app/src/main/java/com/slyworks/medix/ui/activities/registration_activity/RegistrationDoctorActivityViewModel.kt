package com.slyworks.medix.ui.activities.registration_activity

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.slyworks.medix.App
import com.slyworks.auth.RegistrationManager
import com.slyworks.constants.PROFILE_PHOTO_URI
import com.slyworks.models.models.Outcome
import com.slyworks.models.models.TempUserDetails
import com.slyworks.network.NetworkRegister
import com.slyworks.utils.PreferenceManager
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject


/**
 *Created by Joshua Sylvanus, 9:17 PM, 09/05/2022.
 */
class RegistrationDoctorActivityViewModel
    @Inject
    constructor(private var networkRegister: NetworkRegister?,
                private var registrationManager: RegistrationManager?,
                private var preferenceManager: PreferenceManager?) : ViewModel(){
    //region Vars
      var ivProfileUriVal:Uri? = null
      var etFirstNameVal:String = ""
      var etLastNameVal:String = ""
      var etEmailVal:String =  ""
      var etPasswordVal:String = ""
      var etConfirmPasswordVal:String = ""
      var rbMaleVal:Boolean = false
      var rbFemaleVal:Boolean = false
      var cbAgreeVal:Boolean = false
      var specializationList:MutableMap<Int, String> =
          mutableMapOf()

      private val _profileImageUriLiveData:MutableLiveData<Uri?> = MutableLiveData()
      val profileImageUriLiveData:LiveData<Uri?>
      get() = _profileImageUriLiveData as LiveData<Uri?>

      private val _registrationStatusLiveData:MutableLiveData<Outcome> = MutableLiveData()
      val registrationStatusLiveDetails:LiveData<Outcome>
      get() = _registrationStatusLiveData as LiveData<Outcome>

      private val mSubscriptions:CompositeDisposable = CompositeDisposable()

    private var mSubscription2: Disposable = Disposable.empty()
    //endregion

    fun updateValue(id:Int, newText:String){
        specializationList.remove(id)
        specializationList.put(id, newText)
    }

    fun addLayout(id:Int){
       val l:String = ""
        specializationList.put(id, l)
    }

    fun removeLayout(id:Int) = specializationList.remove(id)

    fun setProfileImageURI(uri: Uri)  = preferenceManager!!.set(PROFILE_PHOTO_URI, uri.toString())

    fun register(details:TempUserDetails){
        val d = registrationManager!!
            .register(details)
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe {
                _registrationStatusLiveData.postValue(it)
            }

        mSubscriptions.add(d)
    }

    fun handleProfileImageUri(o:Observable<Uri?>){
        val d =
            o.subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe {
                _profileImageUriLiveData.postValue(it)
            }

        mSubscriptions.add(d)
    }

    fun getNetworkStatus():Boolean = networkRegister!!.getNetworkStatus()

    fun subscribeToNetwork():LiveData<Boolean>{
        val l:MutableLiveData<Boolean> = MutableLiveData()

        networkRegister = NetworkRegister(App.getContext())
        mSubscription2 = networkRegister!!
            .subscribeToNetworkUpdates()
            .observeOn(Schedulers.io())
            .subscribeOn(Schedulers.io())
            .subscribe {
                l.postValue(it)
            }

        return l
    }

    fun unsubscribeToNetwork(){
        networkRegister!!.unsubscribeToNetworkUpdates()
        mSubscription2.dispose()
    }

    override fun onCleared() {
        mSubscriptions.clear()
        networkRegister = null
        super.onCleared()
    }


}

