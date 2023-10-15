package app.slyworks.data_lib.helpers.users

import app.slyworks.data_lib.firebase.FirebaseUtils
import app.slyworks.data_lib.helpers.storage.IUserDetailsHelper
import app.slyworks.data_lib.model.view_entities.FBUserDetailsVModel
import app.slyworks.data_lib.model.view_entities.FBUserDetailsWrapper
import app.slyworks.data_lib.firebase.MChildEventListener
import app.slyworks.firebase_commons_lib.MValueEventListener
import app.slyworks.utils_lib.FBU_FIREBASE_UID
import app.slyworks.utils_lib.Outcome
import com.google.firebase.database.DataSnapshot
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import timber.log.Timber


/**
 * Created by Joshua Sylvanus, 5:35 PM, 07-Oct-2023.
 */
class UsersHelper(private val firebaseUtils: FirebaseUtils,
                  private val userDetailsHelper: IUserDetailsHelper) : IUsersHelper {
    //region Vars
    //endregion

    override fun sendFCMTokenToServer(token:String):Single<Outcome>{
        return Single.create { emitter ->
            val uid:String = userDetailsHelper.getUserDetailsProperty<String>(FBU_FIREBASE_UID)!!
            firebaseUtils.getFCMRegistrationTokenRef(uid)
                .setValue(token)
                .addOnCompleteListener {
                    if(it.isSuccessful){
                        emitter.onSuccess(Outcome.SUCCESS(Unit))
                    }else{
                        Timber.e(it.exception)
                        emitter.onSuccess(Outcome.FAILURE(Unit, "an error occurred"))
                    }
                }
        }
    }

    override fun updateUsersData(details:FBUserDetailsVModel):Single<Outcome>{
        return Single.create { emitter ->
            val uid:String = userDetailsHelper.getUserDetailsProperty<String>(FBU_FIREBASE_UID)!!
            firebaseUtils.getUserDataForUIDRef(uid)
                .setValue(details)
                .addOnCompleteListener {
                    if(it.isSuccessful){
                        emitter.onSuccess(Outcome.SUCCESS(Unit))
                    }else{
                        Timber.e(it.exception)

                        emitter.onSuccess(Outcome.FAILURE(Unit, "an error occurred retrieving your details"))
                    }
                }
        }
    }

    override fun getAnotherUsersData(uid:String): Single<Outcome>{
        return Single.create { emitter ->
            firebaseUtils.getUserDataForUIDRef(uid)
                .get()
                .addOnCompleteListener {
                    if(it.isSuccessful){
                        val user:FBUserDetailsVModel = it.result!!.getValue(FBUserDetailsVModel::class.java)!!
                        emitter.onSuccess(Outcome.SUCCESS(user))
                    }else{
                        Timber.e(it.exception)

                        emitter.onSuccess(Outcome.FAILURE(Unit))
                    }
                }
        }
    }

    override fun listenForChangesToUsersData():Observable<FBUserDetailsVModel>{
       return Observable.create { emitter ->
           val func:(DataSnapshot) -> Unit = {
               val newUserDetails:FBUserDetailsVModel = it.getValue(FBUserDetailsVModel::class.java)!!
               emitter.onNext(newUserDetails)
           }

           val uid:String = userDetailsHelper.getUserDetailsProperty<String>(FBU_FIREBASE_UID)!!
           firebaseUtils.getUserDataForUIDRef(uid)
               .addValueEventListener(
                   MValueEventListener(
                       onDataChangeFunc = func
                   )
               )
       }
    }

    override fun listenForChangesToAnotherUsersData(userUID:String): Observable<FBUserDetailsVModel> {
        return Observable.create { emitter ->
            val func:(DataSnapshot) -> Unit = {
                val user:FBUserDetailsVModel = it.getValue(FBUserDetailsVModel::class.java)!!
                emitter.onNext(user)
            }

            firebaseUtils.getUserDataForUIDRef(userUID)
                .addValueEventListener(
                    MValueEventListener(
                        onDataChangeFunc = func
                    )
                )
        }
    }

    override fun getAllDoctorsData():Single<Outcome>{
        return Single.create { emitter ->
            firebaseUtils.getAllDoctorsRef()
                .get()
                .addOnCompleteListener {
                    if(it.isSuccessful){
                        val list:MutableList<FBUserDetailsVModel> = mutableListOf()
                        it.result!!.children.forEach { child ->
                            val doctor: FBUserDetailsWrapper = child.getValue(FBUserDetailsWrapper::class.java)!!
                            list.add(doctor.details)
                        }

                        emitter.onSuccess(Outcome.SUCCESS(list))
                    }else{
                        Timber.e(it.exception)

                        emitter.onSuccess(Outcome.FAILURE(Unit, it.exception?.message))
                    }
                }
        }
    }

    override fun listenForNewDoctors():Observable<List<FBUserDetailsVModel>>{
        return Observable.create { emitter ->
            val func:(DataSnapshot) -> Unit = {
                val list:MutableList<FBUserDetailsVModel> = mutableListOf()
                it.children.forEach { child ->
                    val doctor: FBUserDetailsWrapper = child.getValue(FBUserDetailsWrapper::class.java)!!
                    list.add(doctor.details)
                }

                emitter.onNext(list)
            }

            firebaseUtils.getAllDoctorsRef()
                .addChildEventListener(
                    MChildEventListener(
                        onChildAddedFunc = func
                    )
                )
        }
    }
}