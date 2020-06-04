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
import com.totvs.camera.app.vision.AnimateBarcode
import com.totvs.camera.app.vision.BarcodeBoundingBox
import com.totvs.camera.app.vision.TranslateBarcode
import com.totvs.camera.core.CameraFacing
import com.totvs.camera.core.OutputFileOptions
import com.totvs.camera.view.CameraView
import com.totvs.camera.view.core.CameraViewModuleOptions
import com.totvs.camera.vision.DetectionAnalyzer
import com.totvs.camera.vision.barcode.BarcodeDetector
import com.totvs.camera.vision.barcode.BarcodeObject
import com.totvs.camera.vision.core.VisionModuleOptions
import com.totvs.camera.vision.face.FaceObject
import com.totvs.camera.vision.face.FastFaceDetector
import com.totvs.camera.vision.face.NullFaceObject
import com.totvs.camera.vision.stream.*
import java.util.concurrent.Executors

private const val PERMISSIONS_REQUEST_CODE = 10
private val PERMISSIONS_REQUIRED = arrayOf(Manifest.permission.CAMERA)
private const val IMMERSIVE_FLAG_TIMEOUT = 500L

class MainActivity : AppCompatActivity() {

    init {
        // enable debug mode for camera view module
        CameraViewModuleOptions.DEBUG_ENABLED = true
        // enable debug mode for vision module
        VisionModuleOptions.DEBUG_ENABLED = true
    }

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

    override fun onResume() {
        super.onResume()

        val container = findViewById<ConstraintLayout>(R.id.camera_container)
        container.postDelayed({
            container.systemUiVisibility = FLAGS_FULLSCREEN
        }, IMMERSIVE_FLAG_TIMEOUT)
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

        val barcodeBoundingBox = BarcodeBoundingBox(this).apply {
            camera.addOverlayGraphic(this)
        }

        analyzer.detections
            .filterIsInstance<FaceObject>()
            .filter { it != NullFaceObject }
            .sendOn(ContextCompat.getMainExecutor(this))
            .connect {
                Log.e("**", "Face receiving: $it")
            }

        analyzer.detections
            .filterIsInstance<BarcodeObject>()
            .transform(TranslateBarcode(camera.graphicOverlay))
            .sendOn(ContextCompat.getMainExecutor(this))
            .transform(AnimateBarcode()) // on main thread
            .connect(barcodeBoundingBox)

        camera.analyzer = analyzer
    }

    companion object {
        fun hasPermissions(context: Context) = PERMISSIONS_REQUIRED.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }
}
