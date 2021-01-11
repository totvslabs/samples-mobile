package com.totvs.clockin.vision.internal

import android.graphics.Bitmap
import android.util.Log
import androidx.annotation.WorkerThread
import com.totvs.clockin.vision.core.*
import com.totvs.clockin.vision.core.Model.Config
import com.totvs.clockin.vision.face.Face
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Face model backed by native implementation
 */
internal class NativeFaceModel private constructor(
    private val config: Config
) : RecognitionModel<Bitmap, Face>,
    DetectionModel<Bitmap, Face> {

    private val model by lazy {
        FaceRecognizer(config.modelDirectory).apply {
            loadEmbeddings(config.modelDirectory)
        }
    }

    private val trained = AtomicBoolean(false)

    override val isTrained: Boolean get() = trained.get()

    @WorkerThread
    override fun init() {
        if (isDebug) {
            Log.i(TAG, "Initializing model")
        }
        // no-op the backing model do auto-initialization
    }

    @WorkerThread
    override fun train() {
        if (isDebug) {
            Log.i(TAG, "Training model")
        }
        model // since model is lazy let's trigger the side effect of the initialization
        trained.set(true)
    }

    @WorkerThread
    override fun release() {
        if (isDebug) {
            Log.i(TAG, "Releasing model")
        }
        trained.set(false)
    }

    protected fun finalize() = release()

    @WorkerThread
    override fun recognize(
        input: Bitmap,
        includeDetection: Boolean,
        onRecognized: (ModelOutput<Face>) -> Unit
    ) {
        if (isDebug) {
            Log.i(TAG, "Performing recognition on: ${input.hashCode()}")
        }
        if (!trained.get()) {
            throw IllegalStateException("FaceModel not trained yet.")
        }
        // sending up the results.
        onRecognized(NativeOutput.fromJson(
            model.faceRecognition(input, /*skip_detection=*/ !includeDetection)
        ))
    }

    @WorkerThread
    override fun detect(input: Bitmap, onDetected: (ModelOutput<Face>) -> Unit) {
        if (isDebug) {
            Log.i(TAG, "Performing recognition on: ${input.hashCode()}")
        }
        if (!trained.get()) {
            throw IllegalStateException("FaceModel not trained yet.")
        }
        onDetected(ModelOutput())
    }

    /**
     * Companion object
     */
    companion object {
        private const val TAG = "NativeFaceModel"

        private val isDebug get() = ClockInVisionModuleOptions.DEBUG_ENABLED

        private lateinit var instance: NativeFaceModel

        /**
         * Returns a singleton instance of this model.
         */
        fun getInstance(config: Config) = synchronized(NativeFaceModel) {
            if (::instance.isInitialized) instance else {
                NativeFaceModel(config).also {
                    instance = it
                }
            }
        }
    }
}