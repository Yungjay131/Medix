package app.slyworks.data_lib.model.models

import app.slyworks.utils_lib.ACCOUNT_TYPE_NOT_SET

/**
 * Created by Joshua Sylvanus, 9:20 AM, 12/13/2021.
 */

enum class AccountType{
    PATIENT{
        override fun toString(): String {
            return app.slyworks.utils_lib.PATIENT
        }
    },
    DOCTOR{
        override fun toString(): String {
            return app.slyworks.utils_lib.DOCTOR
        }
    },
    NOT_SET{
        override fun toString(): String {
            return ACCOUNT_TYPE_NOT_SET
        }
    };

}