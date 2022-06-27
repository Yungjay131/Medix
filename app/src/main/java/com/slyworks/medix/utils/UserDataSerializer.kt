package com.slyworks.medix.utils

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import androidx.datastore.preferences.protobuf.InvalidProtocolBufferException
import com.slyworks.medix.UserDetails
import java.io.InputStream
import java.io.OutputStream


/**
 *Created by Joshua Sylvanus, 12:47 PM, 1/8/2022.
 */
object UserDataSerializer : Serializer<UserDetails> {
    //region Vars
    override val defaultValue: UserDetails
        get() = UserDetails.getDefaultInstance()
    //endregion

    override suspend fun readFrom(input: InputStream): UserDetails {
        try{
            return UserDetails.parseFrom(input)
        }catch(ipbe: InvalidProtocolBufferException){
            throw CorruptionException("cannot read proto file", ipbe)
        }
    }

    override suspend fun writeTo(t: UserDetails, output: OutputStream) {
          t.writeTo(output)
    }
}