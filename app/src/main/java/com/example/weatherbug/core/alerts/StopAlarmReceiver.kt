package com.example.weatherbug.core.alerts

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.app.NotificationManager

/**
 * Receives the "Stop Alarm" action from the weather alert notification.
 * Stops the ringtone started by [AlarmSoundPlayer] and dismisses the notification.
 */
class StopAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        AlarmSoundPlayer.stop()

        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)
        if (notificationId != -1) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                    as NotificationManager
            manager.cancel(notificationId)
        }
    }

    companion object {
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
    }
}
