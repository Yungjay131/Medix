package app.slyworks.data_lib.repositories.splash

import io.reactivex.rxjava3.core.Single


/**
 *Created by Joshua Sylvanus, 6:02 PM, 16-Sep-2023.
 */
interface ISplashRepository {
    fun getLoggedInStatus(): Boolean
}