package com.totvs.camera.view

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import androidx.annotation.CallSuper
import androidx.annotation.GuardedBy
import androidx.annotation.MainThread
import com.totvs.camera.core.CameraFacing
import com.totvs.camera.view.GraphicOverlay.Graphic

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

    internal lateinit var host: CameraView

    val isFrontCamera get() = host.facing == CameraFacing.FRONT

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

        @CallSuper
        open fun onAttached(overlay: GraphicOverlay) {
            this.overlay = overlay
        }

        /**
         * Callback to draw the graphic on the [canvas]
         */
        @MainThread
        abstract fun onDraw(canvas: Canvas)

        /**
         * Post invalidate command onto [GraphicOverlay]
         */
        fun postInvalidate() {
            if (!::overlay.isInitialized)
                throw IllegalStateException("This graphic hasn't been attached to any GraphicOverlay")

            overlay.postInvalidate()
        }
    }
}