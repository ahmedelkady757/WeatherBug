package com.example.weatherbug.core.location

import android.annotation.SuppressLint
import com.example.weatherbug.core.util.AppLogger
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class FusedLocationProvider(
    private val client: FusedLocationProviderClient
) : LocationProvider {

    @SuppressLint("MissingPermission")
    override suspend fun getLastLocation(): Pair<Double, Double>? {
        return suspendCancellableCoroutine { cont ->
            client.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        AppLogger.logVmEvent(
                            "FusedLocationProvider",
                            "lastLocation → lat=${location.latitude} lon=${location.longitude}"
                        )
                        cont.resume(location.latitude to location.longitude)
                    } else {
                        AppLogger.logVmEvent("FusedLocationProvider", "lastLocation → null")
                        cont.resume(null)
                    }
                }
                .addOnFailureListener { e ->
                    AppLogger.logVmError("FusedLocationProvider", "lastLocation failed: ${e.message}")
                    cont.resume(null)
                }
        }
    }

    @SuppressLint("MissingPermission")
    override suspend fun getCurrentLocation(): Pair<Double, Double>? {
        val cts = CancellationTokenSource()
        return suspendCancellableCoroutine { cont ->
            client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
                .addOnSuccessListener { location ->
                    if (location != null) {
                        AppLogger.logVmEvent(
                            "FusedLocationProvider",
                            "currentLocation → lat=${location.latitude} lon=${location.longitude}"
                        )
                        cont.resume(location.latitude to location.longitude)
                    } else {
                        AppLogger.logVmEvent("FusedLocationProvider", "currentLocation → null")
                        cont.resume(null)
                    }
                }
                .addOnFailureListener { e ->
                    AppLogger.logVmError("FusedLocationProvider", "currentLocation failed: ${e.message}")
                    cont.resume(null)
                }
            cont.invokeOnCancellation { cts.cancel() }
        }
    }
}
