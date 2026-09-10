# DOOT — Design System & Screen Specs
**For implementation by Antigravity (Gemini 3.1 Pro) in Jetpack Compose**

---

## 0. Design Concept

DOOT is a communication device before it's an "app." The brief — an offline voice relay for field, disaster, and remote-area use — points to one obvious world to draw from: **field radio hardware.** Physical walkie-talkies, mission-control readouts, signal meters, channel selectors. Not a messaging app that happens to work offline — a **radio panel** that happens to run on a phone.

That's the single idea every screen should reinforce: **you are looking at a transceiver panel, not a chat app.**

### Why this, and not the obvious alternative
The obvious default for a "walkie-talkie app" brief is a friendly rounded chat-bubble UI (think WhatsApp with a mic button). We're deliberately not doing that — it undersells the ISRO/field-communication seriousness of the brief and looks like every other messaging clone. Leaning into hardware-panel language (signal bars, channel selectors, VU-meter-style loaders, monospace readouts) is more distinctive, more true to the actual use case, and — practically — reads as more "engineered" to judges than a chat skin.

### Self-critique pass (what I deliberately avoided)
- No cream background + terracotta serif (the generic "AI-generated doc" look) — went dark-panel instead, which the subject matter actually earns.
- No identical rounded SaaS cards with soft drop-shadows — panels use hairline borders like real device seams, not shadow blobs.
- Only one deliberate glow moment (the PTT button when active) — restraint everywhere else.
- Uppercase tracked labels are used, but only where real hardware would print them (panel labels like "SIGNAL", "CHANNEL") — not decoratively on every heading.
- Monospace type is reserved for actual readouts (timestamps, signal strength, elapsed time) — because real radio equipment has digital numeric displays, not because monospace looks techy.

---

## 1. Design Tokens

### 1.1 Color

| Token | Hex | Use |
|---|---|---|
| `bg.base` | `#0E1216` | App background — the "chassis" |
| `bg.panel` | `#171D22` | Raised panels (top bar, message log container, bottom control panel) |
| `bg.panel.alt` | `#1F262C` | Inputs, secondary surfaces, individual log entries |
| `border.hairline` | `#2B333A` | 1dp borders on all panels — replaces drop shadows |
| `text.primary` | `#EDEFF2` | Primary text |
| `text.secondary` | `#8B96A1` | Secondary/meta text |
| `text.disabled` | `#4B545C` | Disabled state |
| `accent.amber` | `#FFB020` | Standby / idle-ready / "your" transmissions / PTT button |
| `accent.amber.dim` | `#3A2A12` | Amber tint backgrounds (e.g. your message panel fill) |
| `accent.teal` | `#45D9C7` | Connected state / incoming transmissions / signal indicator |
| `accent.teal.dim` | `#0F2C29` | Teal tint backgrounds (e.g. received message panel fill) |
| `accent.red` | `#FF4D4F` | Alert / recording-active / errors |
| `accent.red.dim` | `#3A1315` | Red tint backgrounds |

No gradients. No soft box-shadows on panels — only the PTT glow (see §1.4) uses a shadow, and it's colored, not grey.

### 1.2 Typography

Use **IBM Plex Sans** (UI text) and **IBM Plex Mono** (readouts). This isn't an arbitrary techy choice — IBM Plex ships matching **Devanagari** and **Gurmukhi** weights (Plex Sans Devanagari, Noto Sans Gurmukhi as fallback), which matters because this app actually renders Hindi and Punjabi script on-screen, not just Latin text. Set the font family with script-aware fallback so Hindi/Punjabi transcript text doesn't silently fall back to the system default and look inconsistent with the rest of the UI.

```kotlin
val PlexSans = FontFamily(/* IBM Plex Sans weights 400/500/600/700 */)
val PlexMono = FontFamily(/* IBM Plex Mono weights 400/500 */)
val PlexSansDevanagari = FontFamily(/* fallback for Hindi glyphs */)
val NotoSansGurmukhi = FontFamily(/* fallback for Punjabi glyphs */)
```

