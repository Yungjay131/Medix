package com.slyworks.medix.ui.activities.registrationActivity

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.slyworks.medix.App
import com.slyworks.medix.managers.RegistrationManager
import com.slyworks.models.models.Outcome
import com.slyworks.models.models.TempUserDetails
import com.slyworks.network.NetworkRegister
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers

/**
 *Created by Joshua Sylvanus, 9:17 PM, 09/05/2022.
 */
class RegistrationPatientViewModel : ViewModel(){
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
    var historyList:MutableMap<Int, String> =
        mutableMapOf()

    private val _profileImageUriLiveData: MutableLiveData<Uri?> = MutableLiveData()
    val profileImageUriLiveData: LiveData<Uri?>
        get() = _profileImageUriLiveData as LiveData<Uri?>

    private val _registrationStatusLiveData: MutableLiveData<Outcome> = MutableLiveData()
    val registrationStatusLiveDetails: LiveData<Outcome>
        get() = _registrationStatusLiveData as LiveData<Outcome>

    private val mSubscriptions: CompositeDisposable = CompositeDisposable()
    private var mSubscription2: Disposable = Disposable.empty()
    private var mNetworkRegister:NetworkRegister? = null
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
        val d = RegistrationManager()
            .register(details)
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe {
                _registrationStatusLiveData.postValue(it)
            }

        mSubscriptions.add(d)
    }

    fun handleProfileImageUri(o: Observable<Uri?>){
        val d =
            o.subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe {
                    _profileImageUriLiveData.postValue(it)
                }

        mSubscriptions.add(d)
    }

    fun getNetworkStatus():Boolean = mNetworkRegister!!.getNetworkStatus()

    fun subscribeToNetwork():LiveData<Boolean>{
        val l:MutableLiveData<Boolean> = MutableLiveData()

        mNetworkRegister = NetworkRegister(App.getContext())
        mSubscription2 = mNetworkRegister!!
            .subscribeToNetworkUpdates()
            .observeOn(Schedulers.io())
            .subscribeOn(Schedulers.io())
            .subscribe {
                l.postValue(it)
            }

        return l
    }


    fun unsubscribeToNetwork(){
        mNetworkRegister!!.unsubscribeToNetworkUpdates()
        mNetworkRegister = null
        mSubscription2.dispose()
    }
    override fun onCleared() {
        mSubscriptions.clear()

        super.onCleared()
    }
}