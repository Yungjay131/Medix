package com.slyworks.medix.ui.activities.login_activity

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import com.slyworks.medix.R
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import javax.inject.Inject

/**
 * Created by Joshua Sylvanus, 7:03 PM, 04/11/2022.
 */
@RunWith(RobolectricTestRunner::class)
class LoginActivityTest{
    /*@Inject
    private lateinit var loginActivityViewModel: LoginActivityViewModel*/

    /* if running on device, go to `Developer Options` turn off
    * window animation scale
    * transition animation scale
    * animator duration scale
    *
    * don't keep activities - disabled */

    @Before
    fun setup(){
        ActivityScenario.launch(LoginActivity::class.java)
    }

    @Test
    fun `is login button displayed`(){
        onView(withId(R.id.btnLoginLogin))
            .check(matches(isDisplayed()))
    }

    @Test
    fun `does clicking login button start login flow`(){
        onView(withId(R.id.etLoginEmail))
            .perform(typeText("ej@gmail.com"))

        onView(withId(R.id.etLoginPassword))
            .perform(typeText("password"))

        onView(withId(R.id.btnLoginLogin))
            .perform(click())

        onView(withId(R.id.progress))
            .check(matches(isDisplayed()))
    }

    @Test
    fun `is forgot password text displayed`(){
        onView(withId(R.id.tvLoginForgotPassword))
            .check(matches(isDisplayed()))
    }

    @Test
    fun `does forgot password text match expected`(){
        onView(withId(R.id.tvLoginForgotPassword))
            .check(matches(withText("Forgot Password")))
    }


}