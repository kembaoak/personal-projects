import { TTSEngine } from './shared/ttsEngine.js';

const textInput = document.getElementById('text-input');
const fileInput = document.getElementById('file-input');
const playPauseBtn = document.getElementById('play-pause');
const stopBtn = document.getElementById('stop');
const skipNextBtn = document.getElementById('skip-next');
const skipPrevBtn = document.getElementById('skip-prev');
const voiceSelect = document.getElementById('voice-select');
const rateSlider = document.getElementById('rate');
const rateValue = document.getElementById('rate-value');
const pitchSlider = document.getElementById('pitch');
const pitchValue = document.getElementById('pitch-value');
const nowReading = document.getElementById('now-reading');
const statusEl = document.getElementById('status');

const engine = new TTSEngine();
let isPlaying = false;
let isPaused = false;

function populateVoices() {
  const voices = engine.getVoices();
  const previouslySelected = voiceSelect.value;
  voiceSelect.innerHTML = '';
  voices.forEach((voice) => {
    const option = document.createElement('option');
    option.value = voice.name;
    option.textContent = `${voice.name} (${voice.lang})`;
    voiceSelect.appendChild(option);
  });
  if (previouslySelected) voiceSelect.value = previouslySelected;
}
engine.onVoicesChanged(populateVoices);
populateVoices();

rateSlider.addEventListener('input', () => {
  rateValue.textContent = rateSlider.value;
});
pitchSlider.addEventListener('input', () => {
  pitchValue.textContent = pitchSlider.value;
});

function currentOptions() {
  const voices = engine.getVoices();
  const voice = voices.find((v) => v.name === voiceSelect.value) || null;
  return { voice, rate: parseFloat(rateSlider.value), pitch: parseFloat(pitchSlider.value) };
}

function updateControls() {
  playPauseBtn.textContent = isPlaying && !isPaused ? 'Pause' : 'Play';
  stopBtn.disabled = !isPlaying;
  skipNextBtn.disabled = !isPlaying;
  skipPrevBtn.disabled = !isPlaying;
}

engine.onChunkStart((chunkText, index, total) => {
  nowReading.textContent = chunkText;
  statusEl.textContent = `Reading chunk ${index + 1} of ${total}`;
});

engine.onEnd(() => {
  isPlaying = false;
  isPaused = false;
  statusEl.textContent = 'Finished';
  updateControls();
});

playPauseBtn.addEventListener('click', () => {
  if (!isPlaying) {
    const text = textInput.value.trim();
    if (!text) return;
    engine.speak(text, currentOptions());
    isPlaying = true;
    isPaused = false;
  } else if (isPaused) {
    engine.resume();
    isPaused = false;
  } else {
    engine.pause();
    isPaused = true;
  }
  updateControls();
});

stopBtn.addEventListener('click', () => {
  engine.stop();
  isPlaying = false;
  isPaused = false;
  nowReading.textContent = '';
  statusEl.textContent = '';
  updateControls();
});

skipNextBtn.addEventListener('click', () => engine.skipNext());
skipPrevBtn.addEventListener('click', () => engine.skipPrevious());

fileInput.addEventListener('change', async () => {
  const file = fileInput.files[0];
  if (!file) return;

  statusEl.textContent = `Loading ${file.name}...`;
  try {
    const text = file.name.toLowerCase().endsWith('.pdf')
      ? await extractPdfText(file)
      : await file.text();
    textInput.value = text;
    statusEl.textContent = `Loaded ${file.name}`;
  } catch (err) {
    statusEl.textContent = `Failed to load ${file.name}: ${err.message}`;
  }
});

async function extractPdfText(file) {
  const arrayBuffer = await file.arrayBuffer();
  const pdf = await pdfjsLib.getDocument({ data: arrayBuffer }).promise;
  let fullText = '';
  for (let pageNum = 1; pageNum <= pdf.numPages; pageNum += 1) {
    const page = await pdf.getPage(pageNum);
    const content = await page.getTextContent();
    fullText += content.items.map((item) => item.str).join(' ') + '\n\n';
  }
  return fullText.trim();
}

updateControls();
