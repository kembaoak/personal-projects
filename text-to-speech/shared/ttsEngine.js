// Framework-agnostic wrapper around the Web Speech API.
// Both the extension and the web app import this module, and Phase 2
// (a local Kokoro server) is meant to slot in behind the same speak/pause/
// resume/stop/skip interface so neither front end has to change.

const MAX_CHUNK_LENGTH = 200; // long single utterances get cut off by some voices/browsers

function splitIntoChunks(text, maxLength = MAX_CHUNK_LENGTH) {
  const sentences = text.match(/[^.!?]+[.!?]+(\s+|$)|[^.!?]+$/g) || [text];
  const chunks = [];
  let current = '';

  for (const sentence of sentences) {
    if (current.length > 0 && (current + sentence).length > maxLength) {
      chunks.push(current.trim());
      current = sentence;
    } else {
      current += sentence;
    }
  }
  if (current.trim().length > 0) chunks.push(current.trim());
  return chunks;
}

export class TTSEngine {
  constructor() {
    this.synth = window.speechSynthesis;
    this.queue = [];
    this.currentIndex = 0;
    this.isPaused = false;
    this.isSpeaking = false;
    this._skipping = false;
    this.options = { voice: null, rate: 1, pitch: 1, volume: 1 };
    this._onChunkStart = null;
    this._onBoundary = null;
    this._onEnd = null;
  }

  getVoices() {
    return this.synth.getVoices();
  }

  onVoicesChanged(callback) {
    this.synth.addEventListener('voiceschanged', callback);
  }

  speak(text, options = {}) {
    this.stop();
    this.options = { ...this.options, ...options };
    this.queue = splitIntoChunks(text);
    this.currentIndex = 0;
    this.isPaused = false;
    this.isSpeaking = this.queue.length > 0;
    this._speakCurrentChunk();
  }

  _speakCurrentChunk() {
    if (this.currentIndex >= this.queue.length) {
      this.isSpeaking = false;
      if (this._onEnd) this._onEnd();
      return;
    }

    const chunkText = this.queue[this.currentIndex];
    const utterance = new SpeechSynthesisUtterance(chunkText);
    utterance.voice = this.options.voice;
    utterance.rate = this.options.rate;
    utterance.pitch = this.options.pitch;
    utterance.volume = this.options.volume;

    utterance.onend = () => {
      if (this._skipping) {
        this._skipping = false;
        return;
      }
      this.currentIndex += 1;
      if (!this.isPaused) this._speakCurrentChunk();
    };

    utterance.onboundary = (event) => {
      if (this._onBoundary) this._onBoundary(event, this.currentIndex);
    };

    if (this._onChunkStart) this._onChunkStart(chunkText, this.currentIndex, this.queue.length);

    this.synth.speak(utterance);
  }

  pause() {
    if (this.isSpeaking && !this.isPaused) {
      this.isPaused = true;
      this.synth.pause();
    }
  }

  resume() {
    if (this.isPaused) {
      this.isPaused = false;
      this.synth.resume();
    }
  }

  stop() {
    this.isPaused = false;
    this.isSpeaking = false;
    this._skipping = false;
    this.queue = [];
    this.currentIndex = 0;
    this.synth.cancel();
  }

  skipNext() {
    if (this.currentIndex < this.queue.length - 1) {
      this._skipping = true;
      this.synth.cancel();
      this.currentIndex += 1;
      this.isPaused = false;
      this._speakCurrentChunk();
    } else {
      this.stop();
      if (this._onEnd) this._onEnd();
    }
  }

  skipPrevious() {
    if (this.currentIndex > 0) {
      this._skipping = true;
      this.synth.cancel();
      this.currentIndex -= 1;
      this.isPaused = false;
      this._speakCurrentChunk();
    }
  }

  onChunkStart(callback) {
    this._onChunkStart = callback;
  }

  onBoundary(callback) {
    this._onBoundary = callback;
  }

  onEnd(callback) {
    this._onEnd = callback;
  }
}
