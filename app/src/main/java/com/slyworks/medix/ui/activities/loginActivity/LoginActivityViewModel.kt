package com.slyworks.medix.ui.activities.loginActivity

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slyworks.constants.EVENT_SEND_PASSWORD_RESET_EMAIL
import com.slyworks.constants.EVENT_USER_LOGIN
import com.slyworks.medix.*
import com.slyworks.medix.AppController.clearAndRemove
import com.slyworks.models.models.NotifyMethod
import com.slyworks.models.models.Observer
import com.slyworks.models.models.Outcome
import com.slyworks.network.NetworkRegister
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


/**
 *Created by Joshua Sylvanus, 6:39 AM, 07/06/2022.
 */
class LoginActivityViewModel : ViewModel(){
    //region Vars
    var emailVal:String = ""
    var passwordVal:String = ""
    private var _passwordResetStatus:MutableLiveData<Boolean> = MutableLiveData()
    val passwordResetLiveData:LiveData<Boolean>
    get() = _passwordResetStatus as LiveData<Boolean>

    private var _loginStatusLiveData:MutableLiveData<Outcome> = MutableLiveData()
    val loginStatusLiveData:LiveData<Outcome>
    get() = _loginStatusLiveData as LiveData<Outcome>

    private val mSubscriptions:CompositeDisposable = CompositeDisposable()
    private var mSubscription2:Disposable = Disposable.empty()
    private var mNetworkRegister:NetworkRegister? = null
    //endregion

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

        l.postValue(mNetworkRegister!!.getNetworkStatus())
        return l
    }


    fun unsubscribeToNetwork(){
        mNetworkRegister!!.unsubscribeToNetworkUpdates()
        mNetworkRegister = null
        mSubscription2.dispose()
    }

    fun login(email:String, password:String){
        val d = LoginManager.getInstance()
            .loginUser(email, password)
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe {
                _loginStatusLiveData.postValue(it)
            }

        mSubscriptions.add(d)
    }

    fun handleForgotPassword(email: String){
        val d = RegistrationManager()
            .handleForgotPassword(email)
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe {
                _passwordResetStatus.postValue(it)
            }

        mSubscriptions.add(d)
    }


    override fun onCleared() {
        mSubscriptions.clear()

        LoginManager.getInstance()
            .onDestroy()

        super.onCleared()
    }
}