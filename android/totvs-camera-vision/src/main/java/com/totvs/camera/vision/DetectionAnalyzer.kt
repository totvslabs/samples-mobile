package com.totvs.camera.vision

import android.util.Log
import android.util.Rational
import android.util.Size
import androidx.annotation.GuardedBy
import com.totvs.camera.core.ImageAnalyzer
import com.totvs.camera.core.ImageProxy
import com.totvs.camera.vision.core.VisionModuleOptions.DEBUG_ENABLED
import com.totvs.camera.vision.stream.BroadcastVisionStream
import com.totvs.camera.vision.stream.VisionStream
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Immutable detection analyzer that keep a registry of [VisionDetector]. By default detectors
 * are enabled until they're manually disabled.
 *
 * This analyzer models a set of detectors that have the potential to be all enabled at the
 * same time or only some of them.
 *
 * An enabled detector is one for which on every run of [analyze] it executes it detection
 * procedure.
 *
 * [VisionDetector] can be enabled and disabled at any time through [enable] and [disable]
 * methods. These operations do nothing if the requested [VisionDetector] doesn't exists
 * in this [DetectionAnalyzer].
 *
 * This analyzer post individual detection tasks on [executor] which means that if the
 * executor can ony run less than the number of detectors passed to this analyzer, the
 * executor will halt until previous detection finishes. It's highly recommended that the
 * executor capacity is at least as the number of detectors passed to this analyzer.
 *
 * This analyzer closes the [ImageProxy] after all the detectors are done with it.
 *
 * Contract [VisionDetector] on this analyzer must meet:
 *
 * 1. By nature this analyzer expect the detectors to emit only one detection, thus is a
 * requirement on the detector to identify the appropriate measure to detect the most
 * prominent detection and report it. The behavior is undefined if multiple detections
 * are emitted by a single detection phase.
 *
 * For cases in which multiple emission are allowed from the detectors on a single phase
 * one can subclass this analyzer and override [analyze] then the detectors can post
 * a kind of [VisionObject] that pack multiple results and having the new analyzer
 * coordinate the detectors emissions.
 *
 * 2. [VisionDetector] on this analyzer are required to call [onDetected] callback so the
 * analyzer trigger that they are done performing their work. If this is not performed
 * this analyzer will lock until the [executor] then is shut down.
 */
open class DetectionAnalyzer(
    private val executor: ExecutorService,
    vararg detectors: VisionDetector<*>
) : ImageAnalyzer {

    // default calculated [ImageAnalyzer] values might still be way to high for our needs.
    // Let's set a custom size for the analysis image instead.
    override fun desiredOutputImageSize(
        orientationDegrees: Int,
        isPortrait: Boolean,
        previewSize: Size,
        previewAspectRatio: Rational
    ): Size = if (isPortrait) Size(480, 640) else Size(640, 480)

    /**
     * Registry of all detectors
     */
    @GuardedBy("this")
    protected val registry = mutableMapOf<VisionDetector.Key<*>, EnabledDetector>()
        .apply {
            detectors.forEach { detector ->
                put(detector.key, EnabledDetector(detector))
            }
        }

    /**
     * [VisionStream] of detection determined by this analyzer.
     *
     * This stream represents the flow of objects detected from all the enabled
     * [VisionDetector] in this analyzer.
     */
    val detections: VisionStream<VisionObject> = BroadcastVisionStream()

    /**
     * We skip frames while we're in the middle of a detection task
     */
    private val isBusy = AtomicBoolean(false)

    /**
     * Returns whether the detector for the [key] is enabled or not
     */
    @Synchronized
    open fun isEnabled(key: VisionDetector.Key<*>) = registry[key]?.enabled ?: false

    /**
     * Enable the detector corresponding to [key]
     */
    @Synchronized
    open fun enable(key: VisionDetector.Key<*>) {
        registry[key]?.let { it.enabled = true }
    }

    /**
     * Disable the detector corresponding to [key]
     */
    @Synchronized
    open fun disable(key: VisionDetector.Key<*>) {
        registry[key]?.let { it.enabled = false }
    }

    /**
     * Get the detector corresponding to [key]
     */
    @Synchronized
    open operator fun get(key: VisionDetector.Key<*>): VisionDetector<*>? = registry[key]?.detector

    /**
     * After detection let's post this object to the stream
     */
    private fun post(value: VisionObject) {
        (detections as BroadcastVisionStream).broadcast(value)
    }

    override fun analyze(image: ImageProxy) {
        if (executor.isShutdown) {
            Log.e(TAG, "Abnormal state. Executor is shutDown")
            image.use { }
            return
        }

        if (isBusy.compareAndSet(false, true)) {
            image.use {
                val detectors = registry.values.filter { it.enabled }
                // let's synchronize the detectors to know when we can analyze another frame
                val latch = CountDownLatch(detectors.size)

                detectors.forEach {
                    executor.execute {
                        runCatching {
                            it.detector.detect(executor, image) { value ->
                                latch.countDown()
                                post(value) // let's post the detected value to the stream
                            }
                        }.exceptionOrNull()?.let { ex -> // if the detector failed for some reason
                            if (DEBUG_ENABLED) {
                                Log.e(TAG, "Detector ${it.detector} failed", ex)
                            }
                            latch.countDown()
                        }
                    }
                }
                try {
                    latch.await()
                } catch (ex: Exception) {
                    if (DEBUG_ENABLED) {
                        Log.i(TAG, "Closing detectors")
                    }
                }
                // all detectors are done, let's set to analyze more images
                isBusy.set(false)
            }
        }
    }

    protected data class EnabledDetector(
        val detector: VisionDetector<*>,
        var enabled: Boolean = true
    )

    companion object {
        private const val TAG = "DetectionAnalyzer"
    }
}