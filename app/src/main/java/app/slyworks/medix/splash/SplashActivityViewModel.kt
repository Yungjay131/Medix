package app.slyworks.medix.splash

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import app.slyworks.data_lib.repositories.splash.ISplashRepository
import io.reactivex.rxjava3.disposables.CompositeDisposable
import javax.inject.Inject


/**
 *Created by Joshua Sylvanus, 9:32 PM, 16/08/2022.
 */


sealed class SplashActivityUIState {
    object SessionValid : SplashActivityUIState()
    object SessionInvalid : SplashActivityUIState()
}

class SplashActivityViewModel
    @Inject
    constructor(private val repository: ISplashRepository) : ViewModel() {
    //region Vars
    private val _uiStateLD: MutableLiveData<SplashActivityUIState> = MutableLiveData()
    val uiStateLD: LiveData<SplashActivityUIState> = _uiStateLD

    private val disposables: CompositeDisposable = CompositeDisposable()
    //endregion

    fun checkUserLoggedInStatus() {
        val isUserLoggedIn: Boolean = repository.getLoggedInStatus()
        if (isUserLoggedIn)
            _uiStateLD.setValue(SplashActivityUIState.SessionValid)
        else
            _uiStateLD.setValue(SplashActivityUIState.SessionInvalid)

    }

    override fun onCleared() {
        super.onCleared()

        disposables.clear()
    }
}