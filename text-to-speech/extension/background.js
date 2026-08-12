const OFFSCREEN_DOCUMENT_PATH = 'offscreen.html';

async function hasOffscreenDocument() {
  const contexts = await chrome.runtime.getContexts({
    contextTypes: ['OFFSCREEN_DOCUMENT'],
    documentUrls: [chrome.runtime.getURL(OFFSCREEN_DOCUMENT_PATH)],
  });
  return contexts.length > 0;
}

async function ensureOffscreenDocument() {
  if (await hasOffscreenDocument()) return;
  await chrome.offscreen.createDocument({
    url: OFFSCREEN_DOCUMENT_PATH,
    reasons: ['AUDIO_PLAYBACK'],
    justification: 'Play synthesized speech audio using the Web Speech API',
  });
}

chrome.runtime.onInstalled.addListener(() => {
  chrome.contextMenus.create({
    id: 'read-selection',
    title: 'Read selection aloud',
    contexts: ['selection'],
  });
});

async function getSettings() {
  const stored = await chrome.storage.local.get(['voiceName', 'rate', 'pitch']);
  return {
    voiceName: stored.voiceName || null,
    rate: stored.rate ?? 1,
    pitch: stored.pitch ?? 1,
  };
}

async function speakText(text) {
  await ensureOffscreenDocument();
  const settings = await getSettings();
  chrome.runtime.sendMessage({
    target: 'offscreen',
    type: 'speak',
    text,
    options: settings,
  });
}

chrome.contextMenus.onClicked.addListener((info) => {
  if (info.menuItemId !== 'read-selection' || !info.selectionText) return;
  speakText(info.selectionText);
});

chrome.runtime.onMessage.addListener((message, sender, sendResponse) => {
  if (message.type === 'popup-speak') {
    (async () => {
      await ensureOffscreenDocument();
      chrome.runtime.sendMessage({
        target: 'offscreen',
        type: 'speak',
        text: message.text,
        options: message.options,
      });
      sendResponse({ ok: true });
    })();
    return true;
  }

  if (message.type === 'popup-control') {
    (async () => {
      await ensureOffscreenDocument();
      chrome.runtime.sendMessage({ target: 'offscreen', type: message.action });
      sendResponse({ ok: true });
    })();
    return true;
  }

  return false;
});
