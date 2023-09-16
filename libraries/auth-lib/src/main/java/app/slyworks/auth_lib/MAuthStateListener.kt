package app.slyworks.auth_lib

import app.slyworks.constants_lib.KEY_LOGGED_IN_STATUS
import app.slyworks.utils_lib.PreferenceManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

/**
 *Created by Joshua Sylvanus, 12:11 PM, 12/10/2021.
 */
class MAuthStateListener(private val preferenceManager: PreferenceManager): FirebaseAuth.AuthStateListener {
    private var email:String? = null
    private var password:String? = null
    private var loggedInStatus:Boolean = false
    private var currentUser: FirebaseUser? = null

    fun setEmailAndPassword(email:String, password:String){
        this.email = email
        this.password = password
    }

    fun getEmailAndPassword():Pair<String?,String?> =
        (email to password)

    fun setLoggedInStatus(status:Boolean){
        loggedInStatus = status
        if(!status){
            email = null
            password = null
        }
    }

    fun getLoggedInStatus():Boolean = loggedInStatus

    fun getCurrentUser():FirebaseUser? = currentUser

    override fun onAuthStateChanged(p0: FirebaseAuth) {
        currentUser = p0.currentUser
        loggedInStatus = p0.currentUser != null
        preferenceManager.set(KEY_LOGGED_IN_STATUS, loggedInStatus)
    }
}