# ViPER Player Plugin SDK

Plugins are separate APKs the host binds to over AIDL. A plugin can be a **music source**
(catalog + streams), an **audio DSP** effect, a **lyrics** provider, a **scrobble** sink, a
**metadata** enricher — or several at once. The host owns the player; plugins feed it content,
process its audio, and observe its state.

## The core guarantee

**A plugin built against an old SDK keeps working on a newer host, and a new plugin degrades
gracefully on an older host.** This is achieved by *freezing the wire ABI* and moving all real
semantics off it:

- The AIDL surface (`com.viperplayer.plugin.ipc`) is **frozen**: four methods on `IViperPlugin`
  (`initialize`, `invoke`, `cancel`, `shutdown`), plus tiny result/host callbacks. Its transaction
  codes never change, so binding always succeeds and no transaction is ever "unknown".
- Every operation rides `invoke(verb, requestId, args, callback)`, dispatched by a **string verb**
  (`com.viperplayer.plugin.protocol.Verbs`), gated by **capabilities** negotiated at the handshake.
- Payloads are **self-describing** and decoded tolerantly (`com.viperplayer.plugin.protocol.Codec`,
  JSON-over-bytes): unknown fields are ignored, missing fields default. Structured data travels as
  a `ByteArray` inside the args `Bundle`; live resources (PCM pipe FDs, DSP shared memory, tokens)
  ride the `Bundle` natively.

Adding a feature later = a new verb + new payload fields + a new capability flag. The `.aidl` files
never change. An old plugin simply doesn't advertise the new capability, so the host never sends it
that verb; a new plugin's extra payload fields are ignored by an old host.

### Staying under the 1 MB binder limit

A `Bundle` carries verbs, small fields, and *handles* — never bulk data:

- **Lists** page via a cursor (`Page<T>` / `PageRequest`).
- **URL/DASH/HLS** streams pass a string; the host's player fetches the media itself.
- **PCM** streams flow through a `ParcelFileDescriptor` pipe (`AudioStreamWriter`).
- **DSP** audio flows through `android.os.SharedMemory` regions with a socketpair doorbell.
- **Artwork** is a URL the host's image pipeline resolves.

## Writing a source plugin

```kotlin
class MyService : ViperPluginService() {
    override fun onCreatePlugin(context: Context) = pluginRegistration(
        id = "com.example.mymusic", name = "My Music", version = "1.0",
    ) {
        source(MySource(context), SourceCapabilities(search = true, home = true))
        // lyrics(...), scrobble(...), metadata(...), dsp(...) as needed
        // settingsActivity(MySettingsActivity::class.java.name)
        // onConnect { host -> /* observe player, emit events */ }
    }
}

class MySource(context: Context) : SourceProvider {
    override suspend fun search(request: SearchRequest): SearchResult { /* ... */ }
    override suspend fun getSong(id: String): Song { /* ... */ }
    override suspend fun getAlbum(id: String): Album { /* ... */ }
    override suspend fun getArtist(id: String): Artist { /* ... */ }
    override suspend fun getPlaylist(id: String): Playlist { /* ... */ }
    override suspend fun resolveStream(songId: String): StreamResponse =
        StreamResponse.url("https://.../$songId.mp3")
    // override only what you support; the rest keep safe defaults
}
```

Manifest:

```xml
<service android:name=".MyService" android:exported="true">
    <intent-filter>
        <action android:name="com.viperplayer.plugin.ViperPluginService" />
    </intent-filter>
</service>
```

You write plain `suspend` functions over typed models with real Kotlin nullability — the SDK owns
all IPC: handshake, verb dispatch, (de)serialization, paging, cancellation, and error mapping.
Throw `PluginException(PluginErrorCode.NOT_FOUND, ...)`; any other exception maps to `INTERNAL`.

## Writing a DSP plugin

```kotlin
class MyDspService : ViperPluginService() {
    override fun onCreatePlugin(context: Context) = pluginRegistration(
        id = "com.example.mydsp", name = "My DSP", version = "1.0",
    ) { dsp(MyDspProvider()) }
}

class MyDspProvider : DspProvider {
    override val descriptors = listOf(DspDescriptor("eq", "Equalizer", params = /* ... */))
    override fun createSession(format: DspAudioFormat, maxFramesPerBlock: Int) = object : DspSession() {
        override fun process(buffer: AudioBuffer) { /* transform buffer.samples in place */ }
        override fun configure(params: Map<String, Float>, enabled: Boolean) { /* ... */ }
    }
}
```

DSP runs out-of-process via shared memory: the host buffers ahead and pushes blocks, so a slow or
crashing effect can't break host audio. It is therefore **not** in the real-time path — favour
throughput. Requires Android 8.1+ (shared memory).

## On the host

```kotlin
val connection = PluginConnection.connect(pluginId, binder, hostBridge, scope, hostVersion)
val page = connection.client.source?.search(SearchRequest("daft punk"))
val resolved = connection.client.source?.resolveStream(songId) // StreamSource (+ FD for PCM)
```

`PluginDiscovery.discover(context)` finds installed plugins; `PluginConnection` performs the
handshake and exposes a capability-gated typed `PluginClient`.

## Layout

| Package | Role |
|---|---|
| `com.viperplayer.plugin.ipc` | **Frozen** AIDL transport — never change |
| `com.viperplayer.plugin.protocol` | Verbs, capabilities, codec, Bundle envelope (evolvable) |
| `com.viperplayer.plugin.model` | Serializable data models (real nullables) |
| `com.viperplayer.plugin.author` | Author SDK: `ViperPluginService`, providers |
| `com.viperplayer.plugin.host` | Host SDK: `PluginConnection`, `PluginClient` |
