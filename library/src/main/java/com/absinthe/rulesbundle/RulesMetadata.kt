package com.absinthe.rulesbundle

import org.json.JSONObject
import java.io.File

/** Data identity is independent of the Bundle code version. */
data class RulesMetadata(
    val schemaVersion: Int,
    val dataVersion: Long,
    val ruleCount: Int,
    val sourceRevision: String? = null,
    val compilerRevision: String? = null,
    val contentSha256: String? = null,
    val minimumAndroidReader: Int = 5
) {
    internal companion object {
        fun read(file: File): RulesMetadata {
            require(file.isFile && file.length() in 1..65536) { "Invalid metadata.json" }
            val json = JSONObject(file.readText())
            return RulesMetadata(
                schemaVersion = json.integer("schemaVersion").toIntExact(),
                dataVersion = json.integer("dataVersion"),
                ruleCount = json.integer("ruleCount").toIntExact(),
                sourceRevision = json.getString("sourceRevision"),
                compilerRevision = json.getString("compilerRevision"),
                contentSha256 = json.getString("contentSha256"),
                minimumAndroidReader = json.getJSONObject("minimumReader").integer("android").toIntExact()
            ).also {
                require(it.schemaVersion == 5 && it.minimumAndroidReader in 1..5) {
                    "Unsupported rules reader version"
                }
                require(it.dataVersion > 0 && it.ruleCount >= 0) { "Invalid rules metadata" }
                require(it.sourceRevision!!.matches(Regex("[0-9a-f]{40}"))) { "Invalid source revision" }
                require(it.compilerRevision!!.matches(Regex("[0-9a-f]{64}"))) { "Invalid compiler revision" }
                require(it.contentSha256!!.matches(Regex("[0-9a-f]{64}"))) { "Invalid content hash" }
            }
        }

        private fun JSONObject.integer(name: String): Long {
            val value = get(name)
            require(value is Int || value is Long) { "Invalid metadata integer: $name" }
            return (value as Number).toLong()
        }

        private fun Long.toIntExact(): Int {
            require(this in 0..Int.MAX_VALUE.toLong()) { "Metadata integer out of range" }
            return toInt()
        }
    }
}
