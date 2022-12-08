package app.slyworks.auth_feature.registration

import com.google.common.truth.Truth.assertThat
import junit.framework.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Created by Joshua Sylvanus, 9:33 PM, 08-Dec-2022.
 */

@RunWith(RobolectricTestRunner::class)
class RegistrationGeneral1FragmentTest{
    @Test
    fun `does regex work for at least 1 number`(){
        val s:CharSequence = "password1"
        assertThat(s.contains("[0-9]".toRegex())).isTrue()
    }

    @Test
    fun `does regex work for at least 1 uppercase character`(){
        val s:CharSequence = "Password"
        assertThat(s.contains("[A-Z]".toRegex())).isTrue()
    }

    @Test
    fun `does regex work for at least 1 special character`(){
        val s:CharSequence = "Password$"
        assertThat(s.contains("[@#\$*!~%^&+=]".toRegex())).isTrue()
    }

    @Test
    fun `verify for various inputs`(){
        whenever()
    }
}