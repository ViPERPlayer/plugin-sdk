package com.viperplayer.plugin.v1;

import com.viperplayer.plugin.v1.Artist;
import com.viperplayer.plugin.v1.Album;
import com.viperplayer.plugin.v1.Artwork;

parcelable Song {
    String id;
    String title;
    List<Artist> artists;
    @nullable Album album;
    
    long durationMs = 0;
    boolean hasDurationMs = false;

    float replayGainDb = 0.0f;
    boolean hasReplayGainDb = false;

    float peakAmplitude = 0.0f;
    boolean hasPeakAmplitude = false;

    @nullable Artwork artwork;
    
    int trackNumber = 0;
    boolean hasTrackNumber = false;

    int discNumber = 0;
    boolean hasDiscNumber = false;

    boolean isExplicit = false;
    boolean isPlayable = true;
    boolean requiresInternet = true;
    
    int releaseYear = 0;
    boolean hasReleaseYear = false;

    List<String> genres;
}
