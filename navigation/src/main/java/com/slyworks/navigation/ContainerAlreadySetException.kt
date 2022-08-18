package com.slyworks.navigation

/**
 *Created by Joshua Sylvanus, 10:50 AM, 21/06/2022.
 */
internal class ContainerAlreadySetException(): Exception(){
    override val message: String
        get() = "a FragmentContainer has already been set for this transaction"
}