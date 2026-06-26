package com.viperplayer.plugin.model

import kotlinx.serialization.Serializable

/**
 * Stable error codes carried by `onError(code, ...)` / `onInitializationFailed(code, ...)`. These
 * are part of the contract and append-only — never renumber an existing one.
 */
object PluginErrorCode {
    const val UNKNOWN = 0
    const val NETWORK = 1
    const val AUTH_REQUIRED = 2
    const val AUTH_FAILED = 3
    const val NOT_FOUND = 4
    const val NOT_AVAILABLE = 5
    const val RATE_LIMITED = 6
    const val INVALID_REQUEST = 7
    const val INTERNAL = 8
    const val PLAYBACK = 9
    const val DECODE = 10
    const val CANCELLED = 11

    /** The plugin does not support this verb/capability (the host should degrade gracefully). */
    const val UNSUPPORTED = 12

    /** The plugin requires a newer host than the one that connected. */
    const val INCOMPATIBLE_HOST = 13
}

/** Optional structured error body carried in the `onError` extras Bundle. */
@Serializable
data class PluginErrorBody(
    val code: Int = PluginErrorCode.UNKNOWN,
    val message: String? = null,
    val retryable: Boolean = false,
)

/**
 * Exception type shared by both sides. Authors throw it (or any exception — the SDK maps unknown
 * ones to [PluginErrorCode.INTERNAL]); the host SDK re-raises it from a failed call so callers see
 * a typed code instead of a bare string.
 */
class PluginException(
    val code: Int,
    message: String? = null,
    val retryable: Boolean = false,
    cause: Throwable? = null,
) : Exception(message ?: defaultMessage(code), cause) {

    fun toBody(): PluginErrorBody = PluginErrorBody(code, message, retryable)

    companion object {
        fun defaultMessage(code: Int): String = when (code) {
            PluginErrorCode.NETWORK -> "Network error"
            PluginErrorCode.AUTH_REQUIRED -> "Authentication required"
            PluginErrorCode.AUTH_FAILED -> "Authentication failed"
            PluginErrorCode.NOT_FOUND -> "Not found"
            PluginErrorCode.NOT_AVAILABLE -> "Not available"
            PluginErrorCode.RATE_LIMITED -> "Rate limited"
            PluginErrorCode.INVALID_REQUEST -> "Invalid request"
            PluginErrorCode.INTERNAL -> "Internal plugin error"
            PluginErrorCode.PLAYBACK -> "Playback error"
            PluginErrorCode.DECODE -> "Decode error"
            PluginErrorCode.CANCELLED -> "Cancelled"
            PluginErrorCode.UNSUPPORTED -> "Operation not supported"
            PluginErrorCode.INCOMPATIBLE_HOST -> "Incompatible host version"
            else -> "Unknown error"
        }

        fun from(body: PluginErrorBody): PluginException =
            PluginException(body.code, body.message, body.retryable)
    }
}