| Style | Size / Weight | Tracking | Use |
|---|---|---|---|
| `wordmark` | 34sp / 700 | +1sp | "DOOT" on splash screen only |
| `title` | 22sp / 600 | 0 | Screen titles |
| `body` | 16sp / 400 | 0 | Transcript text, general content |
| `label` | 13sp / 600 | +1.2sp, UPPERCASE | Panel labels: "SIGNAL", "CHANNEL", "CONNECTED TO" |
| `readout` | 14sp / 500, PlexMono, tabular figures | 0 | Timestamps, signal dB, elapsed time, latency |
| `button` | 15sp / 600 | 0 | Button text, sentence case ("Connect", not "CONNECT") |

Sentence case for anything the user reads as an instruction or action (buttons, body text). Uppercase tracked labels are reserved strictly for panel/meter labels, per the hardware-nameplate logic above — don't let it creep onto headings or buttons.

### 1.3 Spacing & Shape

- Spacing scale: 4 / 8 / 12 / 16 / 24 / 32 / 48 dp
- Panel corner radius: **4dp** (near-sharp, technical — not the rounded-card default)
- Button corner radius: **8dp**
- Circular elements (PTT button, status dots, signal rings): full round — the *only* fully-rounded shapes in the UI, which makes the PTT button visually read as a distinct, physical, pressable object against the squared panels around it
- Borders: 1dp `border.hairline` on every panel, always — this is the primary way panels are differentiated from the background, not shadow or fill contrast alone

### 1.4 The one glow moment

Per the "spend your boldness in one place" principle: the **only** shadow/glow effect in the entire app is on the PTT button when actively recording — a soft `accent.amber`-colored (shifting to `accent.red` while held) radial glow behind the circular button, expanding subtly while held. Nowhere else in the app uses a shadow. This makes that one moment — the physical act of transmitting — feel different from everything else on screen, which is exactly the moment that should feel different.

### 1.5 Iconography

Simple 1.5dp-stroke line icons, no fills, no gradients, no glossy/skeuomorphic treatment. Custom-drawn where needed (signal bars, waveform) rather than pulled from a generic icon pack, so they can use the exact accent colors above. Icons needed: mic (PTT), signal bars (4-bar), Wi-Fi/connection, siren/alert triangle, speaker (playback), disconnect/X.

---

## 2. Screen Specs

### 2.1 Loading Screen (`SplashScreen.kt`) — shown every launch

**Why every launch, and why that's honest, not decorative:** the app genuinely reloads STT/TTS models into memory on every cold start (constructing sherpa-onnx recognizer/synthesizer objects from the ONNX files takes a few real seconds even after the one-time asset copy). The loading screen should show **real** status text tied to what's actually happening in `DootApplication.onCreate()` — not a fake spinner. This also reads well to judges: it visibly communicates "on-device AI models loading," which is the whole point of the offline pitch.

```
┌─────────────────────────────┐
│                               │
│                               │
│                               │
│            D O O T            │   ← wordmark style, centered, letter-spaced
│      voice, carried as text   │   ← tagline, text.secondary, 13sp
│                               │
│                               │
│        ▁ ▃ ▅ ▇ ▅ ▃ ▁          │   ← 7-bar signal-meter loader, bars
│                               │      fill/animate sequentially, looping
│                               │
│   LOADING VOICE MODELS...     │   ← readout style, uppercase, text.secondary
│                               │
│                               │
│      Prototype Build          │   ← small, text.disabled, bottom corner
└─────────────────────────────┘
```

**Behavior:**
- Bar loader: 7 vertical bars, `accent.amber`, each animates height in a staggered wave loop (like a VU meter idling) while loading is in progress — implement as an `InfiniteTransition` in Compose, staggered by ~80ms per bar.
- Status text cycles through **real** milestones, driven by actual init callbacks, not a timer:
  1. `COPYING LANGUAGE ASSETS...`
  2. `LOADING SPEECH ENGINE...`
  3. `LOADING VOICE ENGINE...`
  4. `READY`
