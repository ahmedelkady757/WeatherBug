package com.example.weatherbug.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "alerts")
data class AlertItem(
    @PrimaryKey(autoGenerate = true)
    val id:        Int     = 0,
    val startTime: Long,
    val endTime:   Long,
    val alarmType: String,
    val isActive:  Boolean = true
) {
    companion object {
        const val ALARM_TYPE_NOTIFICATION = "NOTIFICATION"
        const val ALARM_TYPE_ALARM        = "ALARM"
    }
}