package com.viperplayer.plugin

/**
 * Constants for plugin discovery and communication.
 */
object PluginConstants {
    /** Intent action that plugins must declare in their manifest */
    const val ACTION_PLUGIN_SERVICE = "com.viperplayer.plugin.ViperPluginService"
    
    /** Metadata key for plugin name */
    const val META_PLUGIN_NAME = "com.viperplayer.plugin.NAME"
    
    /** Metadata key for plugin description */
    const val META_PLUGIN_DESCRIPTION = "com.viperplayer.plugin.DESCRIPTION"
}

/**
 * Error codes for plugin operations.
 */
object ErrorCodes {
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

