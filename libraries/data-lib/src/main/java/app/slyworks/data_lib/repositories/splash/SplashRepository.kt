package app.slyworks.data_lib.repositories.splash

import app.slyworks.data_lib.helpers.auth.ILoginHelper


/**
 * Created by Joshua Sylvanus, 6:01 PM, 16-Sep-2023.
 */
class SplashRepository(private val loginHelper: ILoginHelper)
    : ISplashRepository {

    override fun getLoggedInStatus():Boolean = loginHelper.getLoggedInStatus()
}

