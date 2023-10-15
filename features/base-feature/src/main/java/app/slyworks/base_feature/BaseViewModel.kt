package app.slyworks.base_feature

import androidx.lifecycle.ViewModel
import app.slyworks.base_feature.network_register.INetworkRegister
import io.reactivex.rxjava3.disposables.CompositeDisposable

/**
 * Created by Joshua Sylvanus, 8:35 PM, 17/09/2023.
 */
abstract class BaseViewModel : ViewModel() {
  abstract val networkRegister: INetworkRegister
  abstract val disposables: CompositeDisposable

  open fun cancelOngoingOperation(){
     disposables.clear()
  }
}
