package app.slyworks.data_lib.helpers.crypto

import android.content.Context
import android.os.Build
import android.security.KeyPairGeneratorSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import app.slyworks.utils_lib.Outcome
import app.slyworks.utils_lib.PreferenceHelper
import io.reactivex.rxjava3.core.Single
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.math.BigInteger
import java.nio.charset.Charset
import java.security.Key
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.SecureRandom
import java.util.*
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.security.auth.x500.X500Principal


/**
 *Created by Joshua Sylvanus, 10:56 AM, 17-Sep-2023.
 */
class KeystoreLegacyCryptoDelegate(private val context:Context,
                                   private val preferenceHelper: PreferenceHelper) : ICryptoDelegate {
    //region Vars
    private val KEY_ANDROID_KEYSTORE = "AndroidKeyStore"
    private val KEY_ALIAS = "app.slyworks.KEY_ALIAS"
    private val KEY_ENCRYPTED_KEY = "app.slyworks.KEY_ENCRYPTED_KEY"
    private val AES_MODE = "AES/GCM/NoPadding"/*"AES/ECB/PKCS7Padding"*/
    private val RSA_MODE = "RSA/ECB/PKCS1Padding"

    private val FIXED_IV:String = "qazNjvk5oCy5"

    private var isInitialized:Boolean = false

    private lateinit var keyStore: KeyStore

    private val gcmParameterSpec: GCMParameterSpec =
        GCMParameterSpec(128, FIXED_IV.toByteArray())

    private lateinit var secretKey: Key
    //endregion

    override fun isInitialized(): Boolean = isInitialized

    override fun init() {
        initKeyStore()
        initAESKey()

        isInitialized = true
    }

    override fun initAsync(): Single<Outcome> =
        Single.fromCallable{
            initKeyStore()
            initAESKey()

            isInitialized = true

            return@fromCallable Outcome.SUCCESS(Unit)
        }.onErrorReturn {
            Outcome.FAILURE(it.message)
        }

    private fun initKeyStore(){
        keyStore = KeyStore.getInstance(KEY_ANDROID_KEYSTORE)
        keyStore.load(null)

        if (!keyStore.containsAlias(KEY_ALIAS)) {
            // Generate a key pair for encryption
            val start: Calendar = Calendar.getInstance()
            val end: Calendar = Calendar.getInstance()
            end.add(Calendar.YEAR, 30)

            val spec = KeyPairGeneratorSpec.Builder(context)
                .setAlias(KEY_ALIAS)
                .setSubject(X500Principal("CN=$KEY_ALIAS"))
                .setSerialNumber(BigInteger.TEN)
                .setStartDate(start.getTime())
                .setEndDate(end.getTime())
                .build()

            val keyPairGenerator: KeyPairGenerator =
                KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, KEY_ANDROID_KEYSTORE)
            keyPairGenerator.initialize(spec)
            keyPairGenerator.generateKeyPair()
        }
    }

    private fun initAESKey(){
        var encryptedKeyB64:String? = preferenceHelper.get(KEY_ENCRYPTED_KEY)
        if(encryptedKeyB64  == null){
            val key:ByteArray = ByteArray(16)

            val secureRandom: SecureRandom = SecureRandom()
            secureRandom.nextBytes(key)

            val encryptedKey:ByteArray = rsaEncrypt(key)

            encryptedKeyB64 = Base64.encodeToString(encryptedKey, Base64.NO_WRAP)
            preferenceHelper.set(KEY_ENCRYPTED_KEY, encryptedKeyB64)
        }

        val encryptedKey: ByteArray = Base64.decode(encryptedKeyB64, Base64.NO_WRAP)
        val key:ByteArray = rsaDecrypt(encryptedKey)
        secretKey = SecretKeySpec(key, "AES")
    }

    private fun rsaEncrypt(secret: ByteArray): ByteArray {
        val privateKeyEntry:KeyStore.PrivateKeyEntry =
            keyStore.getEntry(KEY_ALIAS, null) as KeyStore.PrivateKeyEntry

        // Encrypt the text
        val cipher: Cipher = getRSACipher()
        cipher.init(Cipher.ENCRYPT_MODE, privateKeyEntry.certificate.publicKey)

        val outputStream: ByteArrayOutputStream = ByteArrayOutputStream()

        val cipherOutputStream: CipherOutputStream = CipherOutputStream(outputStream, cipher)
        cipherOutputStream.write(secret)
        cipherOutputStream.close()

        return outputStream.toByteArray()
    }

    private fun rsaDecrypt(encrypted: ByteArray): ByteArray {
        val privateKeyEntry:KeyStore.PrivateKeyEntry =
            keyStore.getEntry(KEY_ALIAS, null) as KeyStore.PrivateKeyEntry

        val cipher: Cipher = getRSACipher()
        cipher.init(Cipher.DECRYPT_MODE, privateKeyEntry.getPrivateKey())

        val cipherInputStream: CipherInputStream = CipherInputStream(ByteArrayInputStream(encrypted), cipher)

        val values: ArrayList<Byte> = ArrayList()

        var nextByte: Int
        while (cipherInputStream.read().also { nextByte = it } != -1) {
            values.add(nextByte.toByte())
        }

        val bytes = ByteArray(values.size)
        for (i in bytes.indices) {
            bytes[i] = values[i]
        }
        return bytes
    }

    private fun getRSACipher():Cipher{
        var cipher:Cipher? = null
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
                cipher =  Cipher.getInstance(RSA_MODE, "AndroidOpenSSL")
            else
                cipher = Cipher.getInstance(RSA_MODE, "AndroidKeyStoreBCWorkaround")
        } catch (exception: Exception) {
            throw RuntimeException("getCipher: Failed to get an instance of Cipher", exception)
        }

        return cipher
    }

    private fun getAESCipher():Cipher{
        var cipher:Cipher? = null
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P)
                cipher =  Cipher.getInstance(AES_MODE, "BC")
            else
                cipher = Cipher.getInstance(AES_MODE)
        } catch (exception: Exception) {
            throw RuntimeException("getCipher: Failed to get an instance of Cipher", exception)
        }

        return cipher
    }

    override fun hashAsync(text: String): Single<String> =
        Single.fromCallable { hash(text) }

    override fun hash(text: String): String {
        TODO("Not yet implemented")
    }

    override fun encryptAsync(text: String): Single<String> =
        Single.fromCallable { encrypt(text) }

    override fun encrypt(text: String): String {
        if(text.isEmpty())
            return text

        val textByteArray:ByteArray = text.toByteArray()

        val cipher:Cipher = getAESCipher()
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmParameterSpec)

        val encryptedText:ByteArray = cipher.doFinal(textByteArray)
        return String(Base64.encode(encryptedText, Base64.NO_WRAP), Charset.forName("UTF-8") ).trim()

    }

    override fun decryptAsync(text: String): Single<String> =
        Single.fromCallable { decrypt(text) }

    override fun decrypt(text: String): String {
        if(text.isEmpty())
            return text

        val strByteArray:ByteArray = Base64.decode(text, Base64.NO_WRAP)

        val cipher:Cipher = getAESCipher()
        cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmParameterSpec)

        val decryptedText:ByteArray = cipher.doFinal(strByteArray)
        return String(decryptedText, Charset.forName("UTF-8") ).trim()

    }
}