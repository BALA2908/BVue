# Build Prompt — Personal Ad-Free YouTube Client (Android)

This is a ready-to-use prompt set for building the app with Claude (ideally **Claude Code**, so it can write files and run the build directly).

**How to use it**
1. Paste **Section A (Master Brief)** first. It's the always-on context. If you use Claude Code, save it as a `CLAUDE.md` file in your project root so it stays in context automatically (see the Claude Code docs for the exact setup).
2. Then feed the **Section B** phase prompts **one at a time**, in order. Don't paste them all at once — let each phase finish, test it on a real phone, then move on.
3. After each phase, ask Claude to summarize what changed and exactly how to test it on a physical device.

---

## SECTION A — MASTER BRIEF (paste first / save as CLAUDE.md)

You are helping me build a **personal, single-user Android app**: a minimal, ad-free YouTube client for my own phone. It will **not be published** — it's for my private use only. I understand this bypasses YouTube's ads and is for personal use.

### Vision
A clean app where I can **search YouTube, watch any public video ad-free, and keep audio playing in the background / with the screen off** (podcast/music style). No Google account, no ads, no recommendations clutter — just search → play.

### Architecture (important — do not deviate without telling me why)
**Everything runs on-device. There is no backend server.** Extraction and playback happen on the same phone. This is deliberate: YouTube's stream URLs are temporary and often IP-locked, so extracting on a server and playing on the phone causes IP-mismatch failures. On-device extraction avoids that and is the most reliable single-user setup (it's how the open-source NewPipe app works).

### Tech stack (use these exact choices)
- **Language:** Kotlin
- **minSdk:** 24, **targetSdk:** latest stable
- **UI:** Jetpack Compose (host the player via `AndroidView` + Media3 `PlayerView`)
- **Extraction engine:** **NewPipeExtractor** (`com.github.TeamNewPipe:NewPipeExtractor` via JitPack) — does search, metadata, and resolves stream URLs with no API key and no quota
- **Player:** **AndroidX Media3 / ExoPlayer** — `media3-exoplayer`, `media3-exoplayer-dash`, `media3-ui`, `media3-session`
- **HTTP:** OkHttp
- **Local storage:** Room
- **Async:** Kotlin Coroutines

### NON-NEGOTIABLE RULES (these are the known traps — respect them throughout)
1. **NewPipeExtractor has no built-in HTTP client.** It exposes an abstract `Downloader` that I must implement with OkHttp. Model it on NewPipe's reference `DownloaderImpl` (set a realistic browser `User-Agent`). Call `NewPipe.init(downloader)` exactly once at app startup.
2. **All NewPipeExtractor calls are blocking.** Never call them on the main thread. Wrap every call in a repository layer running on `Dispatchers.IO`. Calling them from UI code will freeze the app.
3. **HD streams are split into separate video and audio.** YouTube only offers combined (muxed) video+audio up to **360p**. For **1080p and above**, you get a **video-only** stream PLUS a **separate audio-only** stream. If you play just the video URL, it will be **silent**. For HD, combine the two with ExoPlayer's **`MergingMediaSource`** (video source + audio source) so they stay in sync. The quality selector must pick the right strategy per resolution (muxed for ≤360p, merged for higher).
4. **Background playback must use Media3's `MediaSessionService`** (the modern approach — not a plain Service or the deprecated MediaBrowserServiceCompat). It must provide a `MediaSession`, a media notification, and working lock-screen + Bluetooth/headset controls.
5. **Manifest & permissions, done correctly:**
   - `INTERNET`
   - `FOREGROUND_SERVICE` and, for **Android 14+**, `FOREGROUND_SERVICE_MEDIA_PLAYBACK`
   - `android:foregroundServiceType="mediaPlayback"` on the service declaration
   - Request `POST_NOTIFICATIONS` at runtime on **Android 13+**
6. **Audio focus:** set `AudioAttributes` with `handleAudioFocus = true` so playback pauses for phone calls and ducks for other audio.
7. **Survive screen-off:** call `setWakeMode(C.WAKE_MODE_NETWORK)` on the player.
8. **Stream URLs expire and are IP-bound.** On any playback error (expired/forbidden URL), automatically **re-extract** the streams and retry once before surfacing an error.
9. **Restricted content:** age-restricted videos need my logged-in cookies (out of scope for v1 — show a clear message). Members-only, rentals, and DRM/Widevine titles generally won't play — detect and show a clear message instead of crashing.

