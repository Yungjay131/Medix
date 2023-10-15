package app.slyworks.utils_lib

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import io.reactivex.rxjava3.core.Single
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.File


/**
 * Created by Joshua Sylvanus, 7:55 AM, 25-Sep-2023.
 */

/* use DI to ensure its a singleton */
class ImageProcessor(context: Context) {
    private val FILE_TYPE_JPG:String = "JPG"
    private val FILE_TYPE_PNG:String = "PNG"

    private val MAX_SIZE_MB:Double = 10 * 1_024.0 * 1_024.0
    private val INVALID_FILE_SIZE = -1L

    private var currentFileSize:Long = 0L

    private val contentResolver: ContentResolver = context.contentResolver

    fun getByteArrayFromUriAsync(uri: Uri): Single<Outcome> =
        Single.just(getByteArrayFromUri(uri))

    fun getByteArrayFromUri(uri: Uri): Outcome {
        Timber.e("processImage() running on:${Thread.currentThread().name}")

        val genericErrorMessage:String = "an error occurred processing image.\nPlease try again"

        var o:Outcome
        try {
            val fileType:String = getFileType(uri)
            if(fileType != FILE_TYPE_JPG && fileType != FILE_TYPE_PNG){
                val message:String = "the image being uploaded should be a .jpg, or .png"
                return Outcome.FAILURE(Unit, message)
            }

            val fileSize: Long = getFileSize(uri)
            if (fileSize == INVALID_FILE_SIZE || fileSize >= MAX_SIZE_MB) {
                val message:String = "please select a file with size less than 8mb"
                return Outcome.FAILURE(Unit, message)
            }

            val bitmap: Bitmap?
            if (Build.VERSION.SDK_INT < 28)
                bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
            else
                bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(contentResolver, uri))

            if(bitmap == null)
                return Outcome.FAILURE(Unit, genericErrorMessage)

            val imageByteArray:ByteArray?
            val stream: ByteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            imageByteArray = stream.toByteArray()

            if(imageByteArray == null)
                return Outcome.FAILURE(Unit, genericErrorMessage)

            o = Outcome.SUCCESS(imageByteArray)
        }catch (e: Exception) {
            Timber.e(e)
            o = Outcome.FAILURE(Unit,genericErrorMessage )
        }

        return o
    }

    private fun getFileSize(uri: Uri):Long{
        val cursor: Cursor? =
            contentResolver.query(uri, null, null, null, null)

        cursor ?: return currentFileSize

        val indexSize:Int = cursor.getColumnIndex(OpenableColumns.SIZE)
        cursor.moveToFirst()

        val size:Long = cursor.getLong(indexSize)
        cursor.close()

        return size
    }


    private fun getFileType(uri: Uri): String {
        val extension: String?

        /* Check uri format to avoid null */
        if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
            //If scheme is a content
            val mime: MimeTypeMap = MimeTypeMap.getSingleton()
            extension = mime.getExtensionFromMimeType(contentResolver.getType(uri))
        } else {
            /* If scheme is a File
               This will replace white spaces with %20 and also other special characters.
               This will avoid returning null values on file name with spaces and special characters.
             */
            extension = MimeTypeMap.getFileExtensionFromUrl(
                Uri.fromFile(
                    File(uri.path)
                ).toString()
            )
        }

        /* empty string will still cause validation to fail, so we're good */
        return extension?.toUpperCase() ?: ""
    }
}
