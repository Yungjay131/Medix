package app.slyworks.data_lib.repositories.home

import app.slyworks.data_lib.helpers.storage.IUserDetailsHelper
import app.slyworks.data_lib.helpers.storage.UserDetailsHelper
import app.slyworks.data_lib.helpers.users.IUsersHelper
import app.slyworks.data_lib.helpers.users.UsersHelper
import app.slyworks.data_lib.model.view_entities.FBUserDetailsVModel
import app.slyworks.utils_lib.FBU_FIRST_NAME
import app.slyworks.utils_lib.FBU_IMAGE_URI
import app.slyworks.utils_lib.FBU_LAST_NAME
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers


/**
 * Created by Joshua Sylvanus, 7:26 PM, 11-Oct-2023.
 */
class HomeRepository(private val userDetailsHelper: IUserDetailsHelper,
                     private val usersHelper: IUsersHelper, private val callHistory:) : IHomeRepository {

    override fun getUserFullName(): String =
        userDetailsHelper.getUserDetailsProperty<String>(FBU_FIRST_NAME)!! +
        userDetailsHelper.getUserDetailsProperty<String>(FBU_LAST_NAME)!!

    override fun getUserProfilePicUri(): String =
        userDetailsHelper.getUserDetailsProperty<String>(FBU_IMAGE_URI)!!

    override fun observeUserDetails(): Observable<FBUserDetailsVModel> =
        usersHelper.listenForChangesToUsersData()
}