package app.slyworks.data_lib.helpers.auth

import app.slyworks.data_lib.firebase.FirebaseUtils
import app.slyworks.data_lib.model.models.TempUserDetails
import app.slyworks.data_lib.model.view_entities.FBUserDetailsVModel
import app.slyworks.utils_lib.*
import com.google.firebase.auth.*
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.StorageReference
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import timber.log.Timber


class RegistrationHelper(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseMessaging: FirebaseMessaging,
    private val firebaseUtils: FirebaseUtils,
    private val imageProcessor: ImageProcessor,
    private val idHelper: IDHelper,
    private val authStateListener: MAuthStateListener ) {

    private lateinit var user: TempUserDetails
    private var currentFirebaseUserResult: AuthResult? = null

    private val disposables:CompositeDisposable = CompositeDisposable()

    /* if a task fails, try to rollback cause the whole registration process
    is meant to be all or nothing, e.g upload profile pic after registering on Firebase
    if this fails delete the created user on firebase
    */
    fun register(userDetails: TempUserDetails): Single<Outcome> {
        user = userDetails
        return createFirebaseUser()
            .concatMap {
                when {
                    it.isSuccess -> uploadUserProfileImageToFirebaseStorage()
                    else -> deleteUser().concatMap { _ -> Single.just(it) }
                }
            }.concatMap {
                when {
                    it.isSuccess -> getUserProfileImageUrl()
                    else -> deleteUser().concatMap { _ -> Single.just(it) }
                }
            }.concatMap { it ->
                when {
                    it.isSuccess -> createAgoraUser()
                    else -> deleteUserProfileImageFromFirebaseStorage().concatMap { _ -> Single.just(it) }
                }
            }.concatMap {
                when {
                    it.isSuccess -> getFCMTokenFromFirebaseMessaging()
                    else -> Single.just(it)
                }
            }.concatMap {
                when {
                    it.isSuccess -> uploadUserDetailsToFirebaseDB()
                    else -> deleteUserDetailsFromFirebaseDB().concatMap { _ -> Single.just(it) }
                }
            }/*.concatMap {
                when {
                    it.isSuccess -> signOutCreatedUser()
                    else -> Single.just(it)
                }
            }*/

    }

    private fun createFirebaseUser(): Single<Outcome> {
        authStateListener.setEmailAndPassword(user.email!!, user.password!!)

        return Single.create { emitter ->
            firebaseAuth.createUserWithEmailAndPassword(user.email!!, user.password!!)
                .addOnCompleteListener {
                    val o: Outcome
                    if (it.isSuccessful) {
                        currentFirebaseUserResult = it.result!!
                        user.firebaseUID = it.result!!.user!!.uid

                        o = Outcome.SUCCESS(Unit)
                    } else {
                        Timber.e(it.exception)
                        o = Outcome.FAILURE(Unit, it.exception?.message ?: "user was not created")
                    }

                    emitter.onSuccess(o)
                }
        }
    }

    private fun uploadUserProfileImageToFirebaseStorage(): Single<Outcome> =
        Single.create { emitter ->
            val o: Outcome = imageProcessor.getByteArrayFromUri(user.image_uri_init!!)
            if (!o.isSuccess)
                emitter.onSuccess(o)

            val imageByteArray: ByteArray = o.getTypedValue()
            firebaseUtils.getUserProfileImageStorageRef(currentFirebaseUserResult!!.user!!.uid)
                .putBytes(imageByteArray)
                .addOnCompleteListener {
                    val o: Outcome
                    if (it.isSuccessful) {
                        o = Outcome.SUCCESS(Unit)
                    } else {
                        Timber.e(it.exception)
                        o = Outcome.FAILURE(Unit, it.exception?.message ?: "profile image was not uploaded")
                    }

                    emitter.onSuccess(o)
                }
        }

    private fun getUserProfileImageUrl():Single<Outcome> =
        Single.create { emitter ->
            val storageReference:StorageReference =
                firebaseUtils.getUserProfileImageStorageRef(currentFirebaseUserResult!!.user!!.uid)

            storageReference.downloadUrl
                .addOnCompleteListener { it ->
                    val o: Outcome
                    if (it.isSuccessful) {
                        user.imageUri = it.result.toString()
                        o = Outcome.SUCCESS(Unit)
                    } else {
                        Timber.e(it.exception)
                        o = Outcome.FAILURE(Unit, it.exception?.message ?: "profile image url was not downloaded")
                    }

                    emitter.onSuccess(o)
                }
        }

    private fun uploadUserDetailsToFirebaseDB(): Single<Outcome> =
        Single.create { emitter ->
            val user: FBUserDetailsVModel =
                FBUserDetailsVModel(
                    accountType = user.accountType.toString(),
                    firstName = user.firstName!!,
                    lastName = user.lastName!!,
                    fullName = "${user.firstName} ${user.lastName}",
                    email = user.email!!,
                    sex = user.gender.toString(),
                    age = user.dob!!,
                    firebaseUID = user.firebaseUID!!,
                    agoraUID = user.agoraUID!!,
                    fcm_registration_token = user.FBRegistrationToken!!,
                    imageUri = user.imageUri!!,
                    history = this.user.history,
                    specialization = this.user.specialization
                )

            val databaseAddress: String = currentFirebaseUserResult!!.user!!.uid

            firebaseUtils.getUserDataForUIDRef(databaseAddress)
                .setValue(user)
                .addOnCompleteListener {
                    val r: Outcome

                    if (it.isSuccessful) {
                        r = Outcome.SUCCESS(value = "user details successfully uploaded")
                    } else {
                        Timber.e("uploadUserDetailsToFirebaseDB2: uploading user details to multiple locations in the DB, completed but failed")
                        r = Outcome.FAILURE(
                            value = "uploading user details to Firebase failed",
                            reason = it.exception?.message ?: "uploading user details failed")
                    }

                    emitter.onSuccess(r)
                }
        }

    private fun createAgoraUser(): Single<Outcome> =
        Single.fromCallable {
            user.agoraUID = idHelper.generateNewUserID()
            Outcome.SUCCESS(Unit, user.agoraUID)
        }

    private fun getFCMTokenFromFirebaseMessaging(): Single<Outcome> {
        return Single.create { emitter ->
            firebaseMessaging.getToken()
                .addOnCompleteListener {
                    val o: Outcome
                    if (it.isSuccessful) {
                        user.FBRegistrationToken = it.result

                        o = Outcome.SUCCESS(Unit,it.result)
                    } else {
                        Timber.e(it.exception)

                        o = Outcome.FAILURE(Unit, it.exception?.message ?: "getting FCM token failed")
                    }

                    emitter.onSuccess(o)
                }
        }
    }


    private fun logoutCreatedUserFromFirebaseAuth():Single<Outcome> =
        Single.fromCallable{
            firebaseAuth.signOut()
            Outcome.SUCCESS(value = "created user logged out successfully")
        }

    private fun deleteUser(user: TempUserDetails = this.user):Single<Outcome>{
        if(firebaseAuth.currentUser == null)
            return Single.just(Outcome.FAILURE(false, "no user currently signed in"))

        return Single.create{ emitter ->
            firebaseAuth.currentUser!!.delete()
                .addOnCompleteListener {
                    val o: Outcome
                    if(it.isSuccessful){
                        Timber.e("deleteUser: delete successful")
                        o = Outcome.SUCCESS(Unit)
                    }else{
                        Timber.e("deleteUser: delete unsuccessful", it.exception)

                        o = Outcome.FAILURE(false, it.exception?.message ?: "deleting user failed")
                    }

                    emitter.onSuccess(o)
                }
        }

    }

    private fun deleteUserProfileImageFromFirebaseStorage():Single<Outcome>{
        if(currentFirebaseUserResult == null || currentFirebaseUserResult!!.user?.uid == null)
            return Single.just(
                Outcome.FAILURE(false,"no user currently signed in")
            )

        return Single.create { emitter ->
            val storageReference = firebaseUtils.getUserProfileImageStorageRef(currentFirebaseUserResult!!.user!!.uid)
            storageReference.delete()
                .addOnCompleteListener {
                    val o: Outcome
                    if(it.isSuccessful){
                        Timber.e("deleteUserProfileImage: delete successful")
                        o = Outcome.SUCCESS(true)
                    }else{
                        Timber.e(it.exception )
                        o = Outcome.FAILURE(false, it.exception?.message ?: "deleting profile image failed")
                    }

                    emitter.onSuccess(o)
                }
        }

    }

    private fun deleteUserDetailsFromFirebaseDB():Single<Outcome>{
        if(currentFirebaseUserResult?.user?.uid == null)
            return Single.just(
                Outcome.FAILURE(false,"no user currently signed in")
            )

        return Single.create { emitter ->
            val databaseAddress: String = currentFirebaseUserResult!!.user!!.uid
            firebaseUtils.getUserDataForUIDRef(databaseAddress)
                .setValue(null)
                .addOnCompleteListener {
                    val o: Outcome
                    if(it.isSuccessful){
                        Timber.e("deleteUserDetailsFromDB: delete successful" )
                        o = Outcome.SUCCESS(true)
                    }else{
                        Timber.e(it.exception)
                        o = Outcome.FAILURE(false, it.exception?.message ?: "deleting user details unsuccessful")
                    }

                    emitter.onSuccess(o)
                }
        }

    }

}

