package com.viperplayer.plugin.host

import android.os.ParcelFileDescriptor
import com.viperplayer.plugin.model.StreamSource

/**
 * What the host gets back from [SourceClient.resolveStream]: the [StreamSource] metadata plus, for
 * a PCM stream, the live read FD to pull audio from. The host is responsible for closing [fd].
 */
class ResolvedStream(
    val source: StreamSource,
    val fd: ParcelFileDescriptor?,
)
