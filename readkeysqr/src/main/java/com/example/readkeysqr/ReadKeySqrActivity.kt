package com.example.readkeysqr

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Size
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.camera.core.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.concurrent.Executors

class ReadKeySqrActivity : AppCompatActivity() {

    // This is an arbitrary number we are using to keep track of the permission
    // request. Where an app has multiple context for requesting permission,
    // this can help differentiate the different contexts.

    private val REQUEST_CODE_PERMISSIONS = 10

    // This is an array of all the permission specified in the manifest.
    private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)

    // Add this after onCreate

    private val executor = Executors.newSingleThreadExecutor()

    private lateinit var viewFinder: TextureView

    private lateinit var panelButtons: LinearLayout
    private lateinit var imageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_read_key_sqr)

        viewFinder = findViewById(R.id.texture_view)
        panelButtons = findViewById(R.id.panel_buttons)

        imageView = findViewById(R.id.overlay_view)

        if(allPermissionsGranted()) {
            viewFinder.post{startCamera()}

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
                viewFinder.post { startCamera() }
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
        val screenSize = Size(viewFinder.width, viewFinder.height)

        // Create configuration object for the viewfinder use case
        val previewConfig = PreviewConfig.Builder().apply {
            setTargetResolution(screenSize)
        }.build()

        // Build the viewfinder use case
        val preview = Preview(previewConfig)

        // Every time the viewfinder is updated, recompute layout
        preview.setOnPreviewOutputUpdateListener {
            updateCameraOutput(it)
        }

        // Setup image analysis pipeline that computes average pixel luminance
        val analyzerConfig = ImageAnalysisConfig.Builder().apply {
            // In our analysis, we care more about the latest image than
            // analyzing *every* image
            setTargetResolution(screenSize)
            setImageReaderMode(
                    ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
        }.build()

        var analyzerKeySqr = KeySqrAnalyzer(this)

        // Build the image analysis use case and instantiate our analyzer
        val analyzerUseCase = ImageAnalysis(analyzerConfig).apply {
            setAnalyzer(executor, analyzerKeySqr)
        }

        // Bind use cases to lifecycle
        // If Android Studio complains about "this" being not a LifecycleOwner
        // try rebuilding the project or updating the appcompat dependency to
        // version 1.1.0 or higher.
        CameraX.bindToLifecycle(this, preview, analyzerUseCase)

        analyzerKeySqr.onActionJson = fun(overlayBitmap) : Int {
            val matrix = Matrix()
            matrix.postRotate(90f)
            val rotatedBitmap = Bitmap.createBitmap(overlayBitmap, 0, 0, overlayBitmap.getWidth(), overlayBitmap.getHeight(), matrix, true);

            imageView.setImageBitmap(rotatedBitmap)
            return 0;
        }

        findViewById<Button>(R.id.btn_take).setOnClickListener({
            CameraX.unbindAll()
            var intent = Intent();
            setResult(RESULT_OK, intent);
            finish();
        })

        findViewById<Button>(R.id.btn_cancel).setOnClickListener({
            panelButtons.visibility = View.INVISIBLE
            preview.setOnPreviewOutputUpdateListener({
                updateCameraOutput(it)
            })
            analyzerUseCase.setAnalyzer(executor, analyzerKeySqr)
        })
    }

    private fun updateCameraOutput(it: Preview.PreviewOutput)
    {
        // To update the SurfaceTexture, we have to remove it and re-add it
        val parent = viewFinder.parent as ViewGroup
        parent.removeView(viewFinder)
        parent.addView(viewFinder, 0)

        viewFinder.surfaceTexture = it.surfaceTexture
    }
}
