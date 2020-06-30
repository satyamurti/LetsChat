package com.mrspd.letschat.util

import android.app.Activity
import android.app.Application
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Bundle
import com.mrspd.letschat.util.eventbus_events.ConnectionChangeEvent
import org.greenrobot.eventbus.EventBus


open class LetsChatActivity : Application() {

    private lateinit var networkCallback: ConnectivityManager.NetworkCallback
    private lateinit var connectivityManager: ConnectivityManager


    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {

            override fun onActivityPaused(activity: Activity) {

            }

            override fun onActivityResumed(activity: Activity) {

            }

            override fun onActivityStarted(activity: Activity) {
            }

            override fun onActivityDestroyed(activity: Activity) {
                if (::networkCallback.isInitialized) {
                    try {
                        connectivityManager.unregisterNetworkCallback(networkCallback)
                    } catch (e: Exception) {
                        println("MyApplication.onActivityDestroyed:${e.message}")
                    }

                }
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
            }

            override fun onActivityStopped(activity: Activity) {

            }

            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                registerNetworkCallback()
            }


        })
    }


    //Detect network state changes api>=21
    fun registerNetworkCallback() {
        connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onLost(network: Network) {
                super.onLost(network)
                EventBus.getDefault()
                    .post(
                        ConnectionChangeEvent(
                            "Internet connection lost, Changes will be saved once connection is restored"
                        )
                    )
                println("MyApplication.onLost:")

            }



        }
        connectivityManager.registerNetworkCallback(
            NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET)
                .build(),
            networkCallback
        )


    }


}