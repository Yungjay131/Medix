package com.slyworks.medix.ui.activities.registration_activity

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.slyworks.auth.RegistrationManager
import com.slyworks.constants.PROFILE_PHOTO_URI
import com.slyworks.medix.helpers.PermissionManager
import com.slyworks.medix.utils.plusAssign
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
class RegistrationPatientActivityViewModel
    @Inject
    constructor(private var networkRegister: NetworkRegister?,
                private var registrationManager: RegistrationManager?,
                private var preferenceManager: PreferenceManager?,
                var permissionManager: PermissionManager?) : ViewModel(){
    //region Vars
    var ivProfileUriVal: Uri? = null
    var etFirstNameVal:String = ""
    var etLastNameVal:String = ""
    var etEmailVal:String =  ""
    var etPasswordVal:String = ""
    var etConfirmPasswordVal:String = ""
    var rbMaleVal:Boolean = false
    var rbFemaleVal:Boolean = false
    var cbAgreeVal:Boolean = false
    var historyList:MutableMap<Int, String> = mutableMapOf()

    private val _profileImageUriLiveData: MutableLiveData<Uri?> = MutableLiveData()
    val profileImageUriLiveData: LiveData<Uri?>
        get() = _profileImageUriLiveData as LiveData<Uri?>

    private val _registrationStatusLiveData: MutableLiveData<Outcome> = MutableLiveData()
    val registrationStatusLiveDetails: LiveData<Outcome>
        get() = _registrationStatusLiveData as LiveData<Outcome>

    private val mSubscriptions: CompositeDisposable = CompositeDisposable()
    private var mSubscription2: Disposable = Disposable.empty()
    //endregion

    fun updateValue(id:Int, newText:String){
        historyList.remove(id)
        historyList.put(id, newText)
    }

    fun addLayout(id:Int){
        val l:String = ""
        historyList.put(id, l)
    }

    fun removeLayout(id:Int) = historyList.remove(id)

    fun register(details: TempUserDetails){
        val d = registrationManager!!
            .register(details)
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe {
                _registrationStatusLiveData.postValue(it)
            }
        mSubscriptions.add(d)
    }

    fun handleProfileImageUri(o: Observable<Uri>){
        mSubscriptions +=
            o.subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe {
                    it?.let{ _profileImageUriLiveData.postValue(it) }
                }
    }

    fun getNetworkStatus():Boolean = networkRegister!!.getNetworkStatus()

    fun subscribeToNetwork():LiveData<Boolean>{
        val l:MutableLiveData<Boolean> = MutableLiveData()
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
        networkRegister = null
        mSubscription2.dispose()
    }

    fun setProfileImageURI(uri: Uri) {
        preferenceManager!!.set(PROFILE_PHOTO_URI, uri.toString())
    }

    override fun onCleared() {
        mSubscriptions.clear()
        networkRegister = null
        preferenceManager = null
        registrationManager = null
        permissionManager = null

        super.onCleared()
    }


}