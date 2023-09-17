package app.slyworks.data_lib.helpers.crypto

import app.slyworks.utils_lib.Outcome
import io.reactivex.rxjava3.core.Single


/**
 *Created by Joshua Sylvanus, 9:47 AM, 17-Sep-2023.
 */
class CryptoHelper(private val delegate:ICryptoDelegate) : ICryptoHelper {
   override fun isInitialized():Boolean = delegate.isInitialized()
   override fun init() = delegate.init()
   override fun initAsync():Single<Outcome> = delegate.initAsync()

   override fun hashAsync(text:String):Single<String> = delegate.hashAsync(text)
   override fun hash(text:String):String = delegate.hash(text)

   override fun encryptAsync(text:String): Single<String> = delegate.encryptAsync(text)
   override fun encrypt(text:String):String = delegate.encrypt(text)

   override fun decryptAsync(text:String):Single<String> = delegate.decryptAsync(text)
   override fun decrypt(text:String): String = delegate.decrypt(text)
}