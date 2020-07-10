package com.totvs.camera.app

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.totvs.camera.app.vision.barcode.AnimateBarcode
import com.totvs.camera.app.vision.barcode.BarcodeBoundingBoxV1
import com.totvs.camera.app.vision.barcode.BarcodeBoundingBoxV2
import com.totvs.camera.app.vision.barcode.TranslateBarcode
import com.totvs.camera.app.vision.face.AnimateEyes
import com.totvs.camera.app.vision.face.FaceGraphic
import com.totvs.camera.core.CameraFacing
import com.totvs.camera.core.ImageProxy
import com.totvs.camera.core.OutputFileOptions
import com.totvs.camera.view.CameraView
import com.totvs.camera.view.core.CameraViewModuleOptions
import com.totvs.camera.vision.DetectionAnalyzer
import com.totvs.camera.vision.barcode.BarcodeDetector
import com.totvs.camera.vision.barcode.BarcodeObject
import com.totvs.camera.vision.core.VisionModuleOptions
import com.totvs.camera.vision.face.FaceObject
import com.totvs.camera.vision.face.FastFaceDetector
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
    private val connections = mutableListOf<Connection>()

    private val analyzer by lazy {
        DetectionAnalyzer(
            executor,
            FastFaceDetector(this),
//            FaceDetector(),
            BarcodeDetector()
        ).apply {
            disable(BarcodeDetector)
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

        val container = findViewById<ViewGroup>(R.id.camera_container)
        container.postDelayed({
            container.systemUiVisibility = FLAGS_FULLSCREEN
        }, IMMERSIVE_FLAG_TIMEOUT)
    }


    override fun onDestroy() {
        super.onDestroy()
        executor.shutdownNow()
        connections.forEach { it.disconnect() }
    }

    private fun addCameraControls() {
        val manager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val characteristics = manager.getCameraCharacteristics("0")
        val stream = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)!!
        val sizes = stream.getOutputSizes(ImageFormat.JPEG)
        sizes.forEach {
            Log.e("***", "size: $it")
        }

        val container = findViewById<ViewGroup>(R.id.camera_container)

        val camera = container.findViewById<CameraView>(R.id.camera_view)

        // delete them if already attached
        findViewById<ConstraintLayout>(R.id.camera_ui_container)?.let {
            container.removeView(it)
        }

        val controls = View.inflate(this, R.layout.camera_ui_controls, container)

        controls.findViewById<ImageButton>(R.id.camera_capture_button).setOnClickListener {
            camera.takePicture { image: ImageProxy?, throwable: Throwable? ->
                image?.use {
                    Log.e(
                        "***",
                        "orientation: ${image.imageInfo.rotationDegrees} ${image.width}x${image.height}"
                    )
                }
            }
//            camera.takePicture(OutputFileOptions.NULL) { file, ex ->
//                Log.e("**", "file saved: ${file?.absolutePath}")
//            }
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

        // install graphics: barcode
        val barcodeBoundingBox: VisionReceiver<BarcodeObject> =
            if (USE_BARCODE_BOUNDING_BOX_V1) {
                BarcodeBoundingBoxV1(this).apply {
                    camera.addOverlayGraphic(this)
                }
            } else {
                BarcodeBoundingBoxV2(this).apply {
                    camera.addOverlayGraphic(this)
                }
            }
        // install graphics: face
        val faceGraphics = FaceGraphic(this).apply {
            camera.addOverlayGraphic(this)
        }
        // face detections
        analyzer.detections
            .filterIsInstance<FaceObject>()
            .sendOn(ContextCompat.getMainExecutor(this))
            .transform(AnimateEyes())
            .connect(faceGraphics)
            .also { connections.add(it) }

        // barcode detections
        if (USE_BARCODE_BOUNDING_BOX_V1) {
            analyzer.detections
                .filterIsInstance<BarcodeObject>()
                // install the coordinate translate transformer
                .transform(TranslateBarcode(camera.graphicOverlay))
                .sendOn(ContextCompat.getMainExecutor(this))
                .transform(AnimateBarcode()) // on main thread
                .connect(barcodeBoundingBox)
                .also {
                    connections.add(it)
                }
        } else {
            analyzer.detections
                .filterIsInstance<BarcodeObject>()
                .sendOn(ContextCompat.getMainExecutor(this))
                .transform(AnimateBarcode()) // on main thread
                .connect(barcodeBoundingBox)
                .also {
                    connections.add(it)
                }
        }
        camera.analyzer = analyzer
    }


    companion object {
        private const val USE_BARCODE_BOUNDING_BOX_V1 = false

        fun hasPermissions(context: Context) = PERMISSIONS_REQUIRED.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }
}
