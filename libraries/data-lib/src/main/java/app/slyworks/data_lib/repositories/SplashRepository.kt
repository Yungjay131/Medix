package app.slyworks.data_lib.repositories

import app.slyworks.data_lib.helpers.auth.ILoginHelper
import io.reactivex.rxjava3.core.Single


/**
 * Created by Joshua Sylvanus, 6:01 PM, 16-Sep-2023.
 */
class SplashRepository(private val loginHelper: ILoginHelper)
    : ISplashRepository {

    override fun getLoggedInStatus():Boolean = loginHelper.getLoggedInStatus()
}

