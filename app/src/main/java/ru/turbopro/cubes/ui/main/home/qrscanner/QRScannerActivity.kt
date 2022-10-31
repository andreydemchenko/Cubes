package ru.turbopro.cubes.ui.main.home.qrscanner

import android.Manifest.permission.CAMERA
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*
import ru.turbopro.cubes.CubesApplication
import ru.turbopro.cubes.R
import ru.turbopro.cubes.data.Result
import ru.turbopro.cubes.databinding.ActivityQrscannerBinding
import ru.turbopro.cubes.viewmodels.AuthViewModel
import ru.turbopro.cubes.viewmodels.QRCodeScannerViewModel
import java.io.IOException


class QRScannerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQrscannerBinding
    private lateinit var viewModel: QRCodeScannerViewModel
    private val requestCodeCameraPermission = 1001
    private lateinit var cameraSource: CameraSource
    private lateinit var barcodeDetector: BarcodeDetector
    private var scannedValue: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(QRCodeScannerViewModel::class.java)
        binding = ActivityQrscannerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (ContextCompat.checkSelfPermission(
                this@QRScannerActivity, CAMERA) != PackageManager.PERMISSION_GRANTED
        ) {
            askForCameraPermission()
        } else {
            setupControls()
        }

        val aniSlide: Animation =
            AnimationUtils.loadAnimation(this@QRScannerActivity, R.anim.scanner_animation)
        binding.barcodeLine.startAnimation(aniSlide)
    }

    private fun checkQRCode() {
        CoroutineScope(Dispatchers.IO).launch {
            if (scannedValue != null) {
                val deferredRes = async {
                    viewModel.authRepository.getLessonNameByCode(scannedValue!!)
                }
                val res = deferredRes.await()
                if (res is Result.Success && res.data.toString() != "null") {
                    checkIfUserNotAlreadyChecked(res.data.toString())
                    Log.d("QRScannerActivity", "checkQRCode: Success, res = $res")
                } else {
                    runOnUiThread {
                        Toast.makeText(applicationContext, getString(R.string.qr_code_wrong_code), Toast.LENGTH_SHORT)
                            .show()
                    }
                    if (res is Result.Error) {
                        Log.d("QRScannerActivity", "checkQRCode: Error, ${res.exception.message}")
                    }
                }
            } else {
                Log.d("QRScannerActivity", "QRCode is Null, Cannot Add!")
            }
        }
    }

    private suspend fun checkIfUserNotAlreadyChecked(lesson: String) {
        CoroutineScope(Dispatchers.IO).launch {
            if (scannedValue != null) {
                val deferredRes = async {
                    viewModel.userData.value?.let { viewModel.authRepository.isUserNotVisitedLesson(lesson, it.userId) }
                }
                val res = deferredRes.await()
                if (res is Result.Success) {
                    if (res.data) {
                        addUserToLesson(lesson)
                        Log.d("QRScannerActivity", "checkIfUserNotAlreadyChecked: Success")
                    } else {
                        runOnUiThread {
                            Toast.makeText(
                                applicationContext,
                                getString(R.string.qr_code_already_registered),
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(
                            applicationContext,
                            getString(R.string.qr_code_something_went_wrong),
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                    if (res is Result.Error) {
                        Log.d("QRScannerActivity", "checkIfUserNotAlreadyChecked: Error, ${res.exception.message}")
                    }
                }
            } else {
                Log.d("QRScannerActivity", "QRCode is Null, Cannot Add!")
            }
        }
    }

    private suspend fun addUserToLesson(lesson: String) {
        CoroutineScope(Dispatchers.IO).launch {
            if (scannedValue != null) {
                val deferredRes = async {
                    viewModel.userData.value?.let { viewModel.authRepository.addUserToLesson(lesson, it.userId) }
                }
                val res = deferredRes.await()
                if (res is Result.Success) {
                    addPoints()
                    runOnUiThread {
                        Toast.makeText(
                            applicationContext,
                            getString(R.string.qr_code_registered_succesfully),
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                    Log.d("QRScannerActivity", "addUserToLesson: Success")
                } else {
                    if (res is Result.Error) {
                        Log.d("QRScannerActivity", "addUserToLesson: Error, ${res.exception.message}")
                    }
                }
            } else {
                Log.d("QRScannerActivity", "QRCode is Null, Cannot Add!")
            }
        }
    }

    private suspend fun addPoints() {
        CoroutineScope(Dispatchers.IO).launch {
            if (scannedValue != null) {
                val deferredRes = async {
                    viewModel.userData.value?.let { viewModel.authRepository.addPointsForVisitingByUserId(3, it.userId) }
                }
                val res = deferredRes.await()
                if (res is Result.Success) {
                    viewModel.authRepository.hardRefreshUserData()
                    Log.d("QRScannerActivity", "addPointsForVisiting: Success")
                } else {
                    if (res is Result.Error) {
                        Log.d("QRScannerActivity", "addPointsForVisiting: Error, ${res.exception.message}")
                    }
                }
            } else {
                Log.d("QRScannerActivity", "QRCode is Null, Cannot Add!")
            }
        }
    }

    private fun setupControls() {
        barcodeDetector =
            BarcodeDetector.Builder(this).setBarcodeFormats(Barcode.ALL_FORMATS).build()

        cameraSource = CameraSource.Builder(this, barcodeDetector)
            .setRequestedPreviewSize(1080, 1080)
            .setAutoFocusEnabled(true)
            .build()

        binding.cameraSurfaceView.holder.addCallback(object : SurfaceHolder.Callback {
            @SuppressLint("MissingPermission")
            override fun surfaceCreated(holder: SurfaceHolder) {
                try {
                    cameraSource.start(holder)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

            @SuppressLint("MissingPermission")
            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {
                try {
                    cameraSource.start(holder)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                cameraSource.stop()
            }
        })

        barcodeDetector.setProcessor(object : Detector.Processor<Barcode> {
            override fun release() {
                Toast.makeText(applicationContext, getString(R.string.qr_code_scanner_closed), Toast.LENGTH_SHORT)
                    .show()
            }

            override fun receiveDetections(detections: Detector.Detections<Barcode>) {
                val barcodes = detections.detectedItems
                if (barcodes.size() == 1) {
                    scannedValue = barcodes.valueAt(0).rawValue
                    checkQRCode()
                    runOnUiThread {
                        cameraSource.stop()
                        //Toast.makeText(this@QRScannerActivity, "value- $scannedValue", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                } else {
                    Toast.makeText(this@QRScannerActivity, "value- else", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun askForCameraPermission() {
        ActivityCompat.requestPermissions(
            this@QRScannerActivity,
            arrayOf(CAMERA),
            requestCodeCameraPermission
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == requestCodeCameraPermission && grantResults.isNotEmpty()) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupControls()
            } else {
                Toast.makeText(applicationContext, getString(R.string.permission_denied), Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraSource.stop()
    }
}