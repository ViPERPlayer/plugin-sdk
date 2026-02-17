package com.viperplayer.plugin.v1;

import com.viperplayer.plugin.v1.AudioStream;

parcelable StreamSource {
    Type type;
    @nullable String url;
    @nullable String dashXml;
    @nullable AudioStream audioStream;

    // Nullable floats represented by float value + boolean flag
    float replayGainDb = 0.0f; 
    boolean hasReplayGainDb = false;

    float peakAmplitude = 0.0f; 
    boolean hasPeakAmplitude = false;

    enum Type {
        URL,
        DASH,
        AUDIO_STREAM
    }
}
