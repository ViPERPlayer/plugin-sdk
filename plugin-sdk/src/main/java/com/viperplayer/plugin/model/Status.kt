package com.viperplayer.plugin.model

import kotlinx.serialization.Serializable

/**
 * The kind of user action a plugin needs. Drives the host's iconography/copy. Unknown values from
 * newer plugins decode as [UNKNOWN] (the codec coerces onto the field default), so a new type never
 * breaks an older host.
 */
@Serializable
enum class ActionType { PERMISSION, LOGIN, VERIFICATION, SETUP, UNKNOWN }

/**
 * A manual step the user must take before (part of) a plugin works: grant a permission, sign in,
 * pass a verification challenge, finish setup...
 *
 * Resolution, in order of preference by the host:
 * 1. [permission] — when the plugin is embedded in the host (same package), the host requests the
 *    permission directly, no activity hop.
 * 2. [activity] — FQCN of an activity in the plugin's package that walks the user through the fix
 *    (login screen, verification page, its settings/permission UI).
 * 3. Neither → the host opens the plugin's settings activity.
 */
@Serializable
data class RequiredAction(
    /** Stable id, unique within the plugin (e.g. "audio-permission", "login"). */
    val id: String,
    val type: ActionType = ActionType.UNKNOWN,
    /** Short, user-facing imperative ("Sign in"). */
    val title: String,
    val description: String? = null,
    /** FQCN of the activity (in the plugin's package) that resolves this action. */
    val activity: String? = null,
    /** Android permission name, for hosts that can request it directly (embedded plugins). */
    val permission: String? = null,
)

/**
 * A plugin's current status, queried by the host at connect and whenever the plugin pushes
 * [com.viperplayer.plugin.protocol.HostVerbs.Event.STATUS_CHANGED]. Empty [actions] = nothing
 * pending, all good.
 */
@Serializable
data class PluginStatus(
    val actions: List<RequiredAction> = emptyList(),
)
