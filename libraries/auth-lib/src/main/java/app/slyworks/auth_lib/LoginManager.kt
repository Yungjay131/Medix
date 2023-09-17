package app.slyworks.auth_lib

import app.slyworks.constants_lib.KEY_FCM_REGISTRATION
import app.slyworks.constants_lib.KEY_IS_THERE_NEW_FCM_REG_TOKEN
import app.slyworks.constants_lib.KEY_LAST_SIGN_IN_TIME
import app.slyworks.constants_lib.KEY_LOGGED_IN_STATUS
import app.slyworks.data_lib.CryptoHelper
import app.slyworks.data_lib.DataManager
import app.slyworks.data_lib.vmodels.FBUserDetailsVModel
import app.slyworks.firebase_commons_lib.FirebaseUtils
import app.slyworks.utils_lib.Outcome
import app.slyworks.utils_lib.PreferenceManager
import app.slyworks.utils_lib.TimeHelper
import com.google.firebase.auth.FirebaseAuth
import io.reactivex.rxjava3.core.Single
import timber.log.Timber



class LoginManager(
    private val preferenceManager: PreferenceManager,
    private val firebaseAuth:FirebaseAuth,
    private val usersManager: UsersManager,
    private val firebaseUtils: FirebaseUtils,
    private val timeHelper: TimeHelper,
    private val cryptoHelper: CryptoHelper,
    private val dataManager: DataManager,
    private val authStateListener: MAuthStateListener) {

    private var loggedInStatus:Boolean = false


    fun getLoginStatus2():Boolean =
        preferenceManager.get(KEY_LOGGED_IN_STATUS, false)!!  &&
        with(preferenceManager.get(KEY_LAST_SIGN_IN_TIME, System.currentTimeMillis())){
                    timeHelper.isWithin3DayPeriod(this!!)
                }





    fun onDestroy(){
        firebaseAuth.removeAuthStateListener(authStateListener)
    }
}