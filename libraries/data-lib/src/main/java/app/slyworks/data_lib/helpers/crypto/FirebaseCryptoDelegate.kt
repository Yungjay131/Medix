package app.slyworks.data_lib.helpers.crypto

import android.util.Base64
import app.slyworks.utils_lib.Outcome
import com.google.firebase.firestore.FirebaseFirestore
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import timber.log.Timber
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
class FirebaseCryptoDelegate(private var firebaseFirestore: FirebaseFirestore)
    : ICryptoDelegate {
    //region Vars
    private var isInitialized:Boolean = false

    private var config:CryptoConfig? = null
    private lateinit var secretKeyFactory:SecretKeyFactory
    private lateinit var cipher:Cipher
    //endregion

    override fun isInitialized():Boolean = isInitialized

    override fun init() {
        if(isInitialized)
            return

        firebaseFirestore.collection("encryption_details")
            .document("details")
            .get()
            .addOnCompleteListener {
                if(!it.isSuccessful){
                    Timber.e("failed to retrieve encryption details", it.exception)
                }

                val data: Map<String, Any> = it.result!!.data!!
                config =
                    CryptoConfig(
                        encryptionAlgorithmShort = data["eas"] as String,
                        encryptionAlgorithm = data["ea"] as String,
                        encryptionKey = SecretKeySpec(Base64.decode(data["ek"] as String, Base64.DEFAULT), data["eas"] as String),
                        encryptionKeySize = (data["eks"] as String).toInt(),
                        encryptionIV = data["eiv"] as String,
                        encryptionSpec = IvParameterSpec(Base64.decode(data["es"] as String, Base64.DEFAULT)),
                        hashSalt = (data["hs"] as String).toByteArray(),
                        hashAlgorithm = data["ha"] as String,
                        hashIterationCount = (data["hic"] as String).toInt(),
                        hashLength = (data["hl"] as String).toInt()
                    )

                secretKeyFactory = SecretKeyFactory.getInstance(config!!.hashAlgorithm)
                cipher = Cipher.getInstance(config!!.encryptionAlgorithm)

                isInitialized = true
            }
    }

    override fun initAsync():Single<Outcome> =
        Single.create<Outcome> { emitter ->
            if(isInitialized)
                emitter.onSuccess(Outcome.SUCCESS(Unit))

            firebaseFirestore.collection("encryption_details")
                .document("details")
                .get()
                .addOnCompleteListener {
                    val o:Outcome
                    if(it.isSuccessful){
                        o = Outcome.SUCCESS(it.result!!.data)
                    }else{
                        Timber.e("failed to retrieve encryption details", it.exception)
                        o = Outcome.FAILURE(Unit, it.exception?.message)
                    }

                    emitter.onSuccess(o)
                }
        }.map {
            if(it.isFailure)
                return@map it
            else{
                val data:Map<String, Any> = it.getTypedValue<Map<String, Any>>()
                config =
                    CryptoConfig(
                        encryptionAlgorithmShort = data["eas"] as String,
                        encryptionAlgorithm = data["ea"] as String,
                        encryptionKey = SecretKeySpec(Base64.decode(data["ek"] as String, Base64.DEFAULT), data["eas"] as String),
                        encryptionKeySize = (data["eks"] as String).toInt(),
                        encryptionIV = data["eiv"] as String,
                        encryptionSpec = IvParameterSpec(Base64.decode(data["es"] as String, Base64.DEFAULT)),
                        hashSalt = (data["hs"] as String).toByteArray(),
                        hashAlgorithm = data["ha"] as String,
                        hashIterationCount = (data["hic"] as String).toInt(),
                        hashLength = (data["hl"] as String).toInt())

                secretKeyFactory = SecretKeyFactory.getInstance(config!!.hashAlgorithm)
                cipher = Cipher.getInstance(config!!.encryptionAlgorithm)

                isInitialized = true

                return@map Outcome.SUCCESS(Unit)
            }
        }

    override fun hashAsync(text:String): Single<String>
    = Single.fromCallable { hash(text) }

    override fun hash(text:String): String {
        val keySpec: KeySpec = PBEKeySpec(text.toCharArray(), config!!.hashSalt, config!!.hashIterationCount, config!!.hashLength )
        val hash:ByteArray = secretKeyFactory.generateSecret(keySpec).getEncoded()
        return String(Base64.encode(hash, Base64.DEFAULT), Charset.forName("UTF-8") ).trim()
    }

    override fun encryptAsync(text:String):Single<String>
    = Single.fromCallable { encrypt(text) }

    override fun encrypt(text:String): String {
        val strByteArray:ByteArray = text.toByteArray()
        cipher.init(Cipher.ENCRYPT_MODE, config!!.encryptionKey, config!!.encryptionSpec)
        val encryptedText:ByteArray = cipher.doFinal(strByteArray)
        return String(Base64.encode(encryptedText, Base64.DEFAULT), Charset.forName("UTF-8") ).trim()
    }

    override fun decryptAsync(text:String):Single<String>
    = Single.fromCallable { decrypt(text) }

    override fun decrypt(text:String):String{
        val strByteArray:ByteArray = Base64.decode(text, Base64.DEFAULT)
        cipher.init(Cipher.DECRYPT_MODE, config!!.encryptionKey, config!!.encryptionSpec)
        val decryptedText:ByteArray = cipher.doFinal(strByteArray)
        return String(decryptedText, Charset.forName("UTF-8") ).trim()
    }
}