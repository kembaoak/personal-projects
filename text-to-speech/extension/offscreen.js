import { TTSEngine } from './shared/ttsEngine.js';

const engine = new TTSEngine();

function resolveVoice(voiceName) {
  if (!voiceName) return null;
  return engine.getVoices().find((v) => v.name === voiceName) || null;
}

chrome.runtime.onMessage.addListener((message) => {
  if (message.target !== 'offscreen') return;

  switch (message.type) {
    case 'speak':
      engine.speak(message.text, {
        voice: resolveVoice(message.options?.voiceName),
        rate: message.options?.rate ?? 1,
        pitch: message.options?.pitch ?? 1,
      });
      break;
    case 'pause':
      engine.pause();
      break;
    case 'resume':
      engine.resume();
      break;
    case 'stop':
      engine.stop();
      break;
    case 'skip-next':
      engine.skipNext();
      break;
  }
});
