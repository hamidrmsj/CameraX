package dev.hamidrmsj.camerax.ui.image_capture

import android.Manifest
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import dev.hamidrmsj.camerax.databinding.FragmentImageCaptureBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.listener.PermissionRequest

import com.karumi.dexter.listener.multi.MultiplePermissionsListener





class ImageCaptureFragment : Fragment() {


    private lateinit var binding: FragmentImageCaptureBinding

    private lateinit var imageCapture: ImageCapture

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentImageCaptureBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkCameraAndStoragePermission()
    }

    private fun checkCameraAndStoragePermission() {
        Dexter.withActivity(requireActivity())
            .withPermissions(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    // check if all permissions are granted
                    if (report.areAllPermissionsGranted()) {
                        // do you work now
                        startCamera()
                    }

                    // check for permanent denial of any permission
                    if (report.isAnyPermissionPermanentlyDenied) {
                        // permission is denied permenantly, navigate user to app settings
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: List<PermissionRequest?>?,
                    token: PermissionToken
                ) {
                    token.continuePermissionRequest()
                }
            })
            .onSameThread()
            .check()
    }

    private fun startCamera() {

        binding.btnTakePicture.setOnClickListener {
            takePhoto()
        }

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

            imageCapture = ImageCapture.Builder()
                .build()

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            // There are a few ways this code could fail, like if the app is no longer in focus.
            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture)

            } catch(exc: Exception) {
                Toast.makeText(requireContext(),"Something went wrong!", Toast.LENGTH_LONG).show()
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun takePhoto() {
        // Create time-stamped output file to hold the image
        val photoFile = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.getDefault()
            ).format(System.currentTimeMillis()) + ".jpg")

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        // Set up image capture listener, which is triggered after photo has
        // been taken
        imageCapture.takePicture(
            outputOptions, ContextCompat.getMainExecutor(requireContext()), object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Toast.makeText(requireContext(),"Photo capture failed!", Toast.LENGTH_LONG).show()
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    val msg = "Photo capture succeeded: $savedUri"
                    Toast.makeText(requireContext(),msg, Toast.LENGTH_LONG).show()

                }
            })
    }


}