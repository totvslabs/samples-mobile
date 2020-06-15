package com.totvs.clockin.vision.events

import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReactContext
import com.facebook.react.uimanager.events.RCTEventEmitter
import com.totvs.clockin.vision.face.Face
import java.io.File

/**
 * Event emitted when face recognition is triggered.
 */
class OnFaceRecognized(
    private val savedImage: File? = null,
    private val faces: List<Face>
) : Event {

    override fun invoke(context: ReactContext, viewId: Int) {
        val persons = Arguments.createArray()
        for (face in faces) {
            if (null == face.label) {
                // If label is null, there is no personId.
                continue
            }
            val parts = face.label!!.split("_")

            val person = Arguments.createMap().apply {
                putString(FIELD_PERSON_ID, parts[0])
                putString(FIELD_PERSON_NAME, if (parts.size > 1) parts[1] else "")
                putDouble(FIELD_CONFIDENCE, face.confidence.toDouble())
                putString(FIELD_IMAGE_ENCODING, face.encoding)
            }
            persons.pushMap(person)
        }

        val event = Arguments.createMap().apply {
            savedImage?.let { path ->
                putString(FIELD_IMAGE_PATH, path.absolutePath)
            }
            putArray(FIELD_RESULTS, persons)
        }
        // dispatch the event.
        context.getJSModule(RCTEventEmitter::class.java).receiveEvent(viewId, name, event)
    }

    companion object : Event.Export {
        override val name: String = "onFaceRecognized"

        private const val FIELD_PERSON_ID = "personid"
        private const val FIELD_PERSON_NAME = "name"
        private const val FIELD_CONFIDENCE = "confidence"
        private const val FIELD_IMAGE_ENCODING = "imageEncoding"
        private const val FIELD_IMAGE_PATH = "imageFilePath"
        private const val FIELD_RESULTS = "results"
    }
}