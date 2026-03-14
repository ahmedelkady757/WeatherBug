package com.example.weatherbug.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "alerts")
data class AlertItem(
    @PrimaryKey(autoGenerate = true)
    val id:               Int     = 0,
    val startTime:        Long,
    val endTime:          Long,
    val alarmType:        String,
    val weatherCondition: String  = CONDITION_ANY,
    val isActive:         Boolean = true
) {
    companion object {
        const val ALARM_TYPE_NOTIFICATION = "NOTIFICATION"
        const val ALARM_TYPE_ALARM        = "ALARM"

        const val CONDITION_ANY           = "Any"
        const val CONDITION_RAIN          = "Rain"
        const val CONDITION_SNOW          = "Snow"
        const val CONDITION_THUNDERSTORM  = "Thunderstorm"
        const val CONDITION_CLEAR         = "Clear"
        const val CONDITION_CLOUDS        = "Clouds"
        const val CONDITION_FOG           = "Fog"

        val ALL_CONDITIONS = listOf(
            CONDITION_ANY,
            CONDITION_RAIN,
            CONDITION_SNOW,
            CONDITION_THUNDERSTORM,
            CONDITION_CLEAR,
            CONDITION_CLOUDS,
            CONDITION_FOG
        )


        fun matches(condition: String, apiMain: String): Boolean = when (condition) {
            CONDITION_ANY          -> true
            CONDITION_RAIN         -> apiMain.equals("Rain", ignoreCase = true)
                                   || apiMain.equals("Drizzle", ignoreCase = true)
            CONDITION_SNOW         -> apiMain.equals("Snow", ignoreCase = true)
            CONDITION_THUNDERSTORM -> apiMain.equals("Thunderstorm", ignoreCase = true)
            CONDITION_CLEAR        -> apiMain.equals("Clear", ignoreCase = true)
            CONDITION_CLOUDS       -> apiMain.equals("Clouds", ignoreCase = true)
            CONDITION_FOG          -> apiMain.equals("Fog", ignoreCase = true)
                                   || apiMain.equals("Mist", ignoreCase = true)
                                   || apiMain.equals("Haze", ignoreCase = true)
                                   || apiMain.equals("Smoke", ignoreCase = true)
            else                   -> true
        }
    }
}