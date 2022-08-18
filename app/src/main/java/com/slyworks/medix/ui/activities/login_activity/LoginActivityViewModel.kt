package com.slyworks.medix.ui.activities.login_activity

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.slyworks.auth.LoginManager
import com.slyworks.auth.RegistrationManager
import com.slyworks.medix.helpers.VibrationManager
import com.slyworks.network.NetworkRegister
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject


/**
 *Created by Joshua Sylvanus, 6:39 AM, 07/06/2022.
 */
class LoginActivityViewModel
    @Inject
    constructor(private var networkRegister: NetworkRegister?,
                private var loginManager: LoginManager?,
                private var registrationManager: RegistrationManager?,
                private var vibrationManager: VibrationManager?) : ViewModel(){
    //region Vars
    private var _passwordResetStatus:MutableLiveData<Boolean> = MutableLiveData()
    val passwordResetLiveData:LiveData<Boolean>
    get() = _passwordResetStatus as LiveData<Boolean>

    private var _progressStateLiveData:MutableLiveData<Boolean> = MutableLiveData()
    val progressStateLiveData:LiveData<Boolean>
    get() = _progressStateLiveData

    private var _loginSuccessLiveData:MutableLiveData<Boolean> = MutableLiveData()
    val loginSuccessLiveData:LiveData<Boolean>
    get() = _loginSuccessLiveData as LiveData<Boolean>

    private var _loginFailureDataLiveData:MutableLiveData<String> = MutableLiveData()
    val loginFailureDataLiveData:LiveData<String>
    get() = _loginFailureDataLiveData as LiveData<String>

    private var _loginFailureLiveData:MutableLiveData<Boolean> = MutableLiveData()
    val loginFailureLiveData:LiveData<Boolean>
    get() = _loginFailureLiveData as LiveData<Boolean>

    var emailVal:String = ""
    var passwordVal:String = ""

    private val mSubscriptions:CompositeDisposable = CompositeDisposable()
    private var mSubscription2:Disposable = Disposable.empty()
    //endregion

    fun vibrate(type:Int) = vibrationManager!!.vibrate(type)

    fun getNetworkStatus() = networkRegister!!.getNetworkStatus()

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
        mSubscription2.dispose()
    }

    fun login(email:String, password:String){
        if(!networkRegister!!.getNetworkStatus()){
            _loginFailureDataLiveData.postValue("Please check your connection and try again")
            _loginFailureLiveData.postValue(true)
            return
        }

        val d = loginManager!!
            .loginUser(email, password)
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe {
                _progressStateLiveData.postValue(false)

                when{
                    it.isSuccess ->{
                        _loginFailureLiveData.postValue(false)
                        _loginSuccessLiveData.postValue(true)
                    }it.isFailure || it.isError ->{
                        _loginFailureDataLiveData.postValue(it.getTypedValue<String>())
                        _loginFailureLiveData.postValue(true)
                    }
                }
            }
        mSubscriptions.add(d)
    }

    fun handleForgotPassword(email: String){
        if(!networkRegister!!.getNetworkStatus()){
            _loginFailureDataLiveData.postValue("Please check your connection and try again")
            _loginFailureLiveData.postValue(true)
            return
        }

        _progressStateLiveData.postValue(true)

        val d = registrationManager!!
            .handleForgotPassword(email)
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe {
                _progressStateLiveData.postValue(false)
                _passwordResetStatus.postValue(it)
            }

        mSubscriptions.add(d)
    }


    override fun onCleared() {
        mSubscriptions.clear()
        loginManager!!.onDestroy()
        loginManager = null

        super.onCleared()
    }
}