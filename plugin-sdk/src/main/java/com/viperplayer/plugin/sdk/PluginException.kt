package com.viperplayer.plugin.sdk

/**
 * Base exception for plugin operations.
 */
open class PluginException(
    val errorCode: Int,
    override val message: String,
    override val cause: Throwable? = null
) : Exception(message, cause) {
    
    companion object {
        fun fromCode(code: Int, message: String = "", cause: Throwable? = null): PluginException {
            return when (code) {
                ErrorCodes.NETWORK_ERROR -> NetworkException(message, cause)
                ErrorCodes.AUTH_REQUIRED -> AuthRequiredException(message)
                ErrorCodes.AUTH_FAILED -> AuthFailedException(message, cause)
                ErrorCodes.NOT_FOUND -> NotFoundException(message)
                ErrorCodes.NOT_AVAILABLE -> NotAvailableException(message)
                ErrorCodes.RATE_LIMITED -> RateLimitedException(message)
                ErrorCodes.DECODE_ERROR -> DecodeException(message, cause)
                ErrorCodes.CANCELLED -> CancelledException(message)
                ErrorCodes.NOT_SUPPORTED -> NotSupportedException(message)
                else -> PluginException(code, message, cause)
            }
        }
    }
}

/** Network-related error */
class NetworkException(
    message: String = "Network error",
    cause: Throwable? = null
) : PluginException(ErrorCodes.NETWORK_ERROR, message, cause)

/** Authentication is required to proceed */
class AuthRequiredException(
    message: String = "Authentication required"
) : PluginException(ErrorCodes.AUTH_REQUIRED, message)

/** Authentication attempt failed */
class AuthFailedException(
    message: String = "Authentication failed",
    cause: Throwable? = null
) : PluginException(ErrorCodes.AUTH_FAILED, message, cause)

/** Requested content was not found */
class NotFoundException(
    message: String = "Content not found"
) : PluginException(ErrorCodes.NOT_FOUND, message)

/** Content not available (e.g., region restriction) */
class NotAvailableException(
    message: String = "Content not available"
) : PluginException(ErrorCodes.NOT_AVAILABLE, message)

/** Rate limited by the service */
class RateLimitedException(
    message: String = "Rate limited"
) : PluginException(ErrorCodes.RATE_LIMITED, message)

/** Audio decoding error */
class DecodeException(
    message: String = "Decode error",
    cause: Throwable? = null
) : PluginException(ErrorCodes.DECODE_ERROR, message, cause)

/** Operation was cancelled */
class CancelledException(
    message: String = "Operation cancelled"
) : PluginException(ErrorCodes.CANCELLED, message)

/** Feature not supported by this plugin */
class NotSupportedException(
    message: String = "Feature not supported"
) : PluginException(ErrorCodes.NOT_SUPPORTED, message)

