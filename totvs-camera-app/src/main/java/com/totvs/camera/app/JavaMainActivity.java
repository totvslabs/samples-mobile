package com.totvs.camera.app;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.totvs.camera.core.Camera;
import com.totvs.camera.core.LensFacing;

/**
 * Use as alternative to MainActivity. must be registered in the manifest first
 */
public class JavaMainActivity extends AppCompatActivity {

    private LensFacing facing = LensFacing.BACK;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        addCameraControls();
    }

    private void addCameraControls() {
        final ViewGroup container = findViewById(R.id.camera_container);

        final Camera camera = (Camera) container.findViewById(R.id.camera_view);

        // delete them if already attached
        View controls = findViewById(R.id.camera_ui_container);
        container.removeView(controls);

        controls = View.inflate(this, R.layout.camera_ui_controls, container);

        controls.findViewById(R.id.camera_capture_button).setOnClickListener(view -> {
//            camera.takePicture(file -> { });
        });

        controls.findViewById(R.id.camera_switch_button).setOnClickListener(view -> {
            if (facing == LensFacing.BACK) {
                facing = LensFacing.FRONT;
            } else {
                facing = LensFacing.BACK;
            }
            camera.setFacing(facing);
        });

        controls.findViewById(R.id.camera_flash_button).setOnClickListener(view -> {
            camera.setTorchEnabled(camera.isTorchEnabled());
//            camera.zoom(0.5f)
//            camera.setTargetRotation(90) // under revision
        });
    }
}
