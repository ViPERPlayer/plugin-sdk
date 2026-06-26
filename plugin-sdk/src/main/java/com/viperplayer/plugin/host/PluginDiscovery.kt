package com.viperplayer.plugin.host

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.viperplayer.plugin.protocol.Protocol

/**
 * A plugin discovered on-device but not yet connected. [id] is the package name; richer detail
 * (capabilities, settings activity) only becomes known after the handshake.
 */
data class DiscoveredPlugin(
    val id: String,
    val name: String,
    val description: String?,
    val version: String,
    val component: ComponentName,
)

/** Finds installed plugins by their exported service + the well-known discovery action. */
object PluginDiscovery {

    fun discover(context: Context): List<DiscoveredPlugin> {
        val pm = context.packageManager
        val intent = Intent(Protocol.ACTION_PLUGIN)
        val services = pm.queryIntentServices(intent, PackageManager.GET_META_DATA)
        return services.mapNotNull { resolveInfo ->
            val serviceInfo = resolveInfo.serviceInfo ?: return@mapNotNull null
            val id = serviceInfo.packageName
            val metaData = serviceInfo.metaData ?: serviceInfo.applicationInfo?.metaData
            val name = metaData?.getString(Protocol.META_NAME)
                ?: serviceInfo.loadLabel(pm).toString()
            val description = metaData?.getString(Protocol.META_DESCRIPTION)
            val version = runCatching {
                pm.getPackageInfo(id, 0).versionName
            }.getOrNull() ?: "0"
            DiscoveredPlugin(
                id = id,
                name = name,
                description = description,
                version = version,
                component = ComponentName(serviceInfo.packageName, serviceInfo.name),
            )
        }
    }
}
