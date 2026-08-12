const readSelectionBtn = document.getElementById('read-selection');
const readPageBtn = document.getElementById('read-page');
const playPauseBtn = document.getElementById('play-pause');
const stopBtn = document.getElementById('stop');
const skipBtn = document.getElementById('skip');
const voiceSelect = document.getElementById('voice-select');
const rateSlider = document.getElementById('rate');
const rateValue = document.getElementById('rate-value');
const pitchSlider = document.getElementById('pitch');
const pitchValue = document.getElementById('pitch-value');

let isPlaying = false;
let isPaused = false;

function populateVoices() {
  const voices = window.speechSynthesis.getVoices();
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

window.speechSynthesis.onvoiceschanged = populateVoices;
populateVoices();

async function loadSettings() {
  const stored = await chrome.storage.local.get(['voiceName', 'rate', 'pitch']);
  if (stored.voiceName) voiceSelect.value = stored.voiceName;
  rateSlider.value = stored.rate ?? 1;
  pitchSlider.value = stored.pitch ?? 1;
  rateValue.textContent = rateSlider.value;
  pitchValue.textContent = pitchSlider.value;
}
loadSettings();

function saveSettings() {
  chrome.storage.local.set({
    voiceName: voiceSelect.value,
    rate: parseFloat(rateSlider.value),
    pitch: parseFloat(pitchSlider.value),
  });
}

voiceSelect.addEventListener('change', saveSettings);
rateSlider.addEventListener('input', () => {
  rateValue.textContent = rateSlider.value;
  saveSettings();
});
pitchSlider.addEventListener('input', () => {
  pitchValue.textContent = pitchSlider.value;
  saveSettings();
});

async function getSelectedTextFromActiveTab() {
  const [tab] = await chrome.tabs.query({ active: true, currentWindow: true });
  const [{ result }] = await chrome.scripting.executeScript({
    target: { tabId: tab.id },
    func: () => window.getSelection().toString(),
  });
  return result;
}

async function getPageTextFromActiveTab() {
  const [tab] = await chrome.tabs.query({ active: true, currentWindow: true });
  const [{ result }] = await chrome.scripting.executeScript({
    target: { tabId: tab.id },
    func: () => document.body.innerText,
  });
  return result;
}

function updatePlayPauseLabel() {
  playPauseBtn.textContent = isPaused ? 'Resume' : 'Pause';
  playPauseBtn.disabled = !isPlaying;
}

async function speak(text) {
  if (!text || !text.trim()) return;
  saveSettings();
  await chrome.runtime.sendMessage({
    type: 'popup-speak',
    text,
    options: {
      voiceName: voiceSelect.value,
      rate: parseFloat(rateSlider.value),
      pitch: parseFloat(pitchSlider.value),
    },
  });
  isPlaying = true;
  isPaused = false;
  updatePlayPauseLabel();
}

readSelectionBtn.addEventListener('click', async () => {
  const text = await getSelectedTextFromActiveTab();
  speak(text);
});

readPageBtn.addEventListener('click', async () => {
  const text = await getPageTextFromActiveTab();
  speak(text);
});

playPauseBtn.addEventListener('click', async () => {
  if (!isPlaying) return;
  if (isPaused) {
    await chrome.runtime.sendMessage({ type: 'popup-control', action: 'resume' });
    isPaused = false;
  } else {
    await chrome.runtime.sendMessage({ type: 'popup-control', action: 'pause' });
    isPaused = true;
  }
  updatePlayPauseLabel();
});

stopBtn.addEventListener('click', async () => {
  await chrome.runtime.sendMessage({ type: 'popup-control', action: 'stop' });
  isPlaying = false;
  isPaused = false;
  updatePlayPauseLabel();
});

skipBtn.addEventListener('click', () => {
  chrome.runtime.sendMessage({ type: 'popup-control', action: 'skip-next' });
});

updatePlayPauseLabel();
