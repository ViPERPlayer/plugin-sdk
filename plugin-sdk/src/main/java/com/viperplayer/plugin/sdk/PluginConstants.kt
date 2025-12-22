package com.viperplayer.plugin.sdk

/**
 * Constants for plugin discovery and communication.
 */
object PluginConstants {
    /** Intent action that plugins must declare in their manifest */
    const val ACTION_PLUGIN_SERVICE = "com.viperplayer.plugin.PLUGIN_SERVICE"
    
    /** Permission that plugins must declare */
    const val PERMISSION_PLUGIN = "com.viperplayer.permission.PLUGIN"
    
    /** Metadata key for plugin API version */
    const val META_API_VERSION = "com.viperplayer.plugin.API_VERSION"
    
    /** Metadata key for plugin name */
    const val META_PLUGIN_NAME = "com.viperplayer.plugin.NAME"
    
    /** Metadata key for plugin description */
    const val META_PLUGIN_DESCRIPTION = "com.viperplayer.plugin.DESCRIPTION"
    
    /** Metadata key for plugin icon */
    const val META_PLUGIN_ICON = "com.viperplayer.plugin.ICON"
    
    /** Current SDK API version */
    const val CURRENT_API_VERSION = 1
    
    /** Minimum supported API version */
    const val MIN_API_VERSION = 1
}

/**
 * Error codes for plugin operations.
 */
object ErrorCodes {
    /** Operation completed successfully */
    const val SUCCESS = 0
    
    /** Unknown/generic error */
    const val UNKNOWN = -1
    
    /** Network error (no connection, timeout, etc.) */
    const val NETWORK_ERROR = 1
    
    /** Authentication required */
    const val AUTH_REQUIRED = 2
    
    /** Authentication failed */
    const val AUTH_FAILED = 3
    
    /** Content not found */
    const val NOT_FOUND = 4
    
    /** Content not available in region */
    const val NOT_AVAILABLE = 5
    
    /** Rate limited */
    const val RATE_LIMITED = 6
    
    /** Invalid request/parameters */
    const val INVALID_REQUEST = 7
    
    /** Plugin internal error */
    const val PLUGIN_ERROR = 8
    
    /** Playback error */
    const val PLAYBACK_ERROR = 9
    
    /** Decoding error */
    const val DECODE_ERROR = 10
    
    /** Operation cancelled */
    const val CANCELLED = 11
    
    /** Feature not supported */
    const val NOT_SUPPORTED = 12
}

