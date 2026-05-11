package com.themaskedguardian.fiskalniskener

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Size
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.TorchState
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ScannerActivity : AppCompatActivity() {

    private lateinit var cameraView: PreviewView
    private lateinit var btnFlash: ImageButton
    private lateinit var btnClose: ImageButton

    private val analyzerExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private var camera: Camera? = null

    @Volatile
    private var detected = false

    private val barcodeScanner = BarcodeScanning.getClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scanner)

        cameraView = findViewById(R.id.camera_view)
        btnFlash   = findViewById(R.id.btn_flash)
        btnClose   = findViewById(R.id.btn_close)

        btnClose.setOnClickListener { finish() }

        btnFlash.setOnClickListener {
            val isOn = camera?.cameraInfo?.torchState?.value == TorchState.ON
            camera?.cameraControl?.enableTorch(!isOn)
        }

        startCamera()
    }

    private fun startCamera() {
        val future = ProcessCameraProvider.getInstance(this)
        future.addListener({
            val provider = try {
                future.get()
            } catch (e: Exception) {
                Toast.makeText(this, "Greška pri pokretanju kamere", Toast.LENGTH_SHORT).show()
                return@addListener
            }

            // Forsiramo 1080p rezoluciju za preview i analizu (bitno za sitne QR kodove)
            val resolutionSelector = ResolutionSelector.Builder()
                .setResolutionStrategy(ResolutionStrategy(
                    Size(1920, 1080),
                    ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER
                ))
                .build()

            val preview = Preview.Builder()
                .setResolutionSelector(resolutionSelector)
                .build().also {
                    it.setSurfaceProvider(cameraView.surfaceProvider)
                }

            val imageAnalysis = ImageAnalysis.Builder()
                .setResolutionSelector(resolutionSelector)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build().also {
                    it.setAnalyzer(analyzerExecutor, ::analyzeImage)
                }

            try {
                provider.unbindAll()
                camera = provider.bindToLifecycle(
                    this,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageAnalysis
                )
                // Automatski fokus na sredinu ekrana
                camera?.cameraControl?.setLinearZoom(0.1f) // Lagani zum pomaže kod sitnih kodova
            } catch (e: Exception) {
                Toast.makeText(this, "Greška kamere: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun analyzeImage(imageProxy: androidx.camera.core.ImageProxy) {
        if (detected) {
            imageProxy.close()
            return
        }

        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }

        val inputImage = InputImage.fromMediaImage(
            mediaImage,
            imageProxy.imageInfo.rotationDegrees
        )

        barcodeScanner.process(inputImage)
            .addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    val url = barcode.rawValue ?: continue
                    if (barcode.format == Barcode.FORMAT_QR_CODE &&
                        url.contains("suf.purs.gov.rs") &&
                        !detected
                    ) {
                        detected = true
                        val intent = Intent(this, ConfirmationActivity::class.java)
                        intent.putExtra(ConfirmationActivity.EXTRA_URL, url)
                        startActivity(intent)
                        finish()
                        break
                    }
                }
            }
            .addOnFailureListener { }
            .addOnCompleteListener { imageProxy.close() }
    }

    override fun onResume() {
        super.onResume()
        detected = false
    }

    override fun onDestroy() {
        super.onDestroy()
        analyzerExecutor.shutdown()
        barcodeScanner.close()
    }
}
