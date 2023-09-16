package app.slyworks.di_base_lib

import javax.inject.Scope


/**
 *Created by Joshua Sylvanus, 8:46 PM, 24/02/2023.
 */
/**@scope for object bound to BroadcastReceivers' lifecycle*/
@Scope
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
annotation class BroadcastReceiverScope
