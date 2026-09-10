# DOOT — Prototype Build Spec (PRD + HLD + LLD)
**Target: 24-hour prototype build, for hand-off to an AI coding agent (Antigravity)**
**Scope: exactly Phase 1 of the sprint plan — English + Hindi + Punjabi, single pair of phones, Wi-Fi Direct**

---

# PART A — PRD (Product Requirements Document)

## A.1 Product one-liner
DOOT is a fully offline, on-device speech-to-speech relay: Phone A captures speech, converts it to text locally, sends the text over a direct Wi-Fi Direct socket to Phone B, which converts it back to speech and plays it — no internet, no cloud API, no pairing infrastructure beyond the two phones themselves.

## A.2 Prototype goal (what "done" looks like tomorrow)
Two Android phones, DOOT installed on both. Judge presses record on Phone A, speaks a sentence in Hindi, Punjabi, or English. Within a few seconds, Phone B speaks the same sentence back out loud in the same language, and both phones show the transcribed text on screen. A separate "Alert" button sends a fixed emergency phrase that plays at max volume on the receiving phone regardless of its current volume/silent setting.

## A.3 In-scope user stories (prototype only)

| # | Story | Acceptance criteria |
|---|---|---|
| U1 | As a user, I open the app and see a "Connect" screen to pair with the other phone | Tapping Connect starts Wi-Fi Direct discovery; once both phones tap Connect, they show "Connected" state |
| U2 | As a user, I press and hold a Talk button, speak, and release | Audio is captured while held (min 1s, max 5s buffer for prototype); on release, STT runs and shows recognized text on my own screen |
| U3 | As a user, my speech is sent to the other phone as text | Other phone receives the text within ~1s of my STT completing |
| U4 | As a user, I hear the other phone's message spoken aloud | Received text is converted to speech and played automatically, no action needed on receiving phone |
| U5 | As a user, I can select English, Hindi, or Punjabi before speaking | A language toggle switches which STT/TTS model is used for the next utterance |
| U6 | As a user, I can send a canned "Alert" that overrides volume | Pressing an Alert button sends a fixed high-priority string; receiving phone plays it at max volume even if phone is on silent/low volume |
| U7 | As a user, if the app has no active connection, it still works standalone | If PTT is used with no peer connected, STT still runs and shows text locally (graceful degradation, judge can test STT alone) |

## A.4 Explicitly OUT of scope for tomorrow (do not build tonight)
- Real streaming/incremental STT (batch-on-release is fine)
- Real VAD-based auto pause detection (fixed max-duration buffer + manual release is the prototype fallback; VAD is Hour 21-24 stretch, not blocking)
- More than 3 languages
- Dynamic/remote model download (models are bundled in the APK via `assets/`)
- Reconnect-on-drop / background service / screen-off resilience (Week 1 of finals sprint)
- Multi-peer (>2 phones)
- Auth, accounts, chat history persistence

## A.5 Non-functional requirements (prototype-level, not final judging-level)
- App must not crash on a mid/low-end device (target: any phone with ≥4GB RAM, Android 8+/API 26+)
- End-to-end latency (speech end → audio starts on other phone) should be visibly under ~5s for the demo to read as "real-time enough" — not optimized, just not embarrassing
- App must request and gracefully handle: `RECORD_AUDIO`, `ACCESS_FINE_LOCATION` (required by WifiP2pManager on API <33), `NEARBY_WIFI_DEVICES` (API 33+), `ACCESS_WIFI_STATE`, `CHANGE_WIFI_STATE`

## A.6 Demo script the app must support
1. Both phones open app → tap Connect → show "Connected to <peer>"
2. Phone A: select Hindi → hold Talk → say a sentence → release → text appears on A → few seconds later, Phone B speaks it in Hindi + shows text
3. Repeat in English, then repeat in Punjabi
4. Phone A: tap Alert → Phone B plays alert tone/message at max volume even with its ringer set to silent

---

# PART B — HLD (High-Level Design)

## B.1 System context

