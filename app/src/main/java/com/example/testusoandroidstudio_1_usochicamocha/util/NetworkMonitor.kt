package com.example.testusoandroidstudio_1_usochicamocha.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class NetworkMonitor(context: Context) {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    // Usamos un MutableStateFlow para mantener el último estado de la red.
    private val _networkStatus = MutableStateFlow(false)
    // Exponemos un StateFlow inmutable para que el resto de la app lo consuma.
    val networkStatus: StateFlow<Boolean> = _networkStatus.asStateFlow()

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            // Cuando una red está disponible, verificamos sus capacidades
            _networkStatus.value = hasInternetConnection()
        }

        override fun onLost(network: Network) {
            _networkStatus.value = false
        }

        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            val isConnected = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            _networkStatus.value = isConnected
        }
    }

    init {
        // Comprobación inicial
        _networkStatus.value = hasInternetConnection()

        // Registrar el callback
        connectivityManager.registerDefaultNetworkCallback(networkCallback)
    }

    private fun hasInternetConnection(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
}