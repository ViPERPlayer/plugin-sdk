package com.viperplayer.plugin.v1;

import android.os.ParcelFileDescriptor;
import com.viperplayer.plugin.v1.AudioFormat;

parcelable AudioStream {
    String streamId;
    String mediaId;
    AudioFormat format;
    long durationMs;
    ParcelFileDescriptor pipe;
    boolean canSeek = true;
}
