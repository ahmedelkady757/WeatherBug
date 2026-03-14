package com.example.weatherbug.core.location

import android.annotation.SuppressLint

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
                        cont.resume(location.latitude to location.longitude)
                    } else {

                        cont.resume(null)
                    }
                }
                .addOnFailureListener { e ->

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
                        cont.resume(location.latitude to location.longitude)
                    } else {

                        cont.resume(null)
                    }
                }
                .addOnFailureListener { e ->

                    cont.resume(null)
                }
            cont.invokeOnCancellation { cts.cancel() }
        }
    }
}
