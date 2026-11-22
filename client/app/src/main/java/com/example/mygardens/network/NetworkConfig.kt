package com.example.mygardens.network

import android.os.Build
import com.example.mygardens.BuildConfig

/**
 * Helper object providing the dynamic base URL depending on whether the app is running on
 * an Android Emulator or a Physical Android Device.
 */
object NetworkConfig {
    val baseUrl: String
        get() {
            val configUrl = BuildConfig.API_BASE_URL
            if (configUrl.isNotBlank()) {
                return if (configUrl.endsWith("/")) configUrl else "$configUrl/"
            }

            // Check if running on Android Emulator vs Physical Device
            val isEmulator = (Build.FINGERPRINT.startsWith("generic")
                    || Build.FINGERPRINT.startsWith("unknown")
                    || Build.MODEL.contains("google_sdk")
                    || Build.MODEL.contains("Emulator")
                    || Build.MODEL.contains("Android SDK built for x86")
                    || Build.MANUFACTURER.contains("Genymotion")
                    || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                    || "google_sdk" == Build.PRODUCT)

            if (isEmulator) return "http://10.0.2.2:8080/"

            error("Set gardenersApiBaseUrl in local.properties to http://<computer-LAN-IP>:8080/ for a physical device.")
        }
}