```
 ┌────────────────────────┐   Wi-Fi Direct (P2P, no router/internet)   ┌────────────────────────┐
 │        PHONE A         │ <-----------------------------------------> │        PHONE B         │
 │   (DOOT app instance)  │              TCP socket, port 8988          │   (DOOT app instance)  │
 └────────────────────────┘                                             └────────────────────────┘
```
Both phones run the identical APK. There is no client/server distinction in the product sense — whichever phone forms the Wi-Fi Direct "group owner" during connection hosts the TCP server socket; the app treats both directions symmetrically (either phone can talk, either can listen).

## B.2 Component breakdown

| Component | Responsibility | Key library |
|---|---|---|
| **ConnectionManager** | Wi-Fi Direct discovery, peer connection, group-owner negotiation, exposes connection state | `android.net.wifi.p2p.WifiP2pManager` |
| **SocketTransport** | Owns the TCP socket (server if group owner, client if not); sends/receives length-prefixed JSON packets | `java.net.Socket` / `ServerSocket` over Kotlin coroutines |
| **AudioCapture** | Records mic audio into a 16kHz mono PCM16 buffer while Talk is held | `android.media.AudioRecord` |
| **SttEngine** | Wraps sherpa-onnx offline recognizer; takes PCM16 float samples, returns recognized text + language tag | `sherpa-onnx` (`OfflineRecognizer`) |
| **TtsEngine** | Wraps sherpa-onnx offline TTS; takes text + language, returns PCM audio, plays it | `sherpa-onnx` (`OfflineTts`) |
| **AlertPlayer** | Forces max-volume, focus-exclusive playback for alert messages | `AudioManager` + `AudioFocusRequest` |
| **AppViewModel** | Orchestrates the pipeline (capture → STT → send → receive → TTS), holds UI state | Kotlin `ViewModel` + `StateFlow` |
| **UI (Compose)** | Connect screen, main Talk screen, language toggle, Alert button, transcript display | Jetpack Compose |

## B.3 End-to-end data flow (happy path)

```
[Phone A]
Hold Talk
   → AudioCapture.start()              (records PCM16 @16kHz while held, max 5s)
Release Talk
   → AudioCapture.stop() → FloatArray samples
   → SttEngine.recognize(samples, lang) → String text        [~0.5-2s on-device]
   → UI shows text on Phone A
   → SocketTransport.send(Packet(type=SPEECH, lang, text))
        ↓ over Wi-Fi Direct TCP socket
[Phone B]
   → SocketTransport receives Packet
   → AppViewModel routes to TtsEngine.synthesize(text, lang) → PCM audio  [~0.3-1s]
   → AudioTrack plays audio
   → UI shows received text on Phone B
```

Alert path is identical but `type=ALERT`, and playback goes through `AlertPlayer` (forces `STREAM_ALARM` + exclusive audio focus + volume override) instead of normal playback.

## B.4 Wire protocol (Phone A ↔ Phone B)

Simple length-prefixed JSON over the TCP socket (keeps it debuggable and trivial for an agent to implement correctly under time pressure — no need for protobuf tonight).

```
Packet framing: [4-byte big-endian Int32 length][UTF-8 JSON payload of that length]

JSON payload schema:
{
  "type": "SPEECH" | "ALERT" | "PING",
  "lang": "hi" | "en" | "pa",
  "text": "string",
  "timestamp": 1234567890
}
```

## B.5 Tech stack (prototype-locked versions)

