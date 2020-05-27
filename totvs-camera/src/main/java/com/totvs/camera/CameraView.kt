package com.totvs.camera

import android.Manifest
import android.Manifest.permission
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.display.DisplayManager
import android.hardware.display.DisplayManager.DisplayListener
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Parcelable
import android.util.AttributeSet
import android.view.Surface
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.CallSuper
import androidx.annotation.FloatRange
import androidx.annotation.MainThread
import androidx.annotation.RequiresPermission
import androidx.camera.core.CameraSelector
import androidx.camera.core.CameraX
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.facebook.react.bridge.LifecycleEventListener
import com.facebook.react.uimanager.ThemedReactContext

/**
 * A [android.view.View] that display a camera preview and has the [Camera] capabilities.
 *
 * This component is lifecycle aware and must be bound a lifecycle in order to render
 * and perform camera's operations. The lifecycle open/close of the camera is handled
 * automatically and is directly tied to the provided lifecycle.
 */
public class CameraView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    style: Int = 0
) : FrameLayout(context, attrs, style), Camera, LifecycleEventListener {

    // Listeners

    /** Display listener */
    private val displayListener = object : DisplayListener {
        override fun onDisplayAdded(displayId: Int) = Unit
        override fun onDisplayRemoved(displayId: Int) = Unit
        override fun onDisplayChanged(displayId: Int) {
            cameraXModule.invalidateView()
        }
    }

    /** [ViewGroup.OnHierarchyChangeListener] to be installed on [previewView] */
    private val viewHierarchyListener = object : OnHierarchyChangeListener {
        override fun onChildViewRemoved(parent: View?, child: View?) = Unit
        override fun onChildViewAdded(parent: View?, child: View?) {
            parent?.measure(
                MeasureSpec.makeMeasureSpec(measuredWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(measuredHeight, MeasureSpec.EXACTLY)
            )
            parent?.layout(0, 0, parent.measuredWidth, parent.measuredHeight)
        }
    }


    /**
     * Lifecycle owner to control [CameraX] lifecycle.
     *
     * This property is a hack around react native environment. Since
     * react native is built on top of old android activity api, it is not
     * lifecycle aware, hence this view context is not a lifecycle owner.
     *
     * We create a custom lifecycle owner tied to react-native self lifecycle
     * when we detect that this view context is not a lifecycle owner, otherwise
     * a custom one is used.
     *
     * This view obtain its lifecycle events from react-native mapping lifecycle events
     * when running on a reac-native environment, otherwise throught a subscription
     * to the holder context.
     *
     * @see also [ReactLifecycleOwner]
     */
    private val lifecycle: LifecycleOwner = when (context) {
        is LifecycleOwner -> context
        is ThemedReactContext -> ReactLifecycleOwner
            .also {
                context.addLifecycleEventListener(this)
            }
        else -> throw IllegalArgumentException("Invalid context type. You must use this view with a LifecycleOwner or ThemedReactContext context")
    }


    /** Preview view where the camera is gonna be displayed */
    internal val previewView: PreviewView = PreviewView(context).apply {
        installHierarchyFitter(this)
    }

    /** CameraX implementation manipulator */
    private val cameraXModule: CameraXModule

    /** [DisplayManager] */
    private val displayManager by lazy {
        context.applicationContext.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
    }

    /** Initialization */
    init {
        addView(previewView, 0)
        cameraXModule = CameraXModule(this).apply {
            @Suppress("MissingPermission") bindToLifecycle(lifecycle)
        }
        setBackgroundResource(android.R.color.black)
    }

    // Computed properties
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

    internal val hasPermissions: Boolean get() = PERMISSIONS_REQUIRED.all {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

    // [Camera] contract
    private var lensFacing: Int
        get() = cameraXModule.facing
        set(value) {
            cameraXModule.facing = value
        }

    override var isTorchEnabled: Boolean
        get() = cameraXModule.isTorchEnabled
        set(value) {
            cameraXModule.isTorchEnabled = value
        }

    override var rotation: Int
        get() = 0
        set(value) = Unit

    override var facing: LensFacing
        get() = if (CameraSelector.LENS_FACING_BACK == cameraXModule.facing) LensFacing.BACK else LensFacing.FRONT
        set(value) {
            cameraXModule.facing = value()
        }

    override var zoom: Float
        get() = cameraXModule.zoom
        set(@FloatRange(from = 0.0, to = 1.0) value) {
            cameraXModule.zoom = value
        }

    override fun toggleCamera() = cameraXModule.toggleCamera()

    // Overrides
    override fun generateDefaultLayoutParams() = LayoutParams(
        LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT
    )

    @CallSuper
    override fun onSaveInstanceState(): Parcelable? {
        super.onSaveInstanceState()
        return Bundle().apply {
            putInt(
                EXTRA_CAMERA_FACING,
                if (CameraSelector.LENS_FACING_BACK == lensFacing) EXTRA_FACING_BACK else EXTRA_FACING_FRONT
            )
        }
    }

    @CallSuper
    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is Bundle) {
            lensFacing = state.getInt(EXTRA_CAMERA_FACING, EXTRA_FACING_BACK)
        } else {
            super.onRestoreInstanceState(state)
        }
        super.onRestoreInstanceState(null) // compensate for kotlin call super requirement
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
            invalidateView()
        }
        super.onLayout(changed, left, top, right, bottom)
    }

    // [Camera] contract

    // Interface methods
    /**
     * Bind this view related camera preview to the [lifecycle]. If at the bind
     * moment the lifecycle is on [androidx.lifecycle.Lifecycle.State.DESTROYED] then
     * the bind fail with a [IllegalArgumentException] otherwise the camera preview will
     * transition to a valid state according to the [lifecycle] state.
     *
     * Most of the time the binding is automatically at this view creation, but if a rebind
     * is needed, then this method can be used.
     */
    @MainThread
    @RequiresPermission(permission.CAMERA)
    public fun bindToLifecycle(lifecycle: LifecycleOwner) {
        cameraXModule.bindToLifecycle(lifecycle)
    }

    /**
     * CameraX add to [previewView] a camera [SurfaceView] which render the camera preview.
     * The [SurfaceView] is added to [previewView] as a posted task on the main thread by
     * [CameraX].
     * It turns out that under an Android native environment the view hierarchy of this view
     * (including [previewView] of course) triggers a [onMeasure] and [onLayout] calls
     * that force this view and it whole hierarchy to update which in turn will cause the
     * camera preview to show because of the render of the [SurfaceView] added to [previewView],
     * but under a react-native environment, after the first pass of [onMeasure] and [onLayout]
     * any changes made to this view hierarchy won't be reflected because no recalculation or
     * re-layout would be performed.
     * e.g
     * if during a layout pass on this view we add a view on [onMeasure] method, then all the views
     * added will be displayed and correctly rendered, in both environments, react-native and
     * Android, but if instead we postpone by [post] an addition of a new child to this view or
     * [previewView] then on an Android environment, any action of parent.addView(child) will
     * trigger on a parent a re-layout request and cause to create another pass of [onMeasure]
     * and [onLayout] which will effectively render the childrens appropriately, but under
     * react-native after the first pass any view added on a new task that is not the one
     * running the current pass, wont trigger another pass. i.e no children will be visible
     * if we add parent.addView(child) in a posted task.
     *
     * This brought as a consequence that under an Android environment, we could see the camera
     * preview but under react-native we couldn't because [CameraX] postpone the addition of the
     * surface to [previewView] way after the first pass on this view has ended.
     *
     * The purpose of this method is install a [ViewGroup.OnHierarchyChangeListener] to [previewView] so
     * we detect when a view is added to it so we trigger manually the pass for re-layout
     * on [previewView]. We do this selectively only when the app is running under a react-native
     * environment.
     *
     * @see also these issues:
     * 1. https://groups.google.com/a/android.com/forum/#!topic/camerax-developers/G9jKs1Bo_CE
     * 2. https://github.com/facebook/react-native/issues/17968
     */
    private fun installHierarchyFitter(view: ViewGroup) {
        if (context is ThemedReactContext) { // only react-native setup
            view.setOnHierarchyChangeListener(viewHierarchyListener)
        }
    }

    // Bridge lifecycle event listeners
    override fun onHostResume()  = (lifecycle as? ReactLifecycleOwner)?.onHostResume() ?: Unit
    override fun onHostPause()   = (lifecycle as? ReactLifecycleOwner)?.onHostPause() ?: Unit
    override fun onHostDestroy() = (lifecycle as? ReactLifecycleOwner)?.onHostDestroy() ?: Unit


    companion object {
        private const val TAG = "CameraView"

        private const val EXTRA_CAMERA_FACING = "camera_facing"
        private const val EXTRA_FACING_BACK = CameraSelector.LENS_FACING_BACK
        private const val EXTRA_FACING_FRONT = CameraSelector.LENS_FACING_FRONT

        private val PERMISSIONS_REQUIRED = arrayOf(permission.CAMERA)
    }
}
