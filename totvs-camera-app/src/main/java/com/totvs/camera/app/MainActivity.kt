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
import com.totvs.camera.Camera
import com.totvs.camera.CameraView
import com.totvs.camera.LensFacing

private const val PERMISSIONS_REQUEST_CODE = 10
private val PERMISSIONS_REQUIRED = arrayOf(Manifest.permission.CAMERA)

class MainActivity : AppCompatActivity() {

    // matching that of [CameraView]
    private var facing: LensFacing = LensFacing.BACK

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!hasPermissions(this)) {
            requestPermissions(PERMISSIONS_REQUIRED, PERMISSIONS_REQUEST_CODE)
        }

        addCameraControls()
    }

    private fun addCameraControls() {
        val container = findViewById<ConstraintLayout>(R.id.camera_container)

        val camera = container.findViewById<CameraView>(R.id.camera_view) as Camera

        // delete them if already attached
        findViewById<ConstraintLayout>(R.id.camera_ui_container)?.let {
            container.removeView(it)
        }

        val controls = View.inflate(this, R.layout.camera_ui_controls, container)

        controls.findViewById<ImageButton>(R.id.camera_capture_button).setOnClickListener {
            camera.takePicture { file ->
                Log.e("**", "file saved")
            }
        }

        controls.findViewById<ImageButton>(R.id.camera_switch_button).setOnClickListener {
            facing = if (facing == LensFacing.BACK) LensFacing.FRONT else LensFacing.BACK
            camera.setFacing(facing)
        }

        controls.findViewById<ImageButton>(R.id.camera_flash_button).setOnClickListener {
            camera.isFlashEnabled = !camera.isFlashEnabled
//            camera.zoom(0.5f)
//            camera.setTargetRotation(90) // under revision
        }
    }

    companion object {
        fun hasPermissions(context: Context) = PERMISSIONS_REQUIRED.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }
}