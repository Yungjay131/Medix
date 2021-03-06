package com.slyworks.models.models

/**
 *Created by Joshua Sylvanus, 9:20 AM, 12/13/2021.
 */

/*format for this file would be
Enums,
Interfaces,
SealedClasses,
DataClasses*/
enum class AccountType{
    PATIENT{
        override fun toString(): String {
            return "PATIENT"
        }
    },
    DOCTOR{
        override fun toString(): String {
            return "DOCTOR"
        }
    },
    NOT_SET{
        override fun toString(): String {
            return "NOT_SET"
        }
    };

}