package com.keysqr.readkeysqr

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Size
import android.widget.*
import androidx.camera.core.*
import androidx.camera.core.ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
import androidx.camera.lifecycle.*
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.common.util.concurrent.ListenableFuture
import java.util.concurrent.Executors


// FIXME - resolve API update: Moved rotationDegrees from class Analyzer to ImageInfo.


class ReadKeySqrActivity : AppCompatActivity() {
//        , CameraXConfig.Provider {
//    override fun getCameraXConfig(): CameraXConfig {
//        return Camera2Config.defaultConfig()
//    }
    // This is an arbitrary number we are using to keep track of the permission
    // request. Where an app has multiple context for requesting permission,
    // this can help differentiate the different contexts.

    private val REQUEST_CODE_PERMISSIONS = 10

    // This is an array of all the permission specified in the manifest.
    private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)

    // Add this after onCreate

    private val executor = Executors.newSingleThreadExecutor()


    private lateinit var cameraProviderFuture : ListenableFuture<ProcessCameraProvider>
//    private lateinit var viewFinder: TextureView
    private lateinit var previewView: PreviewView
    private lateinit var panelButtons: LinearLayout
    private lateinit var imageView: ImageView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        setContentView(R.layout.activity_read_key_sqr)

//        viewFinder = findViewById(R.id.texture_view)
        imageView = findViewById(R.id.overlay_view)
        previewView = findViewById(R.id.preview_view)

        if(allPermissionsGranted()) {
            imageView.post{startCamera()}
            // viewFinder.post{startCamera()}
        }
        else
        {
            ActivityCompat.requestPermissions(
                    this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                // viewFinder.post { startCamera() }
                imageView.post { startCamera() }
            } else {
                Toast.makeText(this,
                        "Permissions not granted by the user.",
                        Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    /**
     * Check if all permission specified in the manifest have been granted
     */
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
                baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun startCamera() {
        cameraProviderFuture?.addListener(Runnable {
            val cameraProvider = cameraProviderFuture.get()
            bindPreview(cameraProvider)
        }, ContextCompat.getMainExecutor(this))
    }

    private fun bindPreview(cameraProvider : ProcessCameraProvider) {
        var cameraSelector : CameraSelector = CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build()
        //val previewSize = Size(viewFinder.width, viewFinder.height)
        val previewSize = Size(previewView.width, previewView.height)

        // Build the viewfinder use case
        val preview: Preview = Preview.Builder()
                .setTargetResolution(previewSize)
                .build()

        preview.setPreviewSurfaceProvider(previewView.getPreviewSurfaceProvider())

        val keySqrImageAnalyzerUseCase = ImageAnalysis.Builder()
                // In our analysis, we care more about the latest image than
                // analyzing *every* image
                .setBackpressureStrategy(STRATEGY_KEEP_ONLY_LATEST)
                // FIXME -- we want higher resolution if the screen is low-res.
                .setTargetResolution(Size(1920, 1080))
                .build()

        var analyzeKeySqr = KeySqrAnalyzer(this)
        keySqrImageAnalyzerUseCase.setAnalyzer(
                // Stuart is guessing with this next parameter
                executor, // ContextCompat.getMainExecutor(this),
                analyzeKeySqr
        )
        // analyzerConfig.setTargetRotation()

        analyzeKeySqr.onActionOverlay = fun(overlayBitmap){
            val matrix = Matrix()
            // FIXME - not sure this will be correct if scanning in landscape.
            // I'd assume this angle should be derived/read from somewhere.
            // https://github.com/dicekeys/read-keysqr-android/issues/18
            matrix.postRotate(90f)
            val rotatedBitmap = Bitmap.createBitmap(overlayBitmap, 0, 0, overlayBitmap.getWidth(), overlayBitmap.getHeight(), matrix, true);

            imageView.setImageBitmap(rotatedBitmap)
        }

        analyzeKeySqr.onActionDone = fun(keySqrAsJson){
            var intent = Intent()
            // FIXME - remove
            intent.putExtra("result", "meh")
            intent.putExtra("keySqrAsJson", keySqrAsJson)
            setResult(RESULT_OK, intent)
            cameraProviderFuture.get().unbindAll()
            finish()
        }

        // Bind the camera selector use case, the preview, and the image analyzer use case
        // to this activity class
        cameraProvider.bindToLifecycle(this, cameraSelector, preview, keySqrImageAnalyzerUseCase)
    }



}
