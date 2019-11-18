package com.keysqr.readkeysqr

import android.graphics.Bitmap
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import java.nio.ByteBuffer
import java.util.concurrent.TimeUnit

public class KeySqrAnalyzer(val activity: ReadKeySqrActivity) : ImageAnalysis.Analyzer {

    private var lastAnalyzedTimestamp = 0L

    public var onActionJson = fun(overlay: Bitmap): Int = null!!

    private val reader = ReadKeySqr()

    override fun analyze(image: ImageProxy, rotationDegrees: Int) {
        val currentTimestamp = System.currentTimeMillis()
        if (currentTimestamp - lastAnalyzedTimestamp >= TimeUnit.SECONDS.toMillis(1)) {
            val buffer = image.planes[0].buffer

            reader.ProcessImage(image.width, image.height, image.planes[0].rowStride, buffer);

            var bufferOverlay = ByteBuffer.allocateDirect(4 * image.width * image.height)
            reader.RenderAugmentationOverlay(image.width, image.height, bufferOverlay)

            bufferOverlay.rewind()
            val bitmap = Bitmap.createBitmap(image.width, image.height, Bitmap.Config.ARGB_8888)
            bitmap.copyPixelsFromBuffer(bufferOverlay)

            activity.runOnUiThread({
                onActionJson(bitmap);
            })

            lastAnalyzedTimestamp = currentTimestamp
        }
    }
}