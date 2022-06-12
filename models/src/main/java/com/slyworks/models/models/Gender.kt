package com.slyworks.models.models

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
    }
}