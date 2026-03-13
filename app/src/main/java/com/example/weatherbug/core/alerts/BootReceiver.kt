package com.example.weatherbug.core.alerts

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.weatherbug.data.repo.WeatherRepo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject


class BootReceiver : BroadcastReceiver(), KoinComponent {

    private val repo:      WeatherRepo    by inject()
    private val scheduler: AlarmScheduler by inject()

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        CoroutineScope(Dispatchers.IO).launch {
            val alerts = repo.getAllAlerts().first()
            val now    = System.currentTimeMillis()
            alerts.forEach { alert ->
                if (alert.startTime > now) {
                    scheduler.schedule(alert)
                } else {
                    repo.deleteAlert(alert)
                }
            }
        }
    }
}
