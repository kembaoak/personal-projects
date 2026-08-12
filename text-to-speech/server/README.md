# Phase 2 — Kokoro-82M server (not built yet)

This folder will hold a minimal FastAPI/Flask server that wraps Kokoro-82M and
exposes `POST /speak` (`{ text, voice } -> audio`). `shared/ttsEngine.js` will
gain a mode that calls this server instead of `speechSynthesis`, so the
extension and web app pick up the higher-quality voice automatically.

See `../tts-project-plan.md` for the full plan.
