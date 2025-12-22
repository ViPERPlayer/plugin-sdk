// IHostCallbackV1.aidl - Callbacks from plugin to host (V1 API)
package com.viperplayer.plugin.aidl;

import com.viperplayer.plugin.aidl.MediaId;
import com.viperplayer.plugin.aidl.Song;
import com.viperplayer.plugin.aidl.PlayerState;

/**
 * Callback interface for plugins to communicate with the host app.
 * Plugins can use this to control playback, update metadata, etc.
 * 
 * API Version: 1
 */
interface IHostCallbackV1 {
    /**
     * Returns the current API version supported by the host.
     */
    int getHostApiVersion();
    
    // ==================== Player Control ====================
    
    /**
     * Request the host to play a specific song.
     * The host will call back to get the audio stream.
     */
    void play(in MediaId mediaId);
    
    /**
     * Request the host to pause playback.
     */
    void pause();
    
    /**
     * Request the host to resume playback.
     */
    void resume();
    
    /**
     * Request the host to stop playback.
     */
    void stop();
    
    /**
     * Request the host to skip to the next track in queue.
     */
    void skipToNext();
    
    /**
     * Request the host to skip to the previous track.
     */
    void skipToPrevious();
    
    /**
     * Request the host to seek to a position (in milliseconds).
     */
    void seekTo(long positionMs);
    
    // ==================== Queue Management ====================
    
    /**
     * Add a song to the end of the queue.
     */
    void addToQueue(in Song song);
    
    /**
     * Add a song to play next (after current track).
     */
    void playNext(in Song song);
    
    /**
     * Clear the entire queue.
     */
    void clearQueue();
    
    // ==================== State Queries ====================
    
    /**
     * Get the current player state.
     */
    PlayerState getPlayerState();
    
    /**
     * Get the currently playing song, or null if nothing is playing.
     */
    Song getCurrentSong();
    
    /**
     * Get the current playback position in milliseconds.
     */
    long getPlaybackPosition();
    
    // ==================== Plugin Notifications ====================
    
    /**
     * Notify the host that plugin content has changed.
     * Host should refresh any cached data from this plugin.
     */
    void notifyContentChanged();
    
    /**
     * Notify the host that plugin metadata has been updated.
     * Use this when song/album/artist details change.
     */
    void notifyMetadataUpdated(in MediaId mediaId);
    
    /**
     * Report an error to the host for display to the user.
     */
    void reportError(int errorCode, String message);
}

