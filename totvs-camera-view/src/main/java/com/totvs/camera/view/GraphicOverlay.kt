package com.totvs.camera.view

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import androidx.annotation.GuardedBy
import com.totvs.camera.core.CameraFacing

/**
 * Overlay view rendered on top of the [CameraView] so we can offer the ability to draw on
 * top of the camera preview.
 *
 * This view is an aggregation of [Graphic] views.
 *
 * Since the [GraphicOverlay] and preview images might be in different coordinate system,
 * we must offer some capabilities to [Graphic] children to transform values to this
 * [GraphicOverlay] coordinate. we do that by computing some scale factors
 */
class GraphicOverlay @JvmOverloads internal constructor(
    context: Context,
    attrs: AttributeSet? = null,
    style: Int = 0
) : View(context, attrs, style) {

    lateinit var host: CameraView

    /**
     * We compute the scale factors by which we need to scale point from the preview to this
     * overlay view coordinate system.
     */
    private val scaleFactorX: Float get() = host.width  / host.previewSize.width.toFloat()
    private val scaleFactorY: Float get() = host.height / host.previewSize.height.toFloat()

    @GuardedBy("this")
    private val graphics = mutableListOf<Graphic>()

    init {
        setWillNotDraw(false) // let's force the onDraw call
    }

    @Synchronized
    fun add(graphic: Graphic) {
        graphics.apply {
            remove(graphic)
            add(graphic)
        }
        graphic.onAttached(this)
        invalidate()
    }

    @Synchronized
    fun remove(graphic: Graphic) {
        graphics.remove(graphic)
        invalidate()
    }

    @Synchronized
    fun clear() {
        graphics.clear()
        invalidate()
    }

    @Synchronized
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        graphics.forEach { it.onDraw(canvas) }
    }

    abstract class Graphic {
        protected lateinit var overlay: GraphicOverlay

        fun onAttached(overlay: GraphicOverlay) {
            this.overlay = overlay
        }

        /**
         * Callback to draw the graphic on the [canvas]
         */
        abstract fun onDraw(canvas: Canvas)

        /**
         * Post invalidate command onto [GraphicOverlay]
         */
        fun postInvalidate() {
            overlay.postInvalidate()
        }

        /**
         * Scale [x] to the host [GraphicOverlay] coordinate system
         */
        fun scaleX(x: Float) = x * overlay.scaleFactorX

        /**
         * Scale [y] to the host [GraphicOverlay] coordinate system
         */
        fun scaleY(y: Float) = y * overlay.scaleFactorY

        /**
         * Translate [x] into the host [GraphicOverlay] coordinate system
         */
        fun translateX(x: Float) = if (overlay.host.facing == CameraFacing.FRONT) {
            overlay.width - scaleX(x) // if front camera, we need to mirror
        } else scaleX(x)

        /**
         * Translate [y] into the host [GraphicOverlay] coordinate system
         */
        fun translateY(y: Float) = scaleY(y)
    }
}