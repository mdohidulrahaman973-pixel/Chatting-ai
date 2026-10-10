package com.example.system

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class SystemStats(
    val batteryPct: Int = 100,
    val isCharging: Boolean = false,
    val ramUsedMb: Long = 0,
    val ramTotalMb: Long = 0,
    val networkType: String = "ONLINE",
    val currentTime: String = "",
    val currentDate: String = ""
)

class SystemDiagnosticsHelper(private val context: Context) {

    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
    private var cameraIdWithFlash: String? = null

    init {
        findFlashCamera()
    }

    private fun findFlashCamera() {
        try {
            cameraManager?.cameraIdList?.forEach { id ->
                val characteristics = cameraManager.getCameraCharacteristics(id)
                val hasFlash = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
                if (hasFlash && cameraIdWithFlash == null) {
                    cameraIdWithFlash = id
                }
            }
        } catch (_: Exception) {}
    }

    fun setTorchMode(enabled: Boolean): Boolean {
        return try {
            val id = cameraIdWithFlash ?: cameraManager?.cameraIdList?.firstOrNull() ?: return false
            cameraManager?.setTorchMode(id, enabled)
            true
        } catch (_: Exception) {
            false
        }
    }

    fun getSystemStats(): SystemStats {
        // Battery info
        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { filter ->
            context.registerReceiver(null, filter)
        }
        val level: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: 100
        val scale: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: 100
        val batteryPct = if (level >= 0 && scale > 0) ((level / scale.toFloat()) * 100).toInt() else 100
        val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

        // Memory info
        val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        actManager?.getMemoryInfo(memInfo)
        val ramTotalMb = (memInfo.totalMem / (1024 * 1024))
        val ramAvailMb = (memInfo.availMem / (1024 * 1024))
        val ramUsedMb = (ramTotalMb - ramAvailMb).coerceAtLeast(0)

        // Network info
        val connManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val activeNet = connManager?.activeNetwork
        val caps = connManager?.getNetworkCapabilities(activeNet)
        val networkType = when {
            caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> "WI-FI [TACTICAL LINK]"
            caps?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> "CELLULAR [5G/LTE LINK]"
            caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true -> "NET LINKED"
            else -> "STANDALONE [LOCAL CORE]"
        }

        // Time and date
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val dateFormat = SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault())
        val now = Date()

        return SystemStats(
            batteryPct = batteryPct,
            isCharging = isCharging,
            ramUsedMb = ramUsedMb,
            ramTotalMb = ramTotalMb,
            networkType = networkType,
            currentTime = timeFormat.format(now),
            currentDate = dateFormat.format(now).uppercase(Locale.getDefault())
        )
    }

    fun generateDiagnosticReportString(): String {
        val stats = getSystemStats()
        return "Tactical telemetry report: Core systems nominal. Power cell at ${stats.batteryPct}%${if (stats.isCharging) " (recharging)" else ""}. Neural memory allocation at ${stats.ramUsedMb}MB of ${stats.ramTotalMb}MB. Network connection is ${stats.networkType}. Current chronometer reads ${stats.currentTime}. Lyra online and waiting for your directive."
    }
}
