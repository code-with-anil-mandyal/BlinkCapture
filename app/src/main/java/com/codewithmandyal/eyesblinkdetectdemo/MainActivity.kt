package com.codewithmandyal.eyesblinkdetectdemo

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.codewithmandyal.eyesblinkdetectdemo.databinding.ActivityMainBinding
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.io.File
import java.util.concurrent.Executors

class MainActivity : ComponentActivity() {

    lateinit var binding: ActivityMainBinding

    private lateinit var imageCapture: ImageCapture

    private var eyesClosed = false
    private var isCaptured = false

//    private val detector by lazy {
//        val option = FaceDetectorOptions.Builder()
//            .setPerformanceMode(
//                FaceDetectorOptions.PERFORMANCE_MODE_FAST
//            )
//            .setClassificationMode(
//                FaceDetectorOptions.CLASSIFICATION_MODE_NONE
//            )
//            .enableTracking()
//            .build()
//
//        FaceDetection.getClient(option)
//    }

    private val detector by lazy {

        val options =
            FaceDetectorOptions.Builder()
                .setPerformanceMode(
                    FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE
                )
                .setClassificationMode(
                    FaceDetectorOptions.CLASSIFICATION_MODE_ALL
                )
                .setMinFaceSize(0.15f)
                .enableTracking()
                .build()

        FaceDetection.getClient(options)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        //setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        checkPermission()
    }

    private fun checkPermission(){
        if(ContextCompat.checkSelfPermission(
            this,
                Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
            ){
            startCamera()
        }else{
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                1
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String?>,
        grantResults: IntArray,
        deviceId: Int
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults, deviceId)
        if(
            requestCode == 101 &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ){
            startCamera()
        }
    }

    inner class FaceAnalyzer : ImageAnalysis.Analyzer{
        @OptIn(ExperimentalGetImage::class)
        override fun analyze(imageProxy: ImageProxy) {
            val mediaImage = imageProxy.image
            if(mediaImage != null){
                val image = InputImage.fromMediaImage(
                    mediaImage,
                    imageProxy.imageInfo.rotationDegrees
                )

                detector.process(image)
                    .addOnSuccessListener { face ->

                        if(face.isNotEmpty()){
                            val face = face[0]

                            processFace(face)
                        }

                    }
                    .addOnCompleteListener {
                        imageProxy.close()
                    }
            }
        }
    }

    fun processFace(face: Face){

            val leftEye = face.leftEyeOpenProbability?:1f
            val rightEye = face.rightEyeOpenProbability?: 1f

            detectBlink(leftEye, rightEye)


    }

//    private fun detectBlink(
//        leftEye: Float,
//        rightEye: Float
//    ) {
//
//        runOnUiThread {
//
//            binding.tvStatus.text =
//                "Left: %.2f Right: %.2f"
//                    .format(leftEye, rightEye)
//        }
//
//        val closed =
//            leftEye < 0.8f &&
//                    rightEye < 0.8f
//
//        val open =
//            leftEye > 0.9f &&
//                    rightEye > 0.9f
//
//        if (closed) {
//
//            eyesClosed = true
//
//            Log.e("check_blink", "Eyes Closed")
//        }
//
//        if (open && eyesClosed) {
//
//            eyesClosed = false
//
//            Log.e("check_blink", "Blink Detected")
//
//            runOnUiThread {
//
//                binding.tvStatus.text =
//                    "Blink Detected!"
//            }
//
//            if (!isCaptured) {
//
//                isCaptured = true
//
//                capturePhoto()
//            }
//        }
//    }


    private var blinkStarted = false

    private var blinkStartTime = 0L

    private var lastCaptureTime = 0L

    private fun detectBlink(
        leftEye: Float,
        rightEye: Float
    ) {

        val currentTime = System.currentTimeMillis()

        runOnUiThread {

            binding.tvStatus.text =
                "L: %.2f  R: %.2f"
                    .format(leftEye, rightEye)
        }

        val eyesClosed =
            leftEye < 0.5f &&
                    rightEye < 0.5f

        val eyesOpen =
            leftEye > 0.85f &&
                    rightEye > 0.85f

        // Blink started
        if (eyesClosed && !blinkStarted) {

            blinkStarted = true
            blinkStartTime = currentTime

            Log.e("BLINK", "Blink Started")
        }

        // Blink completed
        if (blinkStarted && eyesOpen) {

            val blinkDuration =
                currentTime - blinkStartTime

            blinkStarted = false

            Log.e(
                "BLINK",
                "Blink Duration: $blinkDuration"
            )

            // Ignore fake micro changes
            if (blinkDuration in 80..400) {

                // Prevent multiple captures
                if (currentTime - lastCaptureTime > 2000) {

                    lastCaptureTime = currentTime

                    Log.e("BLINK", "VALID BLINK")

                    runOnUiThread {

                        binding.tvStatus.text =
                            "Valid Blink Detected"
                    }

                    capturePhoto()
                }
            }
        }
    }

    fun capturePhoto(){
        val photoFile = File(
            externalMediaDirs.first(),
            "${System.currentTimeMillis()}.jpg"
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback{
                override fun onImageSaved(p0: ImageCapture.OutputFileResults) {
                    Log.e("check_blink", "Image captured")

                    Glide.with(this@MainActivity)
                        .load(photoFile)
                        .into(binding.imageView)

                    binding.tvStatus.text = "Image Captured"
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e("check_blink", "Capture Error ${exception.message}")
                }

            }
        )


    }

    fun startCamera(){
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
           val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()

            preview.setSurfaceProvider(
                binding.previewView.surfaceProvider
            )

            imageCapture = ImageCapture.Builder().build()

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(
                    ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
                )
                .build()

            imageAnalysis.setAnalyzer(
                Executors.newSingleThreadExecutor(),
                FaceAnalyzer()
            )

            val cameraSelector
            = CameraSelector.DEFAULT_FRONT_CAMERA

            try {

                cameraProvider.unbindAll()

                cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageCapture,
                    imageAnalysis
                )

                Log.e("check_blink", "Camera Started")

            }catch (e:Exception){
                Log.e("check_blink", "Binding Failed ${e.message}")
            }




        }, ContextCompat.getMainExecutor(this))
    }


}

