package com.keysqr.readkeysqr

import android.graphics.Bitmap
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import java.nio.ByteBuffer
import java.lang.Exception
import java.util.concurrent.TimeUnit

class KeySqrAnalyzer(val activity: ReadKeySqrActivity) : ImageAnalysis.Analyzer {

    private var lastAnalyzedTimestamp = 0L

    var onActionOverlay = fun(overlay: Bitmap): Unit = null!!
    var onActionDone = fun(keySqrAsJson: String): Unit = null!!

    private val reader = ReadKeySqr()

    override fun analyze(image: ImageProxy) { //     override fun analyze(image: ImageProxy) { //
        try {

            // https://developer.android.com/jetpack/androidx/releases/camera (alpha 7 release notes)
            // "Important: The ImageAnalysis Analyzer method implementation must call image.close() on
            //  received images when finished using them. Otherwise, new images may not be received or
            //  the camera may stall, depending on back pressure setting. Refer to the reference
            //  docs for details."
            
            // Ensure we don't analyze the same frame twice
            val currentTimestamp = System.currentTimeMillis()
            if (currentTimestamp - lastAnalyzedTimestamp < TimeUnit.SECONDS.toMillis(1)) {
                image.close()
                return
            }

            val buffer = image.planes[0].buffer

            val res = reader.ProcessImage(image.width, image.height, image.planes[0].rowStride, buffer)
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

            var bufferOverlay = ByteBuffer.allocateDirect(4 * image.width * image.height)
            reader.RenderAugmentationOverlay(image.width, image.height, bufferOverlay)
            bufferOverlay.rewind()

            val bitmap = Bitmap.createBitmap(image.width, image.height, Bitmap.Config.ARGB_8888)
            bitmap.copyPixelsFromBuffer(bufferOverlay)
            image.close()

            activity.runOnUiThread {
                onActionOverlay(bitmap)
            }
            lastAnalyzedTimestamp = currentTimestamp

        }
        catch (ex: Exception)
        {
            Log.e("KeySqrAnalyzer", ex.message!!)
            image.close()
            throw ex
        }

    }
}