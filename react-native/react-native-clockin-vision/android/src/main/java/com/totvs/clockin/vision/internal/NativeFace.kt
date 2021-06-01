package com.totvs.clockin.vision.internal

import android.util.Log
import com.totvs.clockin.vision.face.Face
import org.json.JSONObject

internal data class NativeFace(
    override val name: String,
    override val personId: String,
    override val distance: Float
) : Face() {
    companion object {
        val Null = NativeFace(name = "", personId = "", distance = .0f)

        fun fromJson(json: JSONObject): NativeFace = try {
            NativeFace(
                name = json.optString("name"),
                personId = json.optString("person_id"),
                distance = json.optDouble("distance").toFloat()
            )
        } catch (ex: Exception) {
            Log.e("NativeFace", "Error parsing face", ex)
            Null
        }
    }
}