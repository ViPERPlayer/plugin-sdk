package com.viperplayer.plugin.protocol

/**
 * Stable, well-known constants that are part of the plugin contract but live *outside* the frozen
 * AIDL ABI: the discovery action, manifest meta-data keys, the protocol version, and the Bundle
 * keys used inside [com.viperplayer.plugin.ipc] transactions.
 *
 * These can grow over time (new keys, higher [VERSION]) without ever touching the AIDL files.
 */
object Protocol {
    /**
     * Wire protocol version, negotiated at handshake. Bumped only for changes the codec cannot
     * absorb on its own (it almost never needs to be). Host and plugin each report their version;
     * each speaks the subset the other understands.
     */
    const val VERSION = 1

    /** Codec identifier negotiated at handshake. v1 = tolerant JSON carried as UTF-8 bytes. */
    const val CODEC_JSON = "json/1"

    /** Intent action a plugin's service must export for the host to discover it. */
    const val ACTION_PLUGIN = "com.viperplayer.plugin.ViperPluginService"

    // ---- AndroidManifest <meta-data> keys (pre-connect, display-only hints) ----
    /** Optional human-readable name shown before the plugin is connected. */
    const val META_NAME = "com.viperplayer.plugin.NAME"

    /** Optional description shown before the plugin is connected. */
    const val META_DESCRIPTION = "com.viperplayer.plugin.DESCRIPTION"

    /**
     * Optional HTTPS URL of the plugin's update feed — a small JSON manifest the host fetches to
     * learn the latest available version and where to download it (see the host's update system).
     * A plugin that omits this key opts out of in-app updates and is simply skipped by the checker.
     */
    const val META_UPDATE_URL = "com.viperplayer.plugin.UPDATE_URL"

    // ---- Bundle keys carried inside invoke()/result transactions ----
    /** Codec-encoded request/response payload (`ByteArray`). */
    const val KEY_PAYLOAD = "viper.payload"

    /** A live, single-use resource for this request (`ParcelFileDescriptor`), e.g. a PCM pipe. */
    const val KEY_FD = "viper.fd"

    /** A second live FD where an op needs two (e.g. DSP input + output). */
    const val KEY_FD2 = "viper.fd2"

    /** Shared-memory region (`android.os.SharedMemory`, API 27+), e.g. a DSP ring buffer. */
    const val KEY_SHM = "viper.shm"

    /** Secondary shared-memory region. */
    const val KEY_SHM2 = "viper.shm2"

    /** A live `IBinder` token for this request (e.g. a streaming control channel). */
    const val KEY_TOKEN = "viper.token"
}
