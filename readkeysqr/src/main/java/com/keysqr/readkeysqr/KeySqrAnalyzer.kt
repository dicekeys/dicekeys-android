package com.keysqr.readkeysqr

import android.graphics.Bitmap
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import java.nio.ByteBuffer
import com.keysqr.readkeysqr.keySqrFromJsonFacesRead
import java.lang.Exception
import java.util.concurrent.TimeUnit

class KeySqrAnalyzer(val activity: ReadKeySqrActivity) : ImageAnalysis.Analyzer {

    private var lastAnalyzedTimestamp = 0L

    var onActionOverlay = fun(overlay: Bitmap): Unit = null!!
    var onActionDone = fun(humanReadableForm: String): Unit = null!!

    private val reader = ReadKeySqr()

    override fun analyze(image: ImageProxy, rotationDegrees: Int) {
        val currentTimestamp = System.currentTimeMillis()
        if (currentTimestamp - lastAnalyzedTimestamp >= TimeUnit.SECONDS.toMillis(1)) {
            try {
                val buffer = image.planes[0].buffer

                val res = reader.ProcessImage(image.width, image.height, image.planes[0].rowStride, buffer);
                if(res)
                {
                    val vKeySqr = keySqrFromJsonFacesRead(reader.JsonKeySqrRead())
                    val humanReadableForm: String? = vKeySqr?.toHumanReadableForm(true)
                    if(humanReadableForm != null)
                    {
                        activity.runOnUiThread({
                            onActionDone(humanReadableForm)
                        })
                    }
                }

                var bufferOverlay = ByteBuffer.allocateDirect(4 * image.width * image.height)
                reader.RenderAugmentationOverlay(image.width, image.height, bufferOverlay)

                bufferOverlay.rewind()
                val bitmap = Bitmap.createBitmap(image.width, image.height, Bitmap.Config.ARGB_8888)
                bitmap.copyPixelsFromBuffer(bufferOverlay)

                activity.runOnUiThread({
                    onActionOverlay(bitmap)
                })
            }
            catch (ex: Exception)
            {
                Log.e("KeySqrAnalyzer", ex.message!!)
            }

            lastAnalyzedTimestamp = currentTimestamp
        }
    }
}