- On `READY`: bars snap to a single steady full-height state for ~300ms (a small "confirm" beat), then cross-fade (300ms, no slide) into the Connect screen. This is the one page-load motion moment for this screen — don't add extra flourishes on top of it.
- If loading fails (missing/corrupt model asset — see LLD §C.7): replace the status line with `MODEL LOAD FAILED` in `accent.red`, and show a `Retry` button. Never let this screen hang silently.

---

### 2.2 Connect Screen (`ConnectScreen.kt`)

This is the "who are we connecting to" screen — framed as scanning for a nearby unit, not "adding a contact."

```
┌─────────────────────────────┐
│  DOOT                    ⚡  │  ← small wordmark top-left, battery/status icon top-right (optional)
│                               │
│                               │
│         ◜       ◝            │
│      ◟  (device icon)  ◞      │  ← radar-sweep rings animating outward
│         ◟       ◞            │      from a center device glyph, teal, looping
│                               │      while searching
│                               │
│   SEARCHING FOR NEARBY UNIT   │  ← label style, text.secondary
│                               │
│  ┌─────────────────────────┐ │
│  │ ●  Rahul's Phone         │ │  ← appears once a peer is found:
│  │    Signal: ▂▄▆█           │ │    panel with signal bars (live, from
│  │                          │ │    Wi-Fi Direct RSSI if available, else static)
│  │         [ Connect ]      │ │
│  └─────────────────────────┘ │
│                               │
│  Make sure both phones have   │  ← help text, text.secondary, only shown
│  Wi-Fi and Location turned on │    while searching / if no device found
└─────────────────────────────┘
```

