package com.themaskedguardian.fiskalniskener

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Size
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ScannerActivity : AppCompatActivity() {

    private lateinit var cameraExecutor: ExecutorService
    private lateinit var cameraProvider: ProcessCameraProvider
    private var isScanning = true
    private var isFlashOn = false
    private var camera: Camera? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scanner)

        val cameraView = findViewById<PreviewView>(R.id.camera_view)
        val btnClose = findViewById<ImageButton>(R.id.btn_close)
        val btnFlash = findViewById<ImageButton>(R.id.btn_flash)

        btnClose.setOnClickListener { finish() }

        btnFlash.setOnClickListener {
            isFlashOn = !isFlashOn
            camera?.cameraControl?.enableTorch(isFlashOn)
            btnFlash.setImageResource(R.drawable.ic_flash)
        }

        cameraExecutor = Executors.newSingleThreadExecutor()
        startCamera(cameraView)
    }

    private fun startCamera(cameraView: PreviewView) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()

            // Postavljamo visoku rezoluciju za preview
            val preview = Preview.Builder()
                .setTargetResolution(Size(1920, 1080))
                .build().also {
                    it.setSurfaceProvider(cameraView.surfaceProvider)
                }

            // Postavljamo visoku rezoluciju i za analizator - ovo je ključno za oštrinu
            val imageAnalyzer = ImageAnalysis.Builder()
                .setTargetResolution(Size(1920, 1080))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor) { imageProxy ->
                        processImageProxy(imageProxy)
                    }
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalyzer)
            } catch (exc: Exception) {
                Toast.makeText(this, "Greška pri pokretanju kamere", Toast.LENGTH_SHORT).show()
            }

        }, ContextCompat.getMainExecutor(this))
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun processImageProxy(imageProxy: ImageProxy) {
        if (!isScanning) {
            imageProxy.close()
            return
        }

        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            
            // Koristimo najbrži direktan pristup skeneru
            BarcodeScanning.getClient().process(image)
                .addOnSuccessListener { barcodes ->
                    for (barcode in barcodes) {
                        if (barcode.valueType == Barcode.TYPE_URL) {
                            val url = barcode.url?.url ?: ""
                            if (url.contains("suf.purs.gov.rs")) {
                                isScanning = false
                                val intent = Intent(this, ConfirmationActivity::class.java)
                                intent.putExtra("RECEIPT_URL", url)
                                startActivity(intent)
                                finish()
                                break
                            }
                        }
                    }
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
