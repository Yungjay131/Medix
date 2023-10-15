package app.slyworks.data_lib.helpers.connection_listener

import app.slyworks.data_lib.model.models.ConnectionStatus
import io.reactivex.rxjava3.core.Observable


/**
 * Created by Joshua Sylvanus, 12:39 AM, 08-Oct-2023.
 */
interface IConnectionStatusHelper {
  fun addHandlerForMyConnectionStatus()
  fun listenForAnotherUsersConnectionStatus(userUID: String): Observable<ConnectionStatus>
}