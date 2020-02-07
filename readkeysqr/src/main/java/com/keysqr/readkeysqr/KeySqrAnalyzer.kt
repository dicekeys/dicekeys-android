package com.keysqr.readkeysqr

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import java.nio.ByteBuffer
import java.lang.Exception
import java.util.concurrent.TimeUnit

class KeySqrAnalyzer(val activity: ReadKeySqrActivity) : ImageAnalysis.Analyzer {

    var onActionOverlay = fun(overlay: Bitmap): Unit = null!!
    var onActionDone = fun(keySqrAsJson: String): Unit = null!!

    private val reader = ReadKeySqr()

    override fun analyze(image: ImageProxy) { //     override fun analyze(image: ImageProxy) { //
        var imageClosed = false
        try {

            val buffer = image.planes[0].buffer
            val width = image.width
            val height = image.height
            val res = reader.ProcessImage(width, height, image.planes[0].rowStride, buffer)

            // https://developer.android.com/jetpack/androidx/releases/camera (alpha 7 release notes)
            // "Important: The ImageAnalysis Analyzer method implementation must call image.close() on
            //  received images when finished using them. Otherwise, new images may not be received or
            //  the camera may stall, depending on back pressure setting. Refer to the reference
            //  docs for details."

            // We've processed the image, so we can close it before we do anything else
            // and allow more images to be processed
            image.close()
            imageClosed = true


            var bufferOverlay = ByteBuffer.allocateDirect(4 * width * height)
            reader.RenderAugmentationOverlay(width, height, bufferOverlay)
            bufferOverlay.rewind()

//            if (bufferOverlay.hasArray())
//                BitmapFactory.decodeByteArray(bufferOverlay.array(), 0, bufferOverlay.remaining(), BitmapFactory.Options(). )
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            bitmap.copyPixelsFromBuffer(bufferOverlay)

            activity.runOnUiThread {
                onActionOverlay(bitmap)
            }

            if(res)
            {
                val keySqrAsJson = reader.JsonKeySqrRead()
                if (keySqrAsJson != "null")
                {
                    activity.runOnUiThread{
                        onActionDone(keySqrAsJson)
                    }
                }
            }

        }
        catch (ex: Exception)
        {
            Log.e("KeySqrAnalyzer", ex.message!!)
            if (!imageClosed) {
                image.close()
            }
            throw ex
        }

    }
}