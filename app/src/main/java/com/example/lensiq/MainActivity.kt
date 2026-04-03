package com.example.lensiq

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.lensiq.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var cameraProvider: ProcessCameraProvider
    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private val classifier = ImageClassifier()
    private var isScanning = false

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            startCamera()
        } else {
            Snackbar.make(binding.root, "Camera permission is required", Snackbar.LENGTH_INDEFINITE)
                .setAction("Retry") { checkCameraPermission() }
                .show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewfinder()
        checkCameraPermission()

        binding.fabScan.setOnClickListener {
            onScanClicked()
        }
    }

    private fun setupViewfinder() {
        val viewfinderOverlay = ViewfinderOverlay(this)
        viewfinderOverlay.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        binding.viewfinderContainer.addView(viewfinderOverlay)
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.previewView.surfaceProvider)
                }

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor) { imageProxy ->
                        if (isScanning) {
                            classifier.classify(
                                imageProxy,
                                onResult = { result ->
                                    isScanning = false
                                    lifecycleScope.launch(Dispatchers.Main) {
                                        navigateToResults(result)
                                    }
                                },
                                onError = { e ->
                                    isScanning = false
                                    lifecycleScope.launch(Dispatchers.Main) {
                                        Snackbar.make(binding.root, "Analysis failed", Snackbar.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        } else {
                            imageProxy.close()
                        }
                    }
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalysis
                )
                animateFab()
            } catch (exc: Exception) {
                // Failed to bind camera
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun animateFab() {
        val pulseAnimation = ScaleAnimation(
            1.0f, 1.06f, 1.0f, 1.06f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        ).apply {
            duration = 1800
            repeatCount = Animation.INFINITE
            repeatMode = Animation.REVERSE
        }
        binding.fabScan.startAnimation(pulseAnimation)
    }

    private fun navigateToResults(result: ScanResult) {
        cameraProvider.unbindAll()
        val intent = Intent(this, ResultActivity::class.java).apply {
            putExtra(EXTRA_LABEL, result.label)
            putExtra(EXTRA_CONFIDENCE, result.confidence)
            putExtra(EXTRA_CATEGORY, result.category)
        }
        startActivity(intent)
    }

    private fun onScanClicked() {
        if (!isScanning) {
            isScanning = true
        }
    }

    override fun onResume() {
        super.onResume()
        if (::cameraProvider.isInitialized) {
            startCamera()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        const val EXTRA_LABEL = "EXTRA_LABEL"
        const val EXTRA_CONFIDENCE = "EXTRA_CONFIDENCE"
        const val EXTRA_CATEGORY = "EXTRA_CATEGORY"
    }
}