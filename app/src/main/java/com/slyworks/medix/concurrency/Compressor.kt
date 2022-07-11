package com.slyworks.medix.concurrency

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.slyworks.medix.App
import java.io.ByteArrayOutputStream
import com.slyworks.models.models.Result
import timber.log.Timber


/**
 *Created by Joshua Sylvanus, 4:03 PM, 12/12/2021.
 */
    /**
     * utility class for compressing images and videos(using Coroutines)
     * */
object Compressor {
    //region Vars
    private val TAG: String? = Compressor::class.simpleName
        private val MAX_PICTURE_SIZE = 5.0
        private val MAX_VIDEO_SIZE = 50.0
        private val MB = 1_000_000.0
        private var mBitmap: Bitmap? = null
        private var mBytes: ByteArray? = null
    //endregion

        fun compressImage(uri: Uri): Result<ByteArray> {
            var status = Result.failure<ByteArray>()

            val bitmap = getBitmap(uri) ?: return status

            var bytes:ByteArray?
                for(i in 1..10){
                    if(i == 10){
                        status = Result.failure<ByteArray>()
                        break
                    }

                    bytes = getBytesFromBitmap(bitmap, 100/i)

                    if(bytes.size / MB < MAX_PICTURE_SIZE){
                        status = Result.success(bytes)
                        break
                    }
                }

            return status
        }

        private fun getBitmap(uri:Uri): Bitmap?{
            var bitmap:Bitmap? = null
            try {
                if (Build.VERSION.SDK_INT < 28)
                    bitmap = MediaStore.Images.Media.getBitmap(App.getContentResolver(), uri)
                else
                    bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(App.getContentResolver(), uri))

            } catch (e: Exception) {
                Timber.e("getBitmap: ${e.message}" )
            }

            return bitmap
        }

        private fun getBytesFromBitmap(bitmap:Bitmap, quality:Int):ByteArray{
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
            return stream.toByteArray()
        }

        fun compressVideo(uri:Uri){}
}