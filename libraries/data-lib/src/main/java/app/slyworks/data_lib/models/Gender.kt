package app.slyworks.data_lib.models

enum class Gender {
    MALE{
        override fun toString(): String {
            return "male"
        }
    },
    FEMALE{
        override fun toString(): String {
            return "female"
        }
    },
    NOT_SET{
        override fun toString(): String {
            return "not_set"
        }
    }
}