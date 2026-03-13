package com.example.weatherbug.core.util


import android.Manifest
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged


class NetworkConnectivityObserver(context: Context) {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager


    fun observe(): Flow<Boolean> = callbackFlow @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE) {

        trySend(isCurrentlyConnected())

        val callback = object : ConnectivityManager.NetworkCallback() {

            override fun onAvailable(network: Network) {
                AppLogger.d("NetworkConnectivityObserver: connection AVAILABLE")
                trySend(true)
            }

            override fun onLost(network: Network) {
                AppLogger.d("NetworkConnectivityObserver: connection LOST")
                trySend(false)
            }

            override fun onUnavailable() {
                AppLogger.d("NetworkConnectivityObserver: connection UNAVAILABLE")
                trySend(false)
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                val hasInternet = networkCapabilities.hasCapability(
                    NetworkCapabilities.NET_CAPABILITY_INTERNET
                ) && networkCapabilities.hasCapability(
                    NetworkCapabilities.NET_CAPABILITY_VALIDATED
                )
                AppLogger.d("NetworkConnectivityObserver: capabilities changed → hasInternet=$hasInternet")
                trySend(hasInternet)
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(request, callback)

        awaitClose {
            AppLogger.d("NetworkConnectivityObserver: unregistering network callback")
            connectivityManager.unregisterNetworkCallback(callback)
        }

    }.distinctUntilChanged()


    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    private fun isCurrentlyConnected(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
}