// IHostCallbackV1.aidl - Callbacks from plugin to host (V1 API)
package com.viperplayer.plugin.v1;

import com.viperplayer.plugin.v1.Song;
import com.viperplayer.plugin.v1.PlayerState;

/**
 * Callback interface for plugins to communicate with the host app.
 * Plugins can use this to control playback, update metadata, etc.
 */
interface IHostCallbackV1 {
    // ==================== Player Control ====================
    
    /**
     * Request the host to play a specific song.
     * The host will call back to get the audio stream.
     */
    void play(in String id) = 1;
    
    /**
     * Request the host to pause playback.
     */
    void pause() = 2;
    
    /**
     * Request the host to resume playback.
     */
    void resume() = 3;
    
    /**
     * Request the host to stop playback.
     */
    void stop() = 4;
    
    /**
     * Request the host to skip to the next track in queue.
     */
    void skipToNext() = 5;
    
    /**
     * Request the host to skip to the previous track.
     */
    void skipToPrevious() = 6;
    
    /**
     * Request the host to seek to a position (in milliseconds).
     */
    void seekTo(long positionMs) = 7;
    
    // ==================== Queue Management ====================
    
    /**
     * Add a song to the end of the queue.
     */
    void addToQueue(in Song song) = 11;
    
    /**
     * Add a song to play next (after current track).
     */
    void playNext(in Song song) = 12;
    
    /**
     * Clear the entire queue.
     */
    void clearQueue() = 13;
    
    // ==================== State Queries ====================
    
    /**
     * Get the current player state.
     */
    PlayerState getPlayerState() = 21;
    
    /**
     * Get the currently playing song, or null if nothing is playing.
     */
    Song getCurrentSong() = 22;
    
    /**
     * Get the current playback position in milliseconds.
     */
    long getPlaybackPosition() = 23;
    
    // ==================== Plugin Notifications ====================
    
    /**
     * Notify the host that plugin content has changed.
     * Host should refresh any cached data from this plugin.
     */
    void notifyContentChanged() = 31;
    
    /**
     * Notify the host that plugin metadata has been updated.
     * Use this when song/album/artist details change.
     */
    void notifyMetadataUpdated(in String id) = 32;
    
    /**
     * Report an error to the host for display to the user.
     */
    void reportError(int errorCode, String message) = 33;
}
