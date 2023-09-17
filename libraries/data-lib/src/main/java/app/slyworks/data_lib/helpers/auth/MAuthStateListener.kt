package app.slyworks.data_lib.helpers.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

/**
 *Created by Joshua Sylvanus, 12:11 PM, 12/10/2021.
 */
class MAuthStateListener : FirebaseAuth.AuthStateListener {
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

    fun getLoggedInStatus():Boolean = loggedInStatus

    fun getCurrentUser():FirebaseUser? = currentUser

    override fun onAuthStateChanged(p0: FirebaseAuth) {
        currentUser = p0.currentUser
        loggedInStatus = p0.currentUser != null
    }
}