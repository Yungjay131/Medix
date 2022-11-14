package com.slyworks.medix.ui.activities.login_activity

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.nhaarman.mockitokotlin2.*
import com.slyworks.auth.LoginManager
import com.slyworks.auth.RegistrationManager
import com.slyworks.communication.RxImmediateSchedulerRule
import com.slyworks.constants.INPUT_ERROR
import com.slyworks.medix.helpers.VibrationManager
import com.slyworks.models.models.Outcome
import com.slyworks.network.NetworkRegister
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.observers.TestObserver
import io.reactivex.rxjava3.schedulers.Schedulers
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

/**
 * Created by Joshua Sylvanus, 7:13 PM, 01/11/2022.
 */
@RunWith(MockitoJUnitRunner::class)
class LoginActivityViewModelTest{
    private lateinit var networkRegister: NetworkRegister
    private lateinit var loginManager: LoginManager
    private lateinit var vibrationManager: VibrationManager
    private lateinit var viewModel:LoginActivityViewModel
    @Mock
    private lateinit var passwordResetStatusObserver:Observer<Boolean>
    @Mock
    private lateinit var progressStateObserver:Observer<Boolean>
    @Mock
    private lateinit var loginSuccessStateObserver:Observer<Boolean>
    @Mock
    private lateinit var loginFailureDataObserver:Observer<String>
    @Mock
    private lateinit var loginFailureStateObserver:Observer<in Boolean>

    /* making the tests run synchronously*/
    @get:Rule
    val taskExecutorRule = InstantTaskExecutorRule()

    @Rule
    val rxImmediateSchedulerRule = RxImmediateSchedulerRule()

    @Before
    fun setup(){
        networkRegister = mock<NetworkRegister>()
        loginManager = mock<LoginManager>()
        vibrationManager = mock<VibrationManager>()

        viewModel =
        LoginActivityViewModel(networkRegister, loginManager, vibrationManager)
        viewModel.passwordResetLiveData.observeForever(passwordResetStatusObserver)
        viewModel.progressStateLiveData.observeForever(progressStateObserver)
        viewModel.loginSuccessLiveData.observeForever(loginSuccessStateObserver)
        viewModel.loginFailureDataLiveData.observeForever(loginFailureDataObserver)
        viewModel.loginFailureLiveData.observeForever(loginFailureStateObserver)
    }

    @Test
    fun doesVibrate_callVibrationManager_vibrate(){
        viewModel.vibrate(INPUT_ERROR)
        verify(vibrationManager).vibrate(eq(INPUT_ERROR))
    }

    @Test
    fun doesGetNetworkStatus_callNetworkRegister_getNetworkStatus(){
        viewModel.getNetworkStatus()
        verify(networkRegister).subscribeToNetworkUpdates()
    }

    @Test
    fun doesSubscribeToNetwork_callNetworkRegister_subscribeToNetworkUpdates(){
        viewModel.subscribeToNetwork()
        verify(networkRegister).subscribeToNetworkUpdates()
    }

    @Test
    fun doesUnsubscribeToNetwork_callNetworkRegister_unsubscribeToNetworkUpdates(){
        viewModel.unsubscribeToNetwork()
        verify(networkRegister).unsubscribeToNetworkUpdates()
    }

    @Test
    fun `does login() call LoginManager#loginUser`(){
        viewModel.login("", "")
        verify(loginManager).loginUser(anyString(), anyString())
    }

    @Test
    fun doesLogin_updateFailureState_whenThereIsNoInternet(){
        whenever(networkRegister.getNetworkStatus())
            .thenReturn(false)

        viewModel.login("", "")

        verify(loginFailureStateObserver).onChanged(eq(true))
    }

    @Test
    fun doesLogin_updateFailureState_whenLoginIsUnsuccessful(){
        val o:Observable<Outcome> =
        Observable.fromCallable { Outcome.FAILURE<String>("error occurred") }
                 //.test()

        /*val testObserver:TestObserver<Outcome> =
            Observable.empty<Outcome>()
                      .test()
        testObserver.values().first()*/

        whenever(networkRegister.getNetworkStatus())
            .thenReturn(true)
        whenever(loginManager.loginUser(anyString(), anyString()))
            .thenReturn(o)

        viewModel.login("","")

        inOrder(){
            /* trace the whole process here */
        }
        verify(loginFailureStateObserver).onChanged(eq(true))
    }

    @Test
    fun doesLogin_updateFailureStateData_appropriately_whenLoginIsUnsuccessful(){}

    @Test
    fun doesLogin_updateSuccessState_appropriately_whenLoginIsSuccessful(){}

    @Test
    fun `does handleForgotPassword() update failureState when there is no internet`(){}

    @Test
    fun `does handleForgotPassword() update passwordResetStatus when unsuccessful`(){}

    @Test
    fun `does handleForgotPassword() update passwordResetStatus when successful`(){}

    @Test
    fun `does onCleared() dereference dependencies`(){}
}