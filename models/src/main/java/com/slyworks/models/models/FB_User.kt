package com.slyworks.models.models

sealed class FB_User {
    data class FB_Patient(
        var uid: String,
        var user_type: String,
        var fullname: String,
        var email: String,
        var history: Array<String>?,
        var medications: Array<String>?,
        var imageUri: String
    ) : FB_User() {
        constructor() : this("", "", "", "", emptyArray(), emptyArray(), "")

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as FB_Patient

            if (uid != other.uid) return false

            return true
        }

        override fun hashCode(): Int {
            return uid.hashCode()
        }

    }

    data class FB_Doctor(
        var uid: String,
        var user_type: String,
        var fullname: String,
        var email: String,
        var specialization: String?,
        var imageUri: String
    ) : FB_User() {
        constructor() : this("", "", "", "", "", "")
    }
}