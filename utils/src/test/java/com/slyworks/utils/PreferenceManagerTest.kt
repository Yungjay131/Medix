package com.slyworks.utils

import android.content.Context
import com.nhaarman.mockitokotlin2.mock
import org.junit.Assert.*
import org.junit.BeforeClass
import org.junit.Test

/**
 * Created by Joshua Sylvanus, 7:21 AM, 23/10/2022.
 */

class PreferenceManagerTest{
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var context:Context

    @BeforeClass
    fun setup(){
        context = mock()
        preferenceManager = PreferenceManager(context)
    }

    @Test
    fun intValueSet_isValueRetrieved(){
        val int:Int = 1
        val key = "key"
        preferenceManager.set(key, int)

        assertEquals(int, preferenceManager.get(""))
    }
}