package com.slyworks.medix.ui.fragments.chatFragment

import androidx.lifecycle.Observer
import com.nhaarman.mockitokotlin2.verify
import com.slyworks.communication.MessageManager
import com.slyworks.models.room_models.Person
import com.slyworks.network.NetworkRegister
import com.slyworks.userdetails.UserDetailsUtils
import org.junit.Assert.*
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

/**
 * Created by Joshua Sylvanus, 8:47 PM, 03/11/2022.
 */
@RunWith(MockitoJUnitRunner::class)
class ChatFragmentViewModelTest{
    @Mock
    private lateinit var networkRegister: NetworkRegister
    @Mock
    private lateinit var userDetailsUtils:UserDetailsUtils
    @Mock
    private lateinit var messageManager: MessageManager
    @Mock
    private lateinit var successStateObserver: Observer<List<Person>>
    private lateinit var viewModel:ChatFragmentViewModel

    @BeforeClass
    fun setup(){
        viewModel =
        ChatFragmentViewModel(networkRegister, userDetailsUtils, messageManager)
        viewModel.successStateLiveData.observeForever(successStateObserver)
    }

    @Test
    fun `does successState receive list of Persons`(){
        val listClass:Class<ArrayList<Person>> =
        ArrayList::class.java as Class<ArrayList<Person>>

        val argumentCaptor:ArgumentCaptor<ArrayList<Person>> =
        ArgumentCaptor.forClass(listClass)

        verify(successStateObserver).onChanged(argumentCaptor.capture())

        assertTrue(argumentCaptor.value.size > 0)

        /*
        * assertTrue(capturedArgument
.containsAll(listOf(wishlist1, wishlist2)))
        * */
    }

}