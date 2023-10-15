package app.slyworks.data_lib.model.view_entities

/**
 * Created by Joshua Sylvanus, 7:36 AM, 1/3/2022.
 */
/*
* "rules": {
    "users":{
       "$uid": {
        ".read":"$uid === auth.uid",
        ".write": "$uid === auth.uid"
      }
    },
    "user-names":{
       "$uid": {
        ".read":"$uid === auth.uid",
        ".write": "$uid === auth.uid"
      }
    }
  }*/

enum class OTPVerificationStage{
    ENTER_OTP, PROCESSING, OTP_RESENT, VERIFICATION_SUCCESS,VERIFICATION_FAILURE
}