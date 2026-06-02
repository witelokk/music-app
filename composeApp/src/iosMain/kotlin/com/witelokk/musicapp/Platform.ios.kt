package com.witelokk.musicapp

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Network.nw_path_is_constrained
import platform.Network.nw_path_is_expensive
import platform.Network.nw_path_monitor_create
import platform.Network.nw_path_monitor_set_queue
import platform.Network.nw_path_monitor_set_update_handler
import platform.Network.nw_path_monitor_start
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString
import platform.UIKit.UIDevice
import platform.darwin.dispatch_get_main_queue

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()

actual fun openAppLanguageSettings(): Boolean {
    val url = NSURL.URLWithString(UIApplicationOpenSettingsURLString) ?: return false
    val application = UIApplication.sharedApplication
    if (!application.canOpenURL(url)) return false

    application.openURL(url, emptyMap<Any?, Any>()) {}
    return true
}

actual fun isActiveNetworkMetered(): Boolean = IosNetworkCostState.isMetered()

actual fun supportsDynamicColors(): Boolean = false

@OptIn(ExperimentalForeignApi::class)
private object IosNetworkCostState {
    private var metered = true

    init {
        val monitor = nw_path_monitor_create()
        nw_path_monitor_set_update_handler(monitor) { path ->
            metered = nw_path_is_expensive(path) || nw_path_is_constrained(path)
        }
        nw_path_monitor_set_queue(monitor, dispatch_get_main_queue())
        nw_path_monitor_start(monitor)
    }

    fun isMetered(): Boolean = metered
}
