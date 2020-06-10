package com.totvs.camera.view

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.util.Size
import android.view.View
import androidx.annotation.CallSuper
import androidx.annotation.GuardedBy
import androidx.annotation.MainThread
import com.totvs.camera.core.CameraFacing
import com.totvs.camera.view.GraphicOverlay.Graphic
import kotlin.math.ceil
import kotlin.math.max

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

    val size get() = Size(width, height)

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
        /**
         * Host [GraphicOverlay] of this graphic
         */
        protected lateinit var overlay: GraphicOverlay

        /**
         * Values by which we need to offset scaled values to center then
         * according to this overlay.size center.
         */
        private var offsetX: Int = 0
        private var offsetY: Int = 0

        /**
         * Scale used to scale values into source coordinates to this [overlay.size] coordinate
         */
        private var scale: Float = 0f

        /**
         * Cached coordinates. optimization purpose
         */
        private var lastSourceCoordinate: Size = Size(0, 0)
        private var lastOverlayCoordinate: Size = Size(0, 0)

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

        /**
         * Scale [x] into this graphic overlay coordinate. [x] is assumed to be in [source]
         * coordinate
         */
        fun scaleX(x: Float, source: Size): Float {
            ensureScales(source)
            return x * scale + offsetX
        }

        /**
         * Scale [y] into this graphic overlay coordinate. [y] is assumed to be in [source]
         * coordinate
         */
        fun scaleY(y: Float, source: Size): Float {
            ensureScales(source)
            return y * scale + offsetY
        }

        /**
         * Translate [x] to this graphic overlay coordinate. [x] is assumed to be in [source]
         * coordinate
         */
        fun translateX(x: Float, source: Size): Float {
            // The front facing image is flipped, so we need to mirror the positions on the vertical axis
            return if (overlay.isFrontCamera) {
                scaleX(source.width - x, source)
            } else {
                scaleX(x, source)
            }
        }

        /**
         * Translate [y] to this graphic overlay coordinate. [y] is assumed to be in [source]
         * coordinate
         */
        fun translateY(y: Float, source: Size): Float = scaleY(y, source)

        /**
         * This function computes the required values to scale any point in [source]
         * coordinate to [overlay.size] coordinate.
         *
         * Out goals here is to expand the sourceSize as much as it fills the target area but
         * preserving the aspect ratio of the source size. In either orientation in order to
         * expand source size to cover at least the maximum side or target we need to get
         * the max ratio between targetWidth/sourceWidth or targetHeight/sourceHeight
         * because doing so we guarantee that the expanded source size will cover the maximum
         * dimension  of target and will go over the smallest dimension of target. If we chose
         * the smallest ratio instead we might get that the expanded source size will be inside
         * the target area and we don't want that. we want to go over target area but preserving
         * source size aspect ratio. once we determine the ratio multiplying both sides of the
         * source size by the same ratio won't affect it aspect ratio. we use also this ratio
         * to expand the coordinates of the boundingBox.
         */
        private fun ensureScales(source: Size) {
            if (!needsReComputation(source)) return

            val targetWidth = overlay.size.width
            val targetHeight = overlay.size.height

            scale = max(
                targetWidth / source.width.toFloat(),
                targetHeight / source.height.toFloat()
            )
            // this is the new area in which we're gonna display our object.
            val scaledSize = Size(
                ceil(source.width * scale).toInt(),
                ceil(source.height * scale).toInt()
            )

            // calculate the offset to center [scaledSize] into target box.
            offsetX = (targetWidth - scaledSize.width) / 2
            offsetY = (targetHeight - scaledSize.height) / 2

            lastOverlayCoordinate = overlay.size
            lastSourceCoordinate = source
        }

        private fun needsReComputation(source: Size): Boolean =
            source != lastSourceCoordinate || lastOverlayCoordinate != overlay.size
    }
}