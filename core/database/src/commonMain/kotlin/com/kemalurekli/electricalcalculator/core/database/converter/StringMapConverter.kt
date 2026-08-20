package com.kemalurekli.electricalcalculator.core.database.converter

import androidx.room.TypeConverter
import kotlinx.serialization.json.Json

/**
 * Persists the input and result maps of a calculation as JSON text.
 *
 * Decoding is defensive: a row written by a different app version — or
 * corrupted on disk — yields an empty map instead of throwing inside a Room
 * query, which would otherwise crash the history screen for every record.
 */
class StringMapConverter {

    @TypeConverter
    fun fromMap(value: Map<String, String>): String = json.encodeToString(value)

    @TypeConverter
    fun toMap(value: String): Map<String, String> =
        runCatching { json.decodeFromString<Map<String, String>>(value) }
            .getOrDefault(emptyMap())

    private companion object {
        val json = Json { ignoreUnknownKeys = true }
    }
}
