package app.slyworks.data_lib.helpers.crypto

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.annotation.RequiresApi
import app.slyworks.utils_lib.Outcome
import io.reactivex.rxjava3.core.Single
import java.nio.charset.Charset
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec


/**
 * Created by Joshua Sylvanus, 10:28 AM, 17-Sep-2023.
 */
@RequiresApi(Build.VERSION_CODES.M)
class KeystoreCryptoDelegate : ICryptoDelegate {
    //region Vars
    private val KEY_ANDROID_KEYSTORE = "AndroidKeyStore"
    private val KEY_ALIAS = "app.slyworks.KEY_ALIAS"
    private val ENCRYPTION = "AES/GCM/NoPadding"

    private val FIXED_IV:String = "qazNjvk5oCy5"

    private var isInitialized:Boolean = false

    private val gcmParameterSpec: GCMParameterSpec =
        GCMParameterSpec(128, FIXED_IV.toByteArray())

    private lateinit var secretKey: SecretKey
    //endregion

    override fun isInitialized(): Boolean = isInitialized

    override fun init() {
        if(isInitialized)
            return

       _init()
    }

    override fun initAsync(): Single<Outcome> =
        Single.fromCallable{
            if(isInitialized)
                return@fromCallable Outcome.SUCCESS(Unit)

            _init()
            return@fromCallable Outcome.SUCCESS(Unit)
        }

    private fun _init(){
        val keyStore: KeyStore = KeyStore.getInstance(KEY_ANDROID_KEYSTORE)
        keyStore.load(null)

        if (!keyStore.containsAlias(KEY_ALIAS)) {
            val keyGenerator: KeyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEY_ANDROID_KEYSTORE)

            val keyGenParameterSpec: KeyGenParameterSpec =
                KeyGenParameterSpec.Builder(KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setRandomizedEncryptionRequired(false)
                    .build()

            keyGenerator.init(keyGenParameterSpec)
            keyGenerator.generateKey()
        }

        secretKey = keyStore.getKey(KEY_ALIAS, null) as SecretKey

        isInitialized = true
    }

    override fun hashAsync(text: String): Single<String>
    = Single.fromCallable{ hash(text) }

    override fun hash(text: String): String {
        TODO("Not yet implemented")
    }

    override fun encryptAsync(text: String): Single<String> =
        Single.fromCallable { encrypt(text) }

    override fun encrypt(text: String): String {
        if(text.isEmpty())
            return text

        val textByteArray:ByteArray = text.toByteArray()

        val cipher: Cipher = Cipher.getInstance(ENCRYPTION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmParameterSpec)

        val encryptedText:ByteArray = cipher.doFinal(textByteArray)
        return String(Base64.encode(encryptedText, Base64.NO_WRAP), Charset.forName("UTF-8") ).trim()

    }

    override fun decryptAsync(text: String): Single<String> =
        Single.fromCallable{ decrypt(text) }

    override fun decrypt(text: String): String {
        if(text.isEmpty())
            return text

        val strByteArray:ByteArray = Base64.decode(text, Base64.NO_WRAP)

        val cipher:Cipher = Cipher.getInstance(ENCRYPTION)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmParameterSpec)

        val decryptedText:ByteArray = cipher.doFinal(strByteArray)
        return String(decryptedText, Charset.forName("UTF-8") ).trim()

    }
}