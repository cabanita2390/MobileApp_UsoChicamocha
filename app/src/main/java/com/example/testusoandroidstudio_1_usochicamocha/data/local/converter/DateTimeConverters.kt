package com.example.testusoandroidstudio_1_usochicamocha.data.local.converter

import androidx.room.TypeConverter
import java.time.LocalDateTime
import java.time.ZoneId

class DateTimeConverters {

    @TypeConverter
    fun fromLocalDateTime(dateTime: LocalDateTime?): Long? {
        return dateTime?.atZone(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
    }

    @TypeConverter
    fun toLocalDateTime(millis: Long?): LocalDateTime? {
        return millis?.let {
            LocalDateTime.ofInstant(
                java.time.Instant.ofEpochMilli(it),
                ZoneId.systemDefault()
            )
        }
    }
}
