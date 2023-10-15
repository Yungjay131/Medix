package app.slyworks.data_lib.repositories.login

import app.slyworks.data_lib.helpers.auth.ILoginHelper
import app.slyworks.data_lib.repositories.login.ILoginRepository
import app.slyworks.utils_lib.Outcome
import io.reactivex.rxjava3.core.Single


/**
 * Created by Joshua Sylvanus, 9:04 AM, 23-Sep-2023.
 */
internal class LoginRepository(private val loginHelper: ILoginHelper) : ILoginRepository {
    override fun login(email: String, password: String): Single<Outcome> =
        loginHelper.loginUser(email, password)

    override fun handleForgotPassword(email: String): Single<Outcome> =
        loginHelper.handleForgotPassword(email)
}