# TTS Project Plan — Chrome Extension + Web App

## Goal
Build a free/near-free text-to-speech system with two front ends:
1. A Chrome extension that reads selected text / page content aloud
2. A standalone web app for pasting or uploading longer text (articles, PDFs, etc.)

Both share one "speech engine" so voice logic isn't duplicated.

## Strategy
Ship in two phases so there's a working product almost immediately, then upgrade quality without rebuilding anything.

- **Phase 1 — Web Speech API** (free, built into Chrome, zero infrastructure)
- **Phase 2 — Kokoro-82M self-hosted** (near-premium quality, still free/near-free, swapped in as a backend behind the same interface)

---

## Phase 1: Web Speech API version (build this first)

### 1. Chrome Extension
- `manifest.json` (Manifest V3)
  - Permissions: `activeTab`, `scripting`, `storage`
  - Background service worker for state (play/pause/voice settings)
  - Content script to grab selected text or full page text (via `document.getSelection()` / Readability-style extraction)
  - Popup UI: play/pause/stop, voice picker, speed/pitch slider
- Core logic: `speechSynthesis.speak(new SpeechSynthesisUtterance(text))`
- Right-click context menu: "Read selection aloud"
- Store last-used voice/speed in `chrome.storage.local`

### 2. Web App
- Simple single-page app (React or plain HTML/JS)
- Paste text box + file upload (txt/pdf — extract text client-side)
- Same `speechSynthesis` API, shared logic pulled into a small `ttsEngine.js` module used by both the extension and the app
- Playback controls: play/pause/skip sentence, speed, voice selection, highlight current sentence while reading (nice-to-have)

### 3. Shared engine module (`ttsEngine.js`)
- Wraps `speechSynthesis` calls
- Splits long text into chunks (browsers choke on very long single utterances)
- Exposes: `speak(text, {voice, rate, pitch})`, `pause()`, `resume()`, `stop()`
- Designed so Phase 2 can swap in a different backend without touching the UI

### Deliverable for Phase 1
Working extension (loadable unpacked in Chrome) + working web app, $0 cost, no server.

---

## Phase 2: Upgrade voice quality with Kokoro-82M (optional, do after Phase 1 works)

### 1. Local TTS server
- Run Kokoro-82M (Apache 2.0, ~2-3GB VRAM, runs on CPU too) behind a minimal FastAPI/Flask server
- One endpoint: `POST /speak` → `{ text, voice }` → returns audio (mp3/wav) or streams it
- Runs on your own machine, or a small cloud VM (~$5-10/mo) if you want it always-on

### 2. Swap the engine
- Update `ttsEngine.js` to optionally call `http://localhost:PORT/speak` instead of `speechSynthesis`
- Add a settings toggle: "Use high-quality voice (local server)" vs "Use browser voice (free, always available)"
- Extension and app both benefit automatically since they share the module

### Deliverable for Phase 2
Same extension/app, better voice option available when the local server is running, still no per-character cost.

---

## Suggested build order for Claude Code
1. Scaffold repo: `/extension`, `/webapp`, `/shared` (ttsEngine.js), `/server` (Phase 2, empty for now)
2. Build `ttsEngine.js` with Web Speech API implementation + text chunking
3. Build the web app around it (fastest to test in isolation)
4. Build the Chrome extension around the same engine
5. Test end-to-end in Chrome (load unpacked extension, run web app locally)
6. (Later) Add Kokoro server and the backend toggle

## Notes / constraints to give Claude Code
- Manifest V3 only (V2 is deprecated in Chrome)
- No external API keys or paid services in Phase 1
- Keep `ttsEngine.js` framework-agnostic so both extension and app can import it
- PDF text extraction client-side only (e.g. pdf.js) — no server needed in Phase 1
