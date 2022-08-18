package com.slyworks.medix


/**
 *Created by Joshua Sylvanus, 8:51 PM, 09/08/2022.
 */
class UniversalExceptionHandler : Thread.UncaughtExceptionHandler {
    //region Vars
    //endregion
    override fun uncaughtException(t: Thread, e: Throwable) { }
}