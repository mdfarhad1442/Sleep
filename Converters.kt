package com.example.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromListIntToString(list: List<Int>?): String {
        return list?.joinToString(",") ?: ""
    }

    @TypeConverter
    fun fromStringToListInt(value: String?): List<Int> {
        if (value.isNullOrBlank()) return emptyList()
        return value.split(",").mapNotNull { it.trim().toIntOrNull() }
    }
}
