package com.totvs.camera.app

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.totvs.camera.core.CameraFacing
import com.totvs.camera.core.OutputFileOptions
import com.totvs.camera.view.CameraView
import com.totvs.camera.vision.DetectionAnalyzer
import com.totvs.camera.vision.barcode.BarcodeDetector
import com.totvs.camera.vision.barcode.BarcodeObject
import com.totvs.camera.vision.barcode.NullBarcodeObject
import com.totvs.camera.vision.face.FaceDetector
import com.totvs.camera.vision.face.FaceObject
import com.totvs.camera.vision.face.FastFaceDetector
import com.totvs.camera.vision.face.NullFaceObject
import com.totvs.camera.vision.stream.connect
import com.totvs.camera.vision.stream.filter
import com.totvs.camera.vision.stream.filterIsInstance
import java.util.concurrent.Executors

private const val PERMISSIONS_REQUEST_CODE = 10
private val PERMISSIONS_REQUIRED = arrayOf(Manifest.permission.CAMERA)

class MainActivity : AppCompatActivity() {

    // matching that of [CameraView]
    private var facing: CameraFacing = CameraFacing.BACK

    private val executor = Executors.newCachedThreadPool()

    private val analyzer by lazy {
        DetectionAnalyzer(
            executor,
            FastFaceDetector(this),
//            FaceDetector(),
            BarcodeDetector()
        ).apply {
            disable(FastFaceDetector)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!hasPermissions(this)) {
            requestPermissions(PERMISSIONS_REQUIRED, PERMISSIONS_REQUEST_CODE)
        }
        installAnalyzer()
        addCameraControls()
    }

    override fun onDestroy() {
        super.onDestroy()
        executor.shutdownNow()
    }

    private fun addCameraControls() {
        val container = findViewById<ConstraintLayout>(R.id.camera_container)

        val camera = container.findViewById<CameraView>(R.id.camera_view)

        // delete them if already attached
        findViewById<ConstraintLayout>(R.id.camera_ui_container)?.let {
            container.removeView(it)
        }

        val controls = View.inflate(this, R.layout.camera_ui_controls, container)

        controls.findViewById<ImageButton>(R.id.camera_capture_button).setOnClickListener {
            camera.takePicture(OutputFileOptions.NULL) { file, ex ->
                Log.e("**", "file saved: ${file?.absolutePath}")
            }
        }

        controls.findViewById<ImageButton>(R.id.camera_switch_button).setOnClickListener {
            facing = if (facing == CameraFacing.BACK) CameraFacing.FRONT else CameraFacing.BACK
            camera.facing = facing
        }

        controls.findViewById<ImageButton>(R.id.camera_flash_button).setOnClickListener {
            camera.isTorchEnabled = !camera.isTorchEnabled
        }
    }

    private fun installAnalyzer() {
        val camera = findViewById<CameraView>(R.id.camera_view)

        analyzer.detections
            .filterIsInstance<FaceObject>()
            .filter { it != NullFaceObject }
            .connect {
                Log.e("**", "Face receiving: $it - ${Thread.currentThread().name}")
            }

        analyzer.detections
            .filterIsInstance<BarcodeObject>()
            .filter { it != NullBarcodeObject }
            .connect {
                Log.e("**", "Barcode object: $it")
            }

        camera.analyzer = analyzer
    }

    companion object {
        fun hasPermissions(context: Context) = PERMISSIONS_REQUIRED.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }
}
