package app.slyworks.core_feature.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import app.slyworks.data_lib.DataManager
import app.slyworks.utils_lib.utils.plusAssign
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject


/**
 *Created by Joshua Sylvanus, 11:28 AM, 1/18/2022.
 */
class HomeFragmentViewModel
    @Inject
    constructor(private val dataManager: DataManager) : ViewModel() {
    //region Vars
    val imageUriLiveData: MutableLiveData<String> = MutableLiveData()
    private val disposables: CompositeDisposable = CompositeDisposable()
    //endregion

    fun observeUserProfilePic(){
        disposables +=
        dataManager.observeUserDetailsFromDataStore()
            .map{ it.imageUri }
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe(imageUriLiveData::postValue)
    }

    /* proxy to DataManager */
    fun <T> getUserProperty(propertyName:String):T =
        dataManager.getUserDetailsProperty<T>(propertyName)!!

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }
}
