package app.slyworks.core_feature.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import app.slyworks.auth_lib.LoginManager
import app.slyworks.data_lib.DataManager
import app.slyworks.network_lib.NetworkRegister
import app.slyworks.utils_lib.utils.plusAssign
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject


/**
 *Created by Joshua Sylvanus, 4:34 PM, 1/23/2022.
 */
class HomeActivityViewModel
    @Inject
    constructor(private val networkRegister: NetworkRegister,
                private val dataManager: DataManager,
                private val loginManager: LoginManager): ViewModel() {
    //region Vars
    private val disposables:CompositeDisposable = CompositeDisposable()
    private val l: MutableLiveData<Boolean> = MutableLiveData()
    //endregion

    fun getUserAccountType(): String = dataManager.getUserDetailsProperty<String>("accountType")!!

    fun logout():Unit = loginManager.logoutUser()

    fun subscribeToNetwork(): LiveData<Boolean> {
        disposables +=
        networkRegister.subscribeToNetworkUpdates()
          .subscribeOn(Schedulers.io())
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe {
              l.postValue(it)
          }

        return l
    }

    fun unsubscribeToNetwork(){
        networkRegister.unsubscribeToNetworkUpdates()
        disposables.clear()
    }

}