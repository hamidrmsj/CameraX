package dev.hamidrmsj.camerax.ui.camera

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.PermissionRequest
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.single.PermissionListener
import dev.hamidrmsj.camerax.R
import dev.hamidrmsj.camerax.databinding.FragmentCameraPreviewBinding

class CameraPreviewFragment : Fragment() {

    companion object {
        private const val TAG = "CameraXBasic"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    }


    private lateinit var binding: FragmentCameraPreviewBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCameraPreviewBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkCameraPermission()
    }

    private fun checkCameraPermission() {
        Dexter.withActivity(requireActivity())
            .withPermission(Manifest.permission.CAMERA)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse) {
                    startCamera()
                }

                override fun onPermissionDenied(response: PermissionDeniedResponse) {
                    // check for permanent denial of permission
//                    if (response.isPermanentlyDenied) {
//                        // navigate user to app settings
//                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permission: com.karumi.dexter.listener.PermissionRequest?,
                    token: PermissionToken?
                ) {
                    token?.continuePermissionRequest()
                }

            }).check()
    }

    private fun startCamera() {
        // We need to communicate with device camera hardware
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        // once device camera hardware become ready
        cameraProviderFuture.addListener( {
            // Used to bind the lifecycle of cameras to the lifecycle owner and add use cases to it
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // when frames come from camera, show it in PreviewView
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.previewView.surfaceProvider)
                }

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            // There are a few ways this code could fail, like if the app is no longer in focus.
            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview)

            } catch(exc: Exception) {
                Toast.makeText(requireContext(),"Something went wrong!", Toast.LENGTH_LONG).show()
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }


}