"use strict";

window.records = [];

const csvInput       = document.getElementById("csv-input");
const fileDisplay    = document.getElementById("file-path-display");
const loadBtn        = document.getElementById("load-btn");
const filePrompt     = document.getElementById("file-prompt");
const loadedBanner   = document.getElementById("loaded-banner");
const bannerFilename = document.getElementById("banner-filename");
const bannerMeta     = document.getElementById("banner-meta");
const mainContent    = document.getElementById("main-content");
const reloadBtn      = document.getElementById("reload-btn");
const loadError      = document.getElementById("load-error");

csvInput.addEventListener("change", () => {
  const file = csvInput.files[0];

  if (!file) return;

  if (!file.name.toLowerCase().endsWith(".csv")) {
    showError("ERROR: Selected file must be a .csv file.");
    loadBtn.disabled = true;
    fileDisplay.value = "";
    return;
  }

  hideError();
  fileDisplay.value = file.name;  
  loadBtn.disabled  = false;
});

loadBtn.addEventListener("click", () => {
  const file = csvInput.files[0];
  if (!file) return;

  const reader = new FileReader();

  reader.onload = (e) => {
    const rawText = e.target.result;

    try {
      window.records = parseCSV(rawText);

      if (window.records.length === 0) {
        showError("ERROR: No valid records found in the file.");
        return;
      }

      bannerFilename.textContent = file.name;
      bannerMeta.textContent     =
        `${window.records.length} records · ${(file.size / 1024).toFixed(1)} KB`;
      filePrompt.style.display  = "none";
      loadedBanner.style.display = "flex";
      mainContent.style.display  = "block";
      renderMP9(window.records);
      renderMP10(window.records);
      renderMP11(window.records);

    } catch (err) {
      showError(`ERROR: ${err.message}`);
    }
  };

  reader.onerror = () => showError("ERROR: Could not read the file.");
  reader.readAsText(file, "UTF-8");
});

reloadBtn.addEventListener("click", () => {
  csvInput.value        = "";
  fileDisplay.value     = "";
  loadBtn.disabled      = true;
  window.records        = [];

  loadedBanner.style.display = "none";
  mainContent.style.display  = "none";
  filePrompt.style.display   = "block";
  hideError();
});

window.switchTab = function (mpId) {
  document.querySelectorAll(".tab").forEach(t => t.classList.remove("active"));
  document.querySelectorAll(".panel").forEach(p => p.classList.remove("active"));
  document.getElementById(`tab-${mpId}`).classList.add("active");
  document.getElementById(`panel-${mpId}`).classList.add("active");
};

function parseCSV(text) {
  const clean = text.replace(/^\uFEFF/, "").replace(/\r\n/g, "\n").replace(/\r/g, "\n");
  const lines  = clean.split("\n");

  const records    = [];
  let headerFound  = false;
  let lineNum      = 0;

  let candidateIdx = -1, typeIdx  = -1, examIdx   = -1;
  let dateIdx      = -1, scoreIdx = -1, resultIdx = -1, timeIdx = -1;

  for (const rawLine of lines) {
    lineNum++;
    const cols = splitCSVLine(rawLine);

    if (!headerFound) {
      cols.forEach((col, i) => {
        const h = col.trim().toLowerCase();
        if      (h === "candidate")             candidateIdx = i;
        else if (h === "student/ faculty/ nte") typeIdx      = i;
        else if (h === "exam")                  examIdx      = i;
        else if (h === "exam date")             dateIdx      = i;
        else if (h === "score")                 scoreIdx     = i;
        else if (h === "result")                resultIdx    = i;
        else if (h === "time used")             timeIdx      = i;
      });

      if (candidateIdx !== -1 && examIdx !== -1 && scoreIdx !== -1) {
        headerFound = true;
      }
      continue;
    }

    if (rawLine.trim() === "" || cols.every(c => c.trim() === "")) continue;

    try {
      const candidate = cols[candidateIdx]?.trim() ?? "";
      const exam      = safeGet(cols, examIdx);
      if (!candidate || !exam) continue;

      const score = parseInt(safeGet(cols, scoreIdx), 10);
      if (isNaN(score)) continue;

      records.push({
        candidate,
        type    : safeGet(cols, typeIdx),
        exam,
        examDate: safeGet(cols, dateIdx),
        score,
        result  : safeGet(cols, resultIdx),
        timeUsed: safeGet(cols, timeIdx),
        passed()       { return this.result.toUpperCase() === "PASS"; },
        duplicateKey() { return `${this.candidate.toLowerCase()}||${this.exam.toLowerCase()}`; },
      });
    } catch {
      
    }
  }

  return records;
}

window.splitCSVLine = function splitCSVLine(line) {
  const fields = [];
  let current  = "";
  let inQuotes = false;

  for (const ch of line) {
    if (ch === '"')              inQuotes = !inQuotes;
    else if (ch === "," && !inQuotes) { fields.push(current); current = ""; }
    else                         current += ch;
  }
  fields.push(current);
  return fields;
};

function safeGet(cols, idx) {
  return (idx >= 0 && idx < cols.length) ? cols[idx].trim() : "";
}

window.truncate = function truncate(s, maxLen) {
  return s.length > maxLen ? s.slice(0, maxLen - 1) + "…" : s;
};

function showError(msg) {
  loadError.textContent     = msg;
  loadError.style.display   = "block";
}
function hideError() {
  loadError.style.display = "none";
}