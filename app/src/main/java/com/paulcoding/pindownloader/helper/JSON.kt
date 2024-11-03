package com.paulcoding.pindownloader.helper

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

val CustomJson =
    Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

inline fun <reified T> traverseObject(
    obj: JsonElement,
    keys: List<String>,
): T? {
    var curr: JsonElement? = obj

    for (key in keys) {
        curr =
            if (key == "[]") {
                curr?.jsonArray?.get(0)
            } else if (key == "{}") {
                curr?.jsonObject?.let { it[it.keys.firstOrNull()] }
            } else {
                curr?.jsonObject?.get(key)
            }
        if (curr == null || curr == JsonNull) {
            return null
        }
    }
    return curr?.let { CustomJson.decodeFromJsonElement<T>(it) }
}
