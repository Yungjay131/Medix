package app.slyworks.data_lib.model


/**
 *Created by Joshua Sylvanus, 10:55 AM, 28/06/2022.
 */
data class ConnectionStatus(
    var status:Boolean = false,
    var timestamp:Long = 0L) {

    constructor():this(
        status = false,
        timestamp = 0L
    )
}
