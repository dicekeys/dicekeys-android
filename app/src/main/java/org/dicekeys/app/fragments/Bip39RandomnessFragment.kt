package org.dicekeys.app.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Bundle
import android.util.Size
import android.view.View
import android.widget.ArrayAdapter
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.AndroidEntryPoint
import org.dicekeys.api.bip39RandomnessRecipeTemplate
import org.dicekeys.app.AppFragment
import org.dicekeys.app.R
import org.dicekeys.app.adapters.GenericAdapter
import org.dicekeys.app.data.DerivedValue
import org.dicekeys.app.data.DerivedValueView
import org.dicekeys.app.databinding.Bip39RandomnessFragmentBinding
import org.dicekeys.app.extensions.*
import org.dicekeys.app.items.Bip39WordItem
import org.dicekeys.app.items.GenericListItem
import org.dicekeys.app.viewmodels.Bip39RandomnessViewModel
import org.dicekeys.crypto.seeded.*
import org.dicekeys.dicekey.DiceKey
import org.dicekeys.dicekey.Face
import org.dicekeys.dicekey.FaceRead
import org.dicekeys.read.DiceKeyAnalyzer
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@AndroidEntryPoint
class Bip39RandomnessFragment : AppFragment<Bip39RandomnessFragmentBinding>(R.layout.bip39_randomness_fragment, 0), GenericAdapter.OnItemClickListener {
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var bip39Adapter: GenericAdapter

    private val viewModel: Bip39RandomnessViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.vm = viewModel

        cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraExecutor = Executors.newSingleThreadExecutor()

        bip39Adapter = GenericAdapter(this)

        binding.recycler.also {
            it.layoutManager = StaggeredGridLayoutManager(3 , StaggeredGridLayoutManager.VERTICAL)
            it.adapter = bip39Adapter
        }

        viewModel.derivedValueAsString.observe(viewLifecycleOwner) {
            it?.let {
                updateBip39Words(it)
            }
        }

        binding.buttonSequenceUp.setOnClickListener {
            viewModel.recipeBuilder.sequenceUp()
        }

        binding.buttonSequenceDown.setOnClickListener {
            viewModel.recipeBuilder.sequenceDown()
        }

        binding.toggleButton.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if(isChecked){
                viewModel.setView(if(checkedId == R.id.button12Words) DerivedValueView.BIP39_12() else DerivedValueView.BIP39_24())
            }
        }

        binding.buttonQrCode.setOnClickListener {
            viewModel.derivedValueAsString.value?.let {
                dialogQR(title = "BIP39", content = it)
            }
        }

        dialog(
            "BIP39 Randomness",
            "This is a tool to create truly random BIP39 phrases by rolling your DiceKey's dice.\n\nEven scanning your dice from different angle gives you a new phrase.\n\nDon't you use it on your locked DiceKeys."
        ){
            binding.randomnessWarning.flash(4000)
        }
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

        var analyzeDiceKey = DiceKeyAnalyzer(requireActivity(), false)
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
            FaceRead.diceKeyFromJsonFacesRead(diceKeyAsJson)?.let { diceKey ->
                if(viewModel.setScannedDiceKey(diceKey.rotate(1))){
                    binding.dicekey.pulse(scale = 1.2f, duration = 600)
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

    private fun updateBip39Words(value: String) {
        val list = mutableListOf<GenericListItem<*>>()

        for((index, word) in value.split(" ").withIndex()){
            list += Bip39WordItem((index + 1).toString(),word)
        }

        bip39Adapter.set(list)
    }

    override fun onItemClicked(view: View, position: Int, item: GenericListItem<*>) {
        viewModel.derivedValueAsString.value?.let {
            askToCopyToClipboard("Do you want to copy the BIP39 phrase to the clipboard?", it, binding.cardDerivedValue)
        }
    }
}

