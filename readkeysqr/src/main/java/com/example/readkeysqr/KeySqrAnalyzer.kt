package com.example.readkeysqr

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import java.util.concurrent.TimeUnit

public class KeySqrAnalyzer(val activity: ReadKeySqrActivity) : ImageAnalysis.Analyzer {
    private var lastAnalyzedTimestamp = 0L

    public var onActionJson = fun(json: String): Int = null!!

    override fun analyze(image: ImageProxy, rotationDegrees: Int) {
        val currentTimestamp = System.currentTimeMillis()
        if (currentTimestamp - lastAnalyzedTimestamp >= TimeUnit.SECONDS.toMillis(1)) {
            val buffer = image.planes[0].buffer
            val json = ReadKeySqr.ReadKeySqrJson(image.width, image.height, image.planes[0].rowStride, buffer);
            if(json != "null")
            {
                activity.runOnUiThread({
                    onActionJson(json)
                })
            }
            lastAnalyzedTimestamp = currentTimestamp
        }
    }
}