**Behavior:**
- Radar rings: 2-3 concentric circles expanding outward from center and fading, looping, `accent.teal` at low opacity — stops the moment a peer is found.
- On peer found: rings freeze, a single outward "ping" ring animates once (not looping) from the peer's position to mark the discovery moment, then the device panel fades/slides up from below.
- Connected state (post-tap, or auto-connect per prototype scope): panel content swaps to `Connected to Rahul's Phone` with a solid `accent.teal` dot (not animating — steady dot = steady connection, that's the meter-reading logic), and auto-navigates to the Talk screen after ~800ms, OR shows an `Enter Channel` button if you'd rather it be a deliberate tap (recommend deliberate tap for the demo — gives the presenter a clean beat to narrate).
- Failed connection: panel shows `Couldn't connect. Try again.` in `accent.red`-tinted panel, `Retry` button. No blame language, states what to do.

---

### 2.3 Talk Screen (`TalkScreen.kt`) — main screen

This is both the "who we're connected to" screen and the transcript/chat screen — combined into one, framed as a radio log, not a messaging thread.

```
┌─────────────────────────────┐
│ ● CONNECTED TO RAHUL'S PHONE │  ← top bar, bg.panel, hairline bottom border
│   Signal ▂▄▆█        [ X ]   │    teal dot + label style text + signal bars
│                               │    + disconnect icon, right-aligned
├─────────────────────────────┤
│  [ EN ]  [ HI ]  [ PA ]      │  ← channel/language selector, segmented,
│                               │    active = amber fill, inactive = outline only
├─────────────────────────────┤
│                               │
│  ┌───────────────────────┐   │  ← received message: left-aligned,
│  │ HI · Rahul             │   │    teal.dim fill, teal left border accent,
│  │ नमस्ते, सब ठीक है         │   │    label row shows lang tag + sender
│  │                 09:41:03│   │    (readout style, bottom-right)
│  └───────────────────────┘   │
│                               │
│              ┌──────────────┐│  ← sent message: right-aligned,
│              │ EN · You     ││    amber.dim fill, amber right border
│              │ All clear    ││    accent
│              │    09:41:18  ││
│              └──────────────┘│
│                               │
│     Channel open.             │  ← empty-state text if no messages yet,
│     Hold to talk.              │    text.secondary, centered
│                               │
├─────────────────────────────┤
│                               │
│      ▁▃▅█▅▃▁ (waveform)       │  ← appears ONLY while recording, above
│         00:03 / 5s             │    the PTT button, animated bars +
│                               │    monospace elapsed/max readout
│                               │
│   ⚠            🎙            │  ← Alert button (outline, red) to the
│  Alert    ( PTT button )      │    left, PTT button centered, large,
│                               │    circular, amber idle / red + glow held
└─────────────────────────────┘
```

**Behavior:**
- **Top bar signal dot**: steady teal = connected. If a packet is received, the dot does a single quick pulse (scale 1→1.3→1, 200ms) instead of a persistent animation — motion answers the event, doesn't run continuously.
- **Language selector**: tapping switches `selectedLang`; only affects the *next* recording, doesn't retroactively change past log entries (each entry keeps the language tag it was actually recorded in).
- **PTT button**: 88dp circle, centered, `accent.amber` fill idle. On press: scales to 96%, fill shifts to `accent.red`, glow radius expands (§1.4), waveform bars appear above with a quick fade-in (150ms), haptic feedback fires (`HapticFeedbackType.LongPress` on press-down). On release: scale springs back, glow contracts and fades, waveform bars fade out over 200ms rather than disappearing instantly, then the new transcript entry fades into the log (only the newest entry animates in — don't re-animate the whole list).
- **Elapsed/max readout** (`00:03 / 5s`): PlexMono, updates live, turns `accent.red` in the last 1 second before the 5s hard cap (LLD §C.5) as a subtle "wrapping up" cue.
- **Alert button**: smaller, outlined in `accent.red`, siren icon + "Alert" label. Tap opens a lightweight confirم — actually per copy guidance below, a short confirm sheet: `Send alert to Rahul's Phone?` with `Send` (filled red) / `Cancel` (text button). On send, plays a brief red flash on the button (not a full-screen takeover) and adds an `ALERT` tagged entry to the log in a red.dim panel, distinct from normal messages.
- **Message log entries**: squared panels (4dp radius) with a **2dp colored left/right border accent** (teal for received, on the left since left-aligned; amber for sent, on the right since right-aligned) rather than full bubble fills — keeps the "log entry" feel instead of "chat bubble" feel. Timestamp always bottom-right of each entry, PlexMono, `text.secondary`.
- **Disconnected mid-session**: top bar dot turns `accent.red`, label changes to `DISCONNECTED — TAP TO RECONNECT`, tapping it re-runs discovery. PTT still works locally (STT still shows text, just doesn't send — per LLD §C.7 graceful degradation) — show a small inline note under the input area: `Not sent — no device connected` under that specific log entry, not a blocking dialog.

---

## 3. Compose Implementation Notes for Antigravity

- Define tokens above as a custom `ColorScheme`/`Typography`/`Shapes` in `ui/theme/Theme.kt`, `Color.kt`, `Type.kt`, `Shape.kt` — don't hardcode hex values inline in screens; reference theme tokens so the palette can be tuned in one place.
- New files needed beyond the LLD's existing `ui/` package: `ui/SplashScreen.kt`, `ui/components/PttButton.kt`, `ui/components/WaveformVisualizer.kt`, `ui/components/SignalBars.kt`, `ui/components/LogEntry.kt`, `ui/components/LanguageSelector.kt`.
- `WaveformVisualizer` and `SignalBars` should be simple custom-drawn Composables using `Canvas`, not third-party charting libraries — they're decorative/indicative, not real audio-reactive FFT output for the prototype (don't over-engineer this; a looping pseudo-random bar animation while recording is genuinely fine for a hackathon demo).
- Respect system font-scale and `reducedMotion` accessibility settings: if reduced motion is on, the radar sweep, PTT glow pulse, and bar-loader should fall back to static/instant state changes instead of animating.
- Minimum touch target 48dp on every tappable element (PTT button already exceeds this at 88dp; make sure Alert button and language selector segments meet it too).
- Verify text contrast: `text.secondary` (`#8B96A1`) on `bg.panel` (`#171D22`) should be checked against WCAG AA for the 13sp label size — if Antigravity's rendering comes out under 4.5:1, lighten `text.secondary` slightly rather than shrinking text further.
