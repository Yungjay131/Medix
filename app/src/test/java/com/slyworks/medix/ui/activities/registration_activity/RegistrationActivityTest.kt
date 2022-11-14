package com.slyworks.medix.ui.activities.registration_activity

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.slyworks.communication.RxImmediateSchedulerRule
import com.slyworks.medix.R
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Created by Joshua Sylvanus, 10:02 PM, 10/11/2022.
 */
@RunWith(RobolectricTestRunner::class)
class RegistrationActivityTest{
    @Rule
    val rxSchedulerRule = RxImmediateSchedulerRule()

    @Before
    fun setup(){
        ActivityScenario.launch(RegistrationPatientActivity::class.java)
    }

    @Test
    fun `does clicking profile imageView display permissionsDialog`(){
        onView(withId(R.id.ivProfile_patient_reg_one))
            .perform(click())
    }

    @Test
    fun `does denying permission dialog display PermissionsRationaleDialog`(){}

    @Test
    fun `does clicking cancel return false`(){}

}