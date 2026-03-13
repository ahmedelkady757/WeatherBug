package com.example.weatherbug.core.alerts

import android.content.Context
import android.media.Ringtone
import android.media.RingtoneManager


object AlarmSoundPlayer {

    private var ringtone: Ringtone? = null

    fun play(context: Context) {
        val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        ringtone = RingtoneManager.getRingtone(context, uri)
        ringtone?.play()
    }

    fun stop() {
        ringtone?.stop()
        ringtone = null
    }
}
