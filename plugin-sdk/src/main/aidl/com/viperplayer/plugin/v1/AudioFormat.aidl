package com.viperplayer.plugin.v1;

parcelable AudioFormat {
    int sampleRate = 44100;
    int channelCount = 2;
    PcmEncoding encoding = PcmEncoding.PCM_16BIT;
    int bitDepth = 16;

    enum PcmEncoding {
        PCM_16BIT,
        PCM_24BIT,
        PCM_FLOAT
    }
}
