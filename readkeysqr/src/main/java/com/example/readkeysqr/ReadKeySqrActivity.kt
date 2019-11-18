package com.example.readkeysqr

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Size
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.camera.core.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.concurrent.Executors
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import java.nio.ByteBuffer


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
    private lateinit var txtJson: TextView

    private lateinit var panelButtons: LinearLayout
    private lateinit var imageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_read_key_sqr)

        txtJson = findViewById(R.id.txt_json)
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

        // Every time the provided texture view changes, recompute layout
        viewFinder.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            updateTransform()
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
        // Create configuration object for the viewfinder use case
        val previewConfig = PreviewConfig.Builder().apply {
            setTargetResolution(Size(640, 480))
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
            panelButtons.visibility = View.VISIBLE
            //preview.removePreviewOutputListener()
            //analyzerUseCase.removeAnalyzer()

            //val bitmap = BitmapFactory.decodeResource(resources, R.drawable.test1)
            /*
            val width = 100
            val height = 100

            val b1 = ByteBuffer.allocateDirect(4*width*height)
            for(i in 1 until 4*width*height step 4)
            {
                b1.put(i, 0xFF.toByte())
            }

            val b2 = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            b1.rewind()
            b2.copyPixelsFromBuffer(b1)

            */
            imageView.setImageBitmap(overlayBitmap)

            //txtJson.text = json
            return 0;
        }

        findViewById<Button>(R.id.btn_take).setOnClickListener({
            CameraX.unbindAll()
            var intent = Intent();
            intent.putExtra("json", txtJson.text);
            setResult(RESULT_OK, intent);
            finish();
        })

        findViewById<Button>(R.id.btn_cancel).setOnClickListener({
            panelButtons.visibility = View.INVISIBLE
            txtJson.text = ""
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
        updateTransform()
    }

    private fun updateTransform() {
        val matrix = Matrix()

        // Compute the center of the view finder
        val centerX = viewFinder.width / 2f
        val centerY = viewFinder.height / 2f

        // Correct preview output to account for display rotation
        val rotationDegrees = when(viewFinder.display.rotation) {
            Surface.ROTATION_0 -> 0
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else -> return
        }
        matrix.postRotate(-rotationDegrees.toFloat(), centerX, centerY)

        // Finally, apply transformations to our TextureView
        viewFinder.setTransform(matrix)
    }


}
