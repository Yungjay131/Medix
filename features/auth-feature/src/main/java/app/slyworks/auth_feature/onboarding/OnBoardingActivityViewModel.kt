package app.slyworks.auth_feature.onboarding

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import app.slyworks.base_feature.BaseViewModel
import app.slyworks.base_feature.network_register.INetworkRegister
import app.slyworks.base_feature.network_register.NetworkRegister
import app.slyworks.utils_lib.utils.plusAssign
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject


/**
 * Created by Joshua Sylvanus, 5:54 PM, 18/05/2022.
 */
class OnBoardingActivityViewModel
    @Inject
    constructor(override val networkRegister: INetworkRegister) : BaseViewModel() {

    override val disposables:CompositeDisposable = CompositeDisposable()
}