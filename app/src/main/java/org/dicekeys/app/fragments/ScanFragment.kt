package org.dicekeys.app.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Bundle
import android.util.Size
import android.view.View
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.AndroidEntryPoint
import org.dicekeys.app.AppFragment
import org.dicekeys.app.R
import org.dicekeys.app.adapters.dicekey
import org.dicekeys.app.databinding.ScanFragmentBinding
import org.dicekeys.app.extensions.setNavigationResult
import org.dicekeys.app.extensions.toast
import org.dicekeys.app.fragments.backup.BackupFragmentArgs
import org.dicekeys.dicekey.FaceRead
import org.dicekeys.read.DiceKeyAnalyzer
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.min

@AndroidEntryPoint
class ScanFragment : AppFragment<ScanFragmentBinding>(R.layout.scan_fragment, 0) {

    companion object {
        const val READ_DICEKEY = "read_dicekey"
    }

    private val args: ScanFragmentArgs by navArgs()

    private lateinit var cameraExecutor: ExecutorService

    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonEnterByHand.setOnClickListener {
            navigate(ScanFragmentDirections.actionScanFragmentToEnterDiceKeyFragment())
        }

        binding.showEnterByHand = args.showEnterByHand

        cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    override fun onResume() {
        super.onResume()

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(requireActivity(), REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    // This is an arbitrary number we are using to keep track of the permission
    // request. Where an app has multiple context for requesting permission,
    // this can help differentiate the different contexts.
    private val REQUEST_CODE_PERMISSIONS = 10

    // This is an array of all the permission specified in the manifest.
    private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)

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

        val previewSize = Size(binding.previewView.width, binding.previewView.height)

        // Build the viewfinder use case
        val preview: Preview = Preview.Builder()
                .setTargetResolution(previewSize)
                .build()
                .also {
                    it.setSurfaceProvider(binding.previewView.surfaceProvider)
                }

        /*
         * Based on the following CameraX documentation, having a hardcoded target resolution is enough
         * for the framework to pick the most appropriate resolution.
         *
         * The target resolution attempts to establish a minimum bound for the image resolution.
         * The actual image resolution will be the closest available resolution in size that is not
         * smaller than the target resolution, as determined by the Camera implementation.
         */

        val analyzerSize = Size(1024, 1024)

        val diceKeyImageAnalyzerUseCase = ImageAnalysis.Builder()
                // In our analysis, we care more about the latest image than
                // analyzing *every* image
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setTargetResolution(analyzerSize)
                .build()

        var analyzeDiceKey = DiceKeyAnalyzer(requireActivity())
        diceKeyImageAnalyzerUseCase.setAnalyzer(
                // Stuart is guessing with this next parameter
                cameraExecutor,
                analyzeDiceKey
        )

        analyzeDiceKey.onActionOverlay = fun(overlayBitmap) {
            val matrix = Matrix()
            // Camera is in landscape mode, fragment is always in portrait
            // it's needed to rotate the bitmap to match the preview
            // hardcoded 90 degrees is fine as we have locked the app orientation.
            matrix.postRotate(90f)
            val rotatedBitmap = Bitmap.createBitmap(overlayBitmap, 0, 0, overlayBitmap.width, overlayBitmap.height, matrix, true);

            binding.overlayView.setImageBitmap(rotatedBitmap)
        }

        analyzeDiceKey.onActionDone = fun(diceKeyAsJson) {
            println("WTF: $diceKeyAsJson")
            cameraProviderFuture.get().unbindAll()

            FaceRead.diceKeyFromJsonFacesRead(diceKeyAsJson)?.let { diceKey ->

                // Rotate 90 degrees
                diceKey.rotate(1).also {
                    setNavigationResult(result = it.toHumanReadableForm(), key = READ_DICEKEY)
                    findNavController().popBackStack()
                }
            }

        }

        try {
            // Unbind use cases before rebinding
            cameraProvider.unbindAll()

            // Bind the camera selector use case, the preview, and the image analyzer use case
            // to this viewLifecycleOwner
            cameraProvider.bindToLifecycle(viewLifecycleOwner, cameraSelector, preview, diceKeyImageAnalyzerUseCase)

        } catch (e: Exception) {
            toast(e.message.toString())
        }
    }
}

