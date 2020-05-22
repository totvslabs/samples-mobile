package com.totvs.camera

import android.content.Context
import android.hardware.display.DisplayManager
import android.hardware.display.DisplayManager.DisplayListener
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Parcelable
import android.util.AttributeSet
import android.view.Surface
import android.widget.FrameLayout
import androidx.camera.core.CameraSelector
import androidx.camera.view.PreviewView

/**
 * Main view where the camera preview is rendered. It conform the set of camera operations
 * that supported through [Camera]. Other operations invoked in this view apart from the
 * one stated in the [Camera] contract, are not guaranteed to be available in future versions
 * of this view.
 *
 */
public class CameraView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    style: Int = 0
) : FrameLayout(context, attrs, style) {

    /** Preview view where the camera is gonna be displayed */
    internal val previewView: PreviewView

    /** CameraX implementation manipulator */
    private val cameraXModule: CameraXModule

    init {
        addView(PreviewView(context).also {
            previewView = it
        })
        cameraXModule = CameraXModule(this)
        setBackgroundResource(android.R.color.black)
    }
    /** Calculated properties */
    /**
     * Returns one of the [android.view.Surface.ROTATION_0] [android.view.Surface.ROTATION_180]
     * [android.view.Surface.ROTATION_90] [android.view.Surface.ROTATION_270] constants
     */
    internal val displaySurfaceRotation: Int
        get() {
            // Null when the view is detached. If we were in the middle of a background operation
            // when it resumes we might found out that this view is not displayed anymore.
            // and the camera was closed
            return display?.rotation ?: 0
        }

    /**
     * Returns the [displaySurfaceRotation] value converted in degrees
     */
    internal val displayRotationDegrees: Int
        get() = when (displaySurfaceRotation) {
            Surface.ROTATION_0 -> 0
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_270 -> 270
            else -> throw IllegalArgumentException("Unsupported surface rotation")
        }

    private val displayManager by lazy {
        context.applicationContext.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
    }

    /** Listeners **/
    private val displayListener = object : DisplayListener {
        override fun onDisplayAdded(displayId: Int) = Unit
        override fun onDisplayRemoved(displayId: Int) = Unit
        override fun onDisplayChanged(displayId: Int) {
            cameraXModule.invalidateViews()
        }
    }

    private var lensFacing: Int
        get() = cameraXModule.facing
        set(value) {
            cameraXModule.facing = value
        }

    override fun generateDefaultLayoutParams() = LayoutParams(
        LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT
    )

    override fun onSaveInstanceState(): Parcelable? {
        super.onSaveInstanceState()
        return Bundle().apply {
            putInt(
                EXTRA_CAMERA_FACING,
                if (CameraSelector.LENS_FACING_BACK == lensFacing) EXTRA_FACING_BACK else EXTRA_FACING_FRONT
            )
        }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is Bundle) {
            lensFacing = state.getInt(EXTRA_CAMERA_FACING, EXTRA_FACING_BACK)
        } else {
            super.onRestoreInstanceState(state)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        displayManager.registerDisplayListener(displayListener, Handler(Looper.getMainLooper()))
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        displayManager.unregisterDisplayListener(displayListener)
    }

    @Suppress("MissingPermission")
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // since bindToLifecycle depends on view dimensions, let's not call it
        // when the dimensions are 0x0
        if (0 < measuredWidth && 0 < measuredHeight) {
            cameraXModule.bindToLifecycleAfterViewMeasured()
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    @Suppress("MissingPermission")
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        // In case the [CameraView] is always set as 0x0 we still need to trigger to
        // cause the lifecycle binding
        cameraXModule.apply {
            bindToLifecycleAfterViewMeasured()
            invalidateViews()
        }
        super.onLayout(changed, left, top, right, bottom)
    }

    companion object {
        private const val TAG = "CameraView"

        private const val EXTRA_CAMERA_FACING = "camera_facing"
        private const val EXTRA_FACING_BACK  = CameraSelector.LENS_FACING_BACK
        private const val EXTRA_FACING_FRONT = CameraSelector.LENS_FACING_FRONT
    }
}
