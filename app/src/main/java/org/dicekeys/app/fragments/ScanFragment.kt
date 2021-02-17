package org.dicekeys.app.fragments

import android.Manifest
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Bundle
import android.util.Size
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.navigation.fragment.findNavController
import androidx.viewpager.widget.ViewPager
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.AndroidEntryPoint
import org.dicekeys.app.AppFragment
import org.dicekeys.app.R
import org.dicekeys.app.databinding.AssembleFragmentBinding
import org.dicekeys.app.databinding.ScanFragmentBinding
import org.dicekeys.app.extensions.setNavigationResult
import org.dicekeys.app.extensions.toast
import org.dicekeys.dicekey.DiceKey
import org.dicekeys.dicekey.Face
import org.dicekeys.dicekey.FaceRead
import org.dicekeys.read.DiceKeyAnalyzer
import org.dicekeys.read.ReadDiceKeyActivity
import java.util.concurrent.Executors

@AndroidEntryPoint
class ScanFragment : AppFragment<ScanFragmentBinding>(R.layout.scan_fragment) {

    companion object {
        const val READ_DICEKEY = "read_dicekey"
    }

    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
    }

    override fun onResume() {
        super.onResume()

        if (allPermissionsGranted()) {
            startCamera()
//            imageView.post { startCamera() }
        } else {
            ActivityCompat.requestPermissions(requireActivity(), REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }
    }

    // This is an arbitrary number we are using to keep track of the permission
    // request. Where an app has multiple context for requesting permission,
    // this can help differentiate the different contexts.
    private val REQUEST_CODE_PERMISSIONS = 10

    // This is an array of all the permission specified in the manifest.
    private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)

    // Add this after onCreate
    private val executor = Executors.newSingleThreadExecutor()


//    private lateinit var previewView: PreviewView
//    private lateinit var imageView: ImageView


    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
               binding.overlayView.post { startCamera() }
            } else {
                toast("Permissions not granted by the user.")
                findNavController().popBackStack()
            }
        }
    }

    /**
     * Check if all permission specified in the manifest have been granted
     */
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    private fun startCamera() {
        cameraProviderFuture.addListener(Runnable {
            val cameraProvider = cameraProviderFuture.get()
            bindPreview(cameraProvider)
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun bindPreview(cameraProvider: ProcessCameraProvider) {
        var cameraSelector: CameraSelector = CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build()
        //val previewSize = Size(viewFinder.width, viewFinder.height)
        val previewSize = Size(binding.previewView.width, binding.previewView.height)


        // Build the viewfinder use case
        val preview: Preview = Preview.Builder()
                .setTargetResolution(previewSize)
                .build()

        val pWidth = binding.previewView.width
        val pHeight = binding.previewView.height
        val analyzerSize: Size =
                if (pWidth * 9 > pHeight * 16)
                // Wider than 16x9, so fix width=1920 and calculate a height which will be <= 1080
                    Size(1920, (1920 * pHeight) / pWidth)
                else if (pHeight * 16 > pWidth * 9)
                // Taller than 16x9, so fix height=1920 and calculate a width which will be <= 1080
                    Size((1920 * pWidth) / pHeight, 1920)
                else if (pWidth > pHeight)
                // Wider than 1x1 but less than 16x9, so fix height=1080 and calculate width <=1920
                    Size((1080 * pWidth / pHeight), 1080)
                else
                // Taller than 1x1, or 1x1, so fix width at 1080 and calculate height 1080<=x<=1920
                    Size(1080, (1080 * pHeight / pWidth))
        val diceKeyImageAnalyzerUseCase = ImageAnalysis.Builder()
                // In our analysis, we care more about the latest image than
                // analyzing *every* image
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setTargetResolution(analyzerSize)
                .build()

        var analyzeDiceKey = DiceKeyAnalyzer(requireActivity())
        diceKeyImageAnalyzerUseCase.setAnalyzer(
                // Stuart is guessing with this next parameter
                executor, // ContextCompat.getMainExecutor(this),
                analyzeDiceKey
        )
        // analyzerConfig.setTargetRotation()

        analyzeDiceKey.onActionOverlay = fun(overlayBitmap) {
            val matrix = Matrix()
            // FIXME - not sure this will be correct if scanning in landscape.
            // I'd assume this angle should be derived/read from somewhere.
            // https://github.com/dicekeys/read-dicekey-android/issues/18
            matrix.postRotate(90f)
            val rotatedBitmap = Bitmap.createBitmap(overlayBitmap, 0, 0, overlayBitmap.getWidth(), overlayBitmap.getHeight(), matrix, true);

            binding.overlayView.setImageBitmap(rotatedBitmap)
        }

        analyzeDiceKey.onActionDone = fun(diceKeyAsJson) {
            cameraProviderFuture.get().unbindAll()
            setNavigationResult(result = diceKeyAsJson, key = READ_DICEKEY)
            findNavController().popBackStack()
        }

        // Bind the camera selector use case, the preview, and the image analyzer use case
        // to this activity class
        var camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, diceKeyImageAnalyzerUseCase)
//        var camera = cameraProvider.bindToLifecycle(this as LifecycleOwner, cameraSelector, preview)

//        preview.setSurfaceProvider(previewView.createSurfaceProvider(camera.cameraInfo))
        preview.setSurfaceProvider(binding.previewView.surfaceProvider)
    }
}