| Layer | Choice |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose (Material 3) |
| Min SDK | 26 (Android 8.0) — Wi-Fi Direct + AudioRecord baseline |
| Target SDK | 34 |
| STT/TTS runtime | `com.k2fsa.sherpa.onnx:sherpa-onnx-android` (Maven Central) |
| STT model | AI4Bharat IndicConformer ONNX (or bundled sherpa-onnx multilingual offline model if IndicConformer conversion isn't ready in time — see fallback in B.6) |
| TTS model | AI4Bharat Indic-TTS/VITS ONNX for Hindi and Punjabi; any sherpa-onnx VITS English model for English |
| Concurrency | Kotlin Coroutines + `StateFlow` |
| Networking | Raw `java.net.Socket`/`ServerSocket` (no extra library) |
| P2P transport | `android.net.wifi.p2p.WifiP2pManager` (Wi-Fi Direct) |

## B.6 Fallback plan if AI4Bharat model conversion isn't ready by tonight
Do **not** let model conversion block the pipeline. sherpa-onnx ships pre-converted, ready-to-download multilingual/English/Hindi offline ASR and TTS models on Hugging Face specifically for plug-and-play testing — Punjabi is the language most likely to need the AI4Bharat-specific model rather than a stock sherpa-onnx one, so budget extra time for that conversion/testing. **Bundle those first**, get the full pipeline working end-to-end, and swap in the AI4Bharat-specific ONNX files as a drop-in replacement once ready — the `SttEngine`/`TtsEngine` wrapper classes should take model file paths as constructor parameters precisely so this swap requires zero code changes, only asset file changes.

---

# PART C — LLD (Low-Level Design)

## C.1 Project/package structure

```
app/
 └── src/main/
     ├── java/com/doot/app/
     │   ├── DootApplication.kt
     │   ├── MainActivity.kt
     │   ├── connection/
     │   │   ├── ConnectionManager.kt
     │   │   ├── ConnectionState.kt          (sealed class: Idle, Discovering, Connecting, Connected, Failed)
     │   │   └── SocketTransport.kt
     │   ├── audio/
     │   │   ├── AudioCapture.kt
     │   │   └── AlertPlayer.kt
     │   ├── speech/
     │   │   ├── SttEngine.kt
     │   │   ├── TtsEngine.kt
     │   │   └── ModelAssets.kt              (paths/constants for bundled model files)
     │   ├── protocol/
     │   │   └── Packet.kt                   (data class + kotlinx.serialization)
     │   ├── ui/
     │   │   ├── ConnectScreen.kt
     │   │   ├── TalkScreen.kt
     │   │   └── theme/
     │   └── viewmodel/
     │       └── AppViewModel.kt
     └── assets/
         ├── models/en/  (sherpa-onnx English offline ASR + TTS ONNX files + tokens.txt)
         ├── models/hi/  (sherpa-onnx/AI4Bharat Hindi offline ASR + TTS ONNX files + tokens.txt)
         └── models/pa/  (AI4Bharat Punjabi offline ASR + TTS ONNX files + tokens.txt)
```

## C.2 Gradle dependencies (add to `app/build.gradle.kts`)

```kotlin
dependencies {
    implementation("com.k2fsa.sherpa.onnx:sherpa-onnx-android:<latest>") // check Maven Central for current version tag
    implementation("androidx.compose.material3:material3:<latest>")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:<latest>")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
}
```
> Agent note: pin exact versions by checking Maven Central at build time — do not hardcode a version that may be stale.

## C.3 AndroidManifest.xml — required entries

```xml
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
<uses-permission android:name="android.permission.CHANGE_WIFI_STATE" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.NEARBY_WIFI_DEVICES"
    android:usesPermissionFlags="neverForLocation"
    tools:targetApi="33" />
<uses-permission android:name="android.permission.INTERNET" /> <!-- for local socket only, no external calls made -->
<uses-feature android:name="android.hardware.wifi.direct" android:required="true" />
```
All of the above must be requested at runtime (not just declared) before `ConnectionManager.startDiscovery()` or `AudioCapture.start()` is called. Show a single combined permission-rationale screen before Connect screen.

## C.4 State machine — PTT / App state

```
sealed class AppState {
    object Disconnected
    object Connected
    object Recording          // Talk held, AudioCapture active
    object Transcribing       // STT running on captured buffer
    object Sending            // packet in flight
    object Receiving          // packet received, TTS running
    object Speaking           // TtsEngine playing audio
}
```
Transitions:
`Disconnected --(peer connects)--> Connected`
`Connected --(Talk pressed)--> Recording --(Talk released)--> Transcribing --(text ready)--> Sending --(ack/send complete)--> Connected`
`Connected --(packet received)--> Receiving --(TTS ready)--> Speaking --(playback done)--> Connected`
Recording and Receiving/Speaking are mutually exclusive on the same device only if you want strict half-duplex; for the prototype, **allow both directions concurrently** (simpler — don't build a turn-taking lock tonight).

## C.5 Class specs

### `ConnectionManager.kt`
```kotlin
class ConnectionManager(context: Context) {
    val state: StateFlow<ConnectionState>
    fun startDiscoveryAndConnect()      // handles WifiP2pManager.discoverPeers + requestConnect on first peer found
    fun getGroupInfo(): WifiP2pInfo?    // exposes isGroupOwner + groupOwnerAddress for SocketTransport
    fun disconnect()
}
```
Implementation notes for the agent:
- Register a `BroadcastReceiver` for `WIFI_P2P_CONNECTION_CHANGED_ACTION`, `WIFI_P2P_PEERS_CHANGED_ACTION`.
- For the prototype: auto-connect to the **first peer discovered** (no peer-picker UI needed — two-phone demo only).
- Once `WifiP2pInfo.groupFormed == true`: if `isGroupOwner`, start `ServerSocket(8988)`; else connect `Socket(groupOwnerAddress, 8988)`. Pass this to `SocketTransport.attach(socket)`.

### `SocketTransport.kt`
```kotlin
class SocketTransport {
    fun attach(socket: Socket)
    suspend fun send(packet: Packet)                 // writes 4-byte length + JSON bytes
    val incoming: Flow<Packet>                        // cold flow reading loop on IO dispatcher
    fun close()
}
```
Implementation notes:
- Reader loop runs in a `withContext(Dispatchers.IO)` coroutine, reads 4 bytes → length → that many bytes → parses JSON → emits into a `MutableSharedFlow<Packet>`.
- Writer is a `Mutex`-guarded suspend function so sends don't interleave.

### `AudioCapture.kt`
```kotlin
class AudioCapture {
    fun start()                    // begins AudioRecord capture, 16000Hz, MONO, PCM_16BIT
    fun stop(): FloatArray         // stops, returns normalized float samples [-1,1] for sherpa-onnx
    val isRecording: Boolean
}
```
Implementation notes:
- Buffer size: `AudioRecord.getMinBufferSize(16000, CHANNEL_IN_MONO, ENCODING_PCM_16BIT)`, cap total capture at 5s (hard stop via `Handler.postDelayed` if Talk held too long, so a stuck button can't OOM).
- Convert `ShortArray` PCM16 → `FloatArray` by dividing by `32768f` (sherpa-onnx expects float samples).

### `SttEngine.kt`
```kotlin
class SttEngine(context: Context) {
    fun loadModel(lang: String)                  // lang = "en" | "hi" | "pa", loads from assets/models/<lang>/
    suspend fun recognize(samples: FloatArray): String   // runs on Dispatchers.Default
    fun release()
}
```
Implementation notes:
- Wraps `sherpa-onnx`'s `OfflineRecognizer` (offline/batch mode, not streaming, for the prototype — matches Phase 1 scope).
- Model files copied from `assets/` to app's internal files dir on first launch (sherpa-onnx JNI needs filesystem paths, not raw asset streams) — do this once in `DootApplication.onCreate()`, show a splash/loading state while copying.
- `loadModel(lang)` swaps which `OfflineRecognizer` instance is active; keep all three (English, Hindi, Punjabi) recognizers loaded simultaneously if RAM allows (simpler than reload-per-switch) — fall back to lazy load/unload only if memory profiling shows a problem (3 models in RAM at once is the most likely place a low-end device struggles, so profile this early, not on demo day).

### `TtsEngine.kt`
```kotlin
class TtsEngine(context: Context) {
    fun loadModel(lang: String)
    suspend fun synthesize(text: String, lang: String): ShortArray   // returns PCM16 for AudioTrack
    fun release()
}
```
Implementation notes:
- Wraps sherpa-onnx `OfflineTts`. Output sample rate comes from the model config (typically 22050Hz for VITS-class models) — read it from the loaded model, don't hardcode.
- Playback via a plain `AudioTrack` in streaming mode for normal messages.

### `AlertPlayer.kt`
```kotlin
class AlertPlayer(context: Context) {
    fun playAlert(pcm: ShortArray, sampleRate: Int)
}
```
Implementation notes:
- Request `AudioFocusRequest` with `AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE`.
- Route playback through `AudioAttributes` with `USAGE_ALARM` / `STREAM_ALARM`, and explicitly set `AudioManager.setStreamVolume(STREAM_ALARM, maxVolume, 0)` before playing, restoring prior volume after playback completes.
- This is the "non-interruptible, max volume" requirement from the problem statement — implement exactly this, nothing fancier needed for prototype.

### `Packet.kt`
```kotlin
@Serializable
data class Packet(
    val type: String,      // "SPEECH" | "ALERT" | "PING"
    val lang: String,      // "en" | "hi" | "pa"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)
```

### `AppViewModel.kt`
Owns instances of all the above, exposes:
```kotlin
val appState: StateFlow<AppState>
val myTranscript: StateFlow<String>
val receivedTranscript: StateFlow<String>
val selectedLang: StateFlow<String>

fun onConnectClicked()
fun onTalkPressed()
fun onTalkReleased()      // triggers capture.stop() -> stt.recognize() -> transport.send()
fun onAlertClicked()
fun onLangToggle(lang: String)
```
Wires `transport.incoming` flow (collected in `viewModelScope`) to trigger TTS playback whenever a `Packet` arrives.

## C.6 Sequence diagram — one full utterance (text form)

```
User(A) -> UI(A): press Talk
UI(A) -> AudioCapture(A): start()
User(A) -> UI(A): release Talk
UI(A) -> AudioCapture(A): stop() -> samples
UI(A) -> SttEngine(A): recognize(samples) -> "namaste, sab theek hai"
UI(A) -> UI(A): display text
UI(A) -> SocketTransport(A): send(Packet(SPEECH, "hi", "namaste, sab theek hai"))
SocketTransport(A) -> SocketTransport(B): [socket write/read]
SocketTransport(B) -> AppViewModel(B): incoming Packet
AppViewModel(B) -> TtsEngine(B): synthesize("namaste, sab theek hai", "hi") -> pcm
AppViewModel(B) -> AudioTrack(B): play(pcm)
UI(B) -> UI(B): display received text
```

## C.7 Edge cases the agent must handle (minimum, for demo safety)
- Talk pressed with no peer connected → still run STT, show text, skip the send silently (no crash)
- STT returns empty string (silence/noise) → don't send an empty packet, show "Didn't catch that" on screen
- Socket disconnects mid-demo → catch `IOException` in the read/write loop, set `ConnectionState.Failed`, show a "Reconnect" button (manual retry is enough for prototype — no auto-reconnect required tonight)
- Model files missing/corrupt in assets → fail fast on app launch with a clear error screen, not a silent crash mid-demo

## C.8 Build order (maps to the 24-hour plan, now as concrete implementation checkpoints)

1. `ModelAssets.kt` + asset-copy-on-launch + `SttEngine` wired to a hardcoded WAV file (no mic yet) → confirms sherpa-onnx integration works at all before touching audio/network
2. `AudioCapture` + wire into `SttEngine` with a manual "Record 5s" button, print text to screen → confirms mic pipeline
3. `ConnectionManager` + `SocketTransport`, hardcoded "Hello" string button → confirms two phones can talk over Wi-Fi Direct
4. Wire real STT output into `SocketTransport.send`, wire `incoming` flow to just display text on Phone B (no TTS yet) → confirms full text relay
5. Add `TtsEngine`, wire receiving flow to speak instead of just display → full loop closed
6. Add `AlertPlayer` + Alert button → done, rehearse

---
**Handoff note for Antigravity:** build in exactly this order (C.8). Each numbered step should be independently runnable and demoable — if time runs out, stop at the latest fully-working step rather than leaving step 6 half-wired and steps 1-5 broken.