### Audio-only ("silent" / screen-off) mode
A toggle that extracts and plays **only the audio stream** — lighter on data and battery, perfect for music/podcasts with the screen off. It reuses the same `MediaSessionService`.

### Local data (Room, all on-device)
Watch history, search history, favorites/playlists, and **resume positions** so videos pick up where I left off.

### Settings
Default quality, audio-only toggle, theme (light/dark).

### How I want you to work
- Build **strictly phase by phase**; do not jump ahead to later phases.
- After each phase: summarize what you built, list any new files, and give me **exact steps to test it on a physical Android device**.
- Use a pragmatic clean architecture (repository + ViewModel), not over-engineered for a solo project.
- Explain any non-obvious decision in 1–2 lines.
- Use the **latest stable** versions of all libraries; if a library's API differs from what you expected, adapt and tell me what changed.
- When solving an extraction or playback problem, reference how the open-source **NewPipe** app handles it.

---

## SECTION B — PHASE PROMPTS (feed one at a time, in order)

### Phase 0 — Project setup
> Set up a new Android project per the Master Brief: Kotlin, minSdk 24, Jetpack Compose. Add and configure all dependencies (NewPipeExtractor via JitPack, Media3 exoplayer/dash/ui/session, OkHttp, Room, Coroutines), including the JitPack repository in settings.gradle. Add the manifest permissions from rule 5. Make sure the project builds and launches a blank Compose screen on a device.
> **Done when:** the app compiles and runs an empty screen on my phone.

### Phase 1 — Extraction layer (the engine)
> Build the extraction layer per rules 1–2. Implement an OkHttp-backed `Downloader` modeled on NewPipe's `DownloaderImpl`, and initialize NewPipeExtractor once at startup. Create a repository with `suspend` functions (all on `Dispatchers.IO`) for: (a) search by query returning videos, (b) resolve a video into its stream lists (muxed, video-only, audio-only), and (c) fetch the trending/home feed. Add a temporary debug call that logs search results and resolved stream URLs.
> **Done when:** I can see search results and resolved stream URLs in logcat.

### Phase 2 — Player (the ad-free core)
> Build the playback flow per rule 3. Screens: a search box → results list → tap a result → player screen. Implement ExoPlayer playback that uses a direct muxed source for ≤360p and a `MergingMediaSource` (video + audio) for 1080p and above. Add a quality selector, a seek bar, and play/pause. Host the Media3 `PlayerView` in Compose via `AndroidView`.
> **Done when:** I can search, tap a result, and watch it **with audio** at both 360p and 1080p.

### Phase 3 — Background play (the headline feature)
> Add background playback per rules 4–7. Implement a Media3 `MediaSessionService` hosting the player, with a media notification and working lock-screen + Bluetooth/headset controls. Configure audio focus (`handleAudioFocus = true`) and `setWakeMode(C.WAKE_MODE_NETWORK)`. Handle the Android 13+ `POST_NOTIFICATIONS` runtime request and the Android 14+ foreground-service requirements.
> **Done when:** audio keeps playing when I background the app or lock the screen, and the notification + lock-screen controls work.

### Phase 4 — Usability & local data
> Add local persistence with Room per the Master Brief: watch history, search history, favorites/playlists, and resume positions (videos resume where I left off). Build a settings screen (default quality, theme) and implement the **audio-only mode** toggle (extract and play only the audio stream, reusing the MediaSessionService).
> **Done when:** history populates, favorites persist across restarts, videos resume from where I stopped, and the audio-only toggle works.

### Phase 5 — Resilience & polish
> Add resilience per rules 8–9: auto re-extract and retry once on expired/forbidden stream URLs, and show clear messages for age-restricted / members-only / DRM content instead of crashing. Add a trending/home screen on launch. Optional if time allows: channel and playlist browsing, and offline downloads (you already have the stream URL from Phase 1 — feed it to a download manager).
> **Done when:** playback automatically recovers from an expired URL, and restricted videos show a clear, friendly message.

---

### Maintenance note (after it's built)
YouTube changes its internals periodically, which breaks extraction. The fix is almost always to **bump the NewPipeExtractor dependency to its latest version** and rebuild. Keep an eye on the NewPipeExtractor releases page when something stops working.
