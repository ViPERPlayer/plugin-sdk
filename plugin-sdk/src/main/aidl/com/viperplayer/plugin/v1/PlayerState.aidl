package com.viperplayer.plugin.v1;

import com.viperplayer.plugin.v1.Song;

parcelable PlayerState {
    PlaybackState state = PlaybackState.IDLE;
    @nullable Song currentSong;
    long positionMs = 0;
    long durationMs = 0;
    boolean shuffleEnabled = false;
    RepeatMode repeatMode = RepeatMode.OFF;
    float volume = 1.0f;
    int queueSize = 0;
    int queuePosition = 0;

    enum PlaybackState {
        IDLE,
        BUFFERING,
        PLAYING,
        PAUSED,
        STOPPED,
        ERROR
    }

    enum RepeatMode {
        OFF,
        ONE,
        ALL
    }
}
