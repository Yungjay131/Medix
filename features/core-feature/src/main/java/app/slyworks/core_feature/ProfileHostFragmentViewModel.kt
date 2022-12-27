package app.slyworks.core_feature

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import app.slyworks.auth_lib.UsersManager
import app.slyworks.communication_lib.ConsultationRequestsManager
import app.slyworks.data_lib.DataManager
import app.slyworks.data_lib.models.ConsultationRequestVModel
import app.slyworks.data_lib.models.FBUserDetailsVModel
import app.slyworks.models_commons_lib.models.MessageMode
import app.slyworks.network_lib.NetworkRegister
import app.slyworks.utils_lib.utils.plusAssign
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject

/**
 * Created by Joshua Sylvanus, 4:21 AM, 1/8/2022.
 */

class ProfileHostFragmentViewModel
  @Inject
  constructor(private val networkRegister: NetworkRegister,
              private val usersManager: UsersManager,
              private val consultationRequestsManager: ConsultationRequestsManager,
              private val dataManager: DataManager) : ViewModel() {

   //region Vars
    val doctorsListLiveData: MutableLiveData<MutableList<FBUserDetailsVModel>> = MutableLiveData()

    private val _consultationRequestStatusLiveData:MutableLiveData<String> = MutableLiveData()
    val consultationRequestStatusLiveData: LiveData<String>
        get() = _consultationRequestStatusLiveData

    fun getUserDetailsUser():FBUserDetailsVModel = dataManager.getUserDetailsParam<FBUserDetailsVModel>()!!

    fun observeConsultationRequestStatus(userUID:String){
        this.userUID = userUID
        disposables +=
            consultationRequestsManager.observeConsultationRequestStatus(userUID)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe {
                    _consultationRequestStatusLiveData.postValue(it)
                }
    }

    fun sendConsultationRequest(request: ConsultationRequestVModel, mode: MessageMode = MessageMode.DB_MESSAGE)
            = consultationRequestsManager.sendConsultationRequest(request, mode)

    private lateinit var userUID:String

    private val disposables: CompositeDisposable = CompositeDisposable()
   //endregion

    fun getNetworkStatus():Boolean = networkRegister.getNetworkStatus()

    fun getAllDoctors() = usersManager.getAllDoctors()

    private fun observeDoctors(){
        disposables +=
            usersManager.observeDoctors()
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .subscribe({
                    if(it.isSuccess)
                        doctorsListLiveData.postValue(it.getTypedValue())
                },{})
    }

    override fun onCleared() {
        super.onCleared()
        consultationRequestsManager.detachCheckRequestStatusListener(userUID)
        disposables.clear()
    }
}
