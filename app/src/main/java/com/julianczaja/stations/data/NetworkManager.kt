package com.julianczaja.stations.data

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.core.content.getSystemService

class NetworkManager(private val context: Context) {

    fun isCurrentlyConnected() = context.getSystemService<ConnectivityManager>()
        ?.isCurrentlyConnected() ?: false

    private fun ConnectivityManager.isCurrentlyConnected(): Boolean = activeNetwork
        ?.let(::getNetworkCapabilities)
        ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        ?: false
}
