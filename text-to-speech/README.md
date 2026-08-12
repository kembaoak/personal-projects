# Text to Speech

Phase 1 (Web Speech API) implementation. See `tts-project-plan.md` for the full plan.

## Structure

- `shared/ttsEngine.js` — the speech engine (chunking, speak/pause/resume/stop/skip). Single source of truth, symlinked into `webapp/shared` and `extension/shared`.
- `webapp/` — standalone page: paste or upload text (.txt/.pdf), play it back.
- `extension/` — Chrome extension (Manifest V3): right-click "Read selection aloud", or open the popup to read the selection/page.
- `server/` — empty placeholder for the Phase 2 Kokoro backend.

## Run the web app

No build step needed.

```sh
cd webapp
python3 -m http.server 8000
```

Then open http://localhost:8000. (Opening `index.html` directly via `file://` also works in Chrome, but a local server avoids any file:// edge cases.)

## Load the Chrome extension

1. Go to `chrome://extensions`
2. Enable "Developer mode" (top right)
3. Click "Load unpacked" and select the `extension/` folder
4. Select text on any page and right-click → "Read selection aloud", or click the extension icon to open the popup (Read Selection / Read Page, plus voice/speed/pitch controls)

## Notes

- Both front ends share `shared/ttsEngine.js` — voice logic isn't duplicated.
- The extension uses an offscreen document (`offscreen.html`/`offscreen.js`) to run `speechSynthesis`, since MV3 service workers have no DOM/audio access. This is what the background service worker and popup both message.
- PDF text extraction in the web app uses pdf.js from a CDN — no server or API key involved.
