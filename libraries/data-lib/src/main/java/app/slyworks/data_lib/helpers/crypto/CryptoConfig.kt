package app.slyworks.data_lib.helpers.crypto

import java.security.spec.AlgorithmParameterSpec
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

data class CryptoConfig (
     val encryptionAlgorithmShort:String,
     val encryptionAlgorithm:String,
     val encryptionKey: SecretKey,
     val encryptionKeySize:Int,
     val encryptionIV:String,
     val encryptionSpec:AlgorithmParameterSpec,
     val hashSalt: ByteArray,
     val hashAlgorithm:String,
     val hashIterationCount:Int,
     val hashLength:Int,
     ){

     companion object{
          @JvmStatic
          val DEFAULT: CryptoConfig =
               CryptoConfig(
                     encryptionAlgorithmShort = "",
                     encryptionAlgorithm = "",
                     encryptionKey = SecretKeySpec(ByteArray(1),""),
                     encryptionKeySize = -1,
                     encryptionIV = "",
                     encryptionSpec = GCMParameterSpec(-1, ByteArray(1)),
                     hashSalt = ByteArray(1),
                     hashAlgorithm = "",
                     hashIterationCount = -1,
                     hashLength = -1
          )
     }

     override fun equals(other: Any?): Boolean {
          if (this === other) return true
          if (javaClass != other?.javaClass) return false

          other as CryptoConfig

          if (encryptionAlgorithmShort != other.encryptionAlgorithmShort) return false
          if (encryptionAlgorithm != other.encryptionAlgorithm) return false
          if (encryptionKey != other.encryptionKey) return false
          if (encryptionKeySize != other.encryptionKeySize) return false
          if (encryptionIV != other.encryptionIV) return false
          if (encryptionSpec != other.encryptionSpec) return false
          if (!hashSalt.contentEquals(other.hashSalt)) return false
          if (hashAlgorithm != other.hashAlgorithm) return false
          if (hashIterationCount != other.hashIterationCount) return false
          if (hashLength != other.hashLength) return false

          return true
     }

     override fun hashCode(): Int {
          var result = encryptionAlgorithmShort.hashCode()
          result = 31 * result + encryptionAlgorithm.hashCode()
          result = 31 * result + encryptionKey.hashCode()
          result = 31 * result + encryptionKeySize
          result = 31 * result + encryptionIV.hashCode()
          result = 31 * result + encryptionSpec.hashCode()
          result = 31 * result + hashSalt.contentHashCode()
          result = 31 * result + hashAlgorithm.hashCode()
          result = 31 * result + hashIterationCount
          result = 31 * result + hashLength
          return result
     }
}
