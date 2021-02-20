package org.dicekeys.read

import android.app.Activity
import android.graphics.Bitmap
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import java.nio.ByteBuffer
import java.lang.Exception

class DiceKeyAnalyzer(val activity: Activity) : ImageAnalysis.Analyzer {

    var onActionOverlay = fun(overlay: Bitmap): Unit = null!!
    var onActionDone = fun(diceKeyAsJson: String): Unit = null!!
    var done: Boolean = false

    private val reader = ReadDiceKey()

    override fun analyze(image: ImageProxy) {
        if (done) {
            // Once we've successfully analyzed the image, don't do any more analysis
            // (if we do, we may be in the middle of an analysis when this object is freed,
            //  causing a seg fault.  we don't like those!)
            return
        }
        var imageClosed = false
        try {

            val buffer = image.planes[0].buffer
            val width = image.width
            val height = image.height
            val res = reader.processImage(width, height, image.planes[0].rowStride, buffer)

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
            reader.renderAugmentationOverlay(width, height, bufferOverlay)
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
                val diceKeyAsJson = reader.jsonDiceKeyRead()
                if (diceKeyAsJson != "null")
                {
                    done = true
                    activity.runOnUiThread{
                        onActionDone(diceKeyAsJson)
                    }
                }
            }

        }
        catch (ex: Exception)
        {
            Log.e("DiceKeyAnalyzer", ex.message!!)
            if (!imageClosed) {
                image.close()
            }
            throw ex
        }

    }
}