package app.slyworks.data_lib.helpers.storage

import app.slyworks.data_lib.helpers.crypto.ICryptoHelper
import app.slyworks.data_lib.model.view_entities.FBUserDetailsVModel
import app.slyworks.utils_lib.*
import com.google.gson.Gson
import io.reactivex.rxjava3.core.Single
import timber.log.Timber


/**
 * Created by Joshua Sylvanus, 9:40 AM, 17-Sep-2023.
 */
class UserDetailsHelper(
    private val cryptoHelper: ICryptoHelper,
    private val preferenceHelper: PreferenceHelper) : IUserDetailsHelper {
    //region Vars
    private var userDetails: FBUserDetailsVModel? = null
    private val gson: Gson = Gson()
    //endregion

    init {
        cryptoHelper.init()
    }

    override fun saveUserDetails(userDetails: FBUserDetailsVModel):Single<Outcome> =
        Single.fromCallable {
          val wasSavedSuccessfully:Boolean = saveUserDetailsToSharedPrefs(userDetails)
          if(!wasSavedSuccessfully) {
              /* try one last time */
              val wasSavedSuccessfully2:Boolean = saveUserDetailsToSharedPrefs(userDetails)
              if(wasSavedSuccessfully2)
                  return@fromCallable Outcome.SUCCESS(Unit)
              else
                  return@fromCallable Outcome.FAILURE(Unit, "failed to save user details")
          }

          return@fromCallable Outcome.SUCCESS(Unit)
      }

    override fun getUserDetails(): FBUserDetailsVModel {
        if(userDetails == null)
            userDetails = getUserDetailsFromSharedPrefs()

        /* if still null crash the app??? */
        if(userDetails == null)
            throw IllegalStateException("userDetails should not be null")

        return userDetails!!
    }

    override fun getUserID(): String? = userDetails.firebaseUID

    override fun <T> getUserDetailsProperty(propertyKey:String):T?{
        return when(propertyKey){
            FBU_USER_DETAILS -> userDetails as? T?
            FBU_ACCOUNT_TYPE -> userDetails?.accountType as? T?
            FBU_FIRST_NAME -> userDetails?.firstName as? T?
            FBU_LAST_NAME -> userDetails?.lastName as? T?
            FBU_FULL_NAME -> userDetails?.fullName as? T?
            FBU_EMAIL -> userDetails?.email as? T?
            FBU_SEX -> userDetails?.sex as? T?
            FBU_AGE -> userDetails?.age as? T?
            FBU_FIREBASE_UID -> userDetails?.firebaseUID as? T?
            FBU_AGORA_UID -> userDetails?.agoraUID as? T?
            FBU_FCM_REGISTRATION_TOKEN -> userDetails?.fcm_registration_token as? T?
            FBU_IMAGE_URI -> userDetails?.imageUri as? T?
            FBU_HISTORY -> userDetails?.history as? T?
            FBU_SPECIALIZATION -> userDetails?.specialization as? T?
            else -> throw IllegalArgumentException("invalid propertyKey")
        }
    }

    override fun clearUserDetails(): Single<Outcome> =
        Single.fromCallable {
            clearUserDetailsFromSharedPrefs()
            return@fromCallable Outcome.SUCCESS(Unit)
        }

    private fun saveUserDetailsToSharedPrefs(userDetails: FBUserDetailsVModel):Boolean{
        var wasSavedSuccessfully:Boolean = false
        try{
            val userDetailsJson:String = gson.toJson(userDetails)
            val encryptedDPDetailsJson:String = cryptoHelper.encrypt(userDetailsJson)
            preferenceHelper.set(USER_DETAILS, encryptedDPDetailsJson)

            wasSavedSuccessfully = true
        }catch (e:Exception){
            Timber.e("error occurred saving userDetails:", e)
        }

        return wasSavedSuccessfully
    }

    @Synchronized
    private fun getUserDetailsFromSharedPrefs(): FBUserDetailsVModel?{
        var userDetails: FBUserDetailsVModel? = null
        try {
            val _userDetailsString:String? = preferenceHelper.get(USER_DETAILS)
            if(_userDetailsString != null){
                val userString:String = cryptoHelper.decrypt(_userDetailsString)
                userDetails = gson.fromJson(userString, FBUserDetailsVModel::class.java)
            }
        }catch(e:Exception){
            Timber.e("error occurred retrieving userDetails:", e)
        }

        return userDetails
    }

    private fun clearUserDetailsFromSharedPrefs(){
        preferenceHelper.clearPreference(USER_DETAILS)
    }
}