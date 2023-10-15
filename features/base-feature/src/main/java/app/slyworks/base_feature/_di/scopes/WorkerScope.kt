package app.slyworks.base_feature._di.scopes

import javax.inject.Scope


/**
 *Created by Joshua Sylvanus, 8:46 PM, 16/08/2022.
 */
/**@scope for object bound to WorkManager Worker lifecycle*/
@Scope
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
annotation class WorkerScope
