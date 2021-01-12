package com.totvs.clockin.vision.internal

import android.util.Log
import com.totvs.clockin.vision.core.ModelOutput
import org.json.JSONArray
import org.json.JSONObject

internal data class NativeOutput(
    override val status: String,
    override val encoding: String,
    override val entities: List<NativeFace>
) : ModelOutput<NativeFace> {

    companion object {
        val Null = NativeOutput(status = "", encoding = "", entities = emptyList())

        fun fromJson(json: String): NativeOutput = try {
            with(JSONObject(json)) {
                NativeOutput(
                    status = optString("status", ""),
                    encoding = optString("embedding", ""),
                    entities = parseFaces(optJSONArray("results"))
                )
            }
        } catch (ex: Exception) {
            Null
        }

        private fun parseFaces(results: JSONArray?): List<NativeFace> = try {
            if (null == results)
                emptyList()
            else {
                mutableListOf<NativeFace>().apply {
                    for (i in 0 until results.length()) {
                        NativeFace.fromJson(results.getJSONObject(i))
                            .takeIf {
                                it != NativeFace.Null
                            }
                            ?.let(this::add)
                    }
                }
            }
        } catch (ex: Exception) {
            Log.e("NativeOutput", "Error parsing output", ex)
            emptyList()
        }
    }
}