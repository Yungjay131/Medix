package app.slyworks.data_lib.helpers.crypto

import app.slyworks.utils_lib.Outcome
import io.reactivex.rxjava3.core.Single


/**
 * Created by Joshua Sylvanus, 10:25 AM, 17-Sep-2023.
 */
interface ICryptoDelegate {
    fun isInitialized():Boolean
    fun init()
    fun initAsync(): Single<Outcome>
    fun hashAsync(text:String): Single<String>
    fun hash(text:String): String
    fun encryptAsync(text:String): Single<String>
    fun encrypt(text:String): String
    fun decryptAsync(text:String): Single<String>
    fun decrypt(text:String):String
}