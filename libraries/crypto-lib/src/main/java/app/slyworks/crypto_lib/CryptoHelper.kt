package app.slyworks.crypto_lib

import android.util.Base64
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import java.nio.charset.Charset
import java.security.spec.AlgorithmParameterSpec
import java.security.spec.KeySpec
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * Created by Joshua Sylvanus, 7:04 AM, 08-Oct-22.
 */
class CryptoHelper(private var config: CryptoConfig = CryptoConfig.DEFAULT()) {
    //region Vars
    private var isInitialized:Boolean = false

    private lateinit var secretKeyFactory:SecretKeyFactory
    private lateinit var cipher:Cipher
    //endregion

    fun isInitialized():Boolean = isInitialized

    fun initializeAsync(details:CryptoDetails):Completable =
        Completable.fromAction {
            if(isInitialized)
              return@fromAction

            config = object : CryptoConfig{
                override val encryptionAlgorithmShort: String
                    get() = details.eas
                override val encryptionAlgorithm: String
                    get() = details.ea
                override val encryptionKey: SecretKey
                    get() = SecretKeySpec(Base64.decode(details.ek, Base64.DEFAULT), details.eas)
                override val encryptionKeySize: Int
                    get() = details.eks
                override val encryptionIV: String
                    get() =  details.eiv
                override val encryptionSpec: AlgorithmParameterSpec
                    get() = IvParameterSpec(Base64.decode(details.es, Base64.DEFAULT))
                override val hashSalt: ByteArray
                    get() = details.hs.toByteArray()
                override val hashAlgorithm: String
                    get() = details.ha
                override val hashIterationCount: Int
                    get() = details.hic
                override val hashLength: Int
                    get() = details.hl
            }

            secretKeyFactory = SecretKeyFactory.getInstance(config.hashAlgorithm)
            cipher = Cipher.getInstance(config.encryptionAlgorithm)
            isInitialized = true
        }

    fun hashAsync(str:String): Observable<String>
    = Observable.fromCallable { hash(str) }

    fun hash(str:String): String {
        val keySpec: KeySpec = PBEKeySpec(str.toCharArray(), config.hashSalt, config.hashIterationCount /*15500 65536*/, config.hashLength /*128*/ )
        val hash:ByteArray = secretKeyFactory.generateSecret(keySpec).getEncoded()
        return String(Base64.encode(hash, Base64.DEFAULT), Charset.forName("UTF-8") ).trim()
    }

    fun encryptAsync(str:String):Observable<String>
    = Observable.fromCallable { encrypt(str) }

    fun encrypt(str:String): String {
        val strByteArray:ByteArray = str.toByteArray()
        cipher.init(Cipher.ENCRYPT_MODE, config.encryptionKey, config.encryptionSpec)
        val encryptedText:ByteArray = cipher.doFinal(strByteArray)
        return String(Base64.encode(encryptedText, Base64.DEFAULT), Charset.forName("UTF-8") ).trim()
    }

    fun decryptAsync(str:String):Observable<String>
    = Observable.fromCallable { decrypt(str) }

    fun decrypt(str:String):String{
        val strByteArray:ByteArray = Base64.decode(str, Base64.DEFAULT)
        cipher.init(Cipher.DECRYPT_MODE, config.encryptionKey, config.encryptionSpec)
        val decryptedText:ByteArray = cipher.doFinal(strByteArray)
        return String(decryptedText, Charset.forName("UTF-8") ).trim()
    }
}