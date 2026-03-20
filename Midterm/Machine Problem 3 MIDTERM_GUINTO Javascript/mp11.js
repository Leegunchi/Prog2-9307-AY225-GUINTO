"use strict";

const FREQ_COLORS = [
  "var(--accent)", "#5eb5ff", "#ff5f57", "#ffbd2e",
  "#bf7fff",       "#ff9960", "#60ffca", "#ff60a8",
];

function renderMP11(records) {
  const container = document.getElementById("mp11-results");
  if (!container) return;

  const total = records.length;

  const examFreq   = new Map();  
  const resultFreq = new Map();  
  const typeFreq   = new Map();  

  for (const r of records) {
    examFreq  .set(r.exam,   (examFreq  .get(r.exam)   ?? 0) + 1);
    resultFreq.set(r.result, (resultFreq.get(r.result) ?? 0) + 1);
    typeFreq  .set(r.type,   (typeFreq  .get(r.type)   ?? 0) + 1);
  }

  const passCount = resultFreq.get("PASS") ?? 0;

  container.innerHTML = `

    <!-- Section header -->
    <div class="section-header">
      <div class="section-title">Frequency Count</div>
      <div class="section-badge">MP11 · ${total} records · ${examFreq.size} exams</div>
    </div>

    <!-- Exam frequency -->
    <div class="freq-section">
      <div class="freq-section-title">Exam — ${examFreq.size} distinct values</div>
      ${buildFreqRows(examFreq, total, FREQ_COLORS)}
    </div>

    <!-- Result frequency -->
    <div class="freq-section">
      <div class="freq-section-title">Result — pass / fail breakdown</div>
      ${buildFreqRows(resultFreq, total, ["var(--accent)", "var(--red)"])}
    </div>

    <!-- Candidate type frequency -->
    <div class="freq-section">
      <div class="freq-section-title">Candidate Type — ${typeFreq.size} distinct values</div>
      ${buildFreqRows(typeFreq, total, ["#5eb5ff", "#ffbd2e", "#bf7fff"])}
    </div>

    <!-- Summary footer -->
    <div class="summary-box" style="margin-top:8px">
      <div class="summary-item">
        <div class="stat-label">Total Records</div>
        <div class="stat-value">${total}</div>
      </div>
      <div class="summary-item">
        <div class="stat-label">Distinct Exams</div>
        <div class="stat-value">${examFreq.size}</div>
      </div>
      <div class="summary-item">
        <div class="stat-label">Pass Rate</div>
        <div class="stat-value" style="color:var(--accent)">
          ${((passCount / total) * 100).toFixed(1)}%
        </div>
      </div>
      <div class="summary-item">
        <div class="stat-label">Candidate Types</div>
        <div class="stat-value">${typeFreq.size}</div>
      </div>
    </div>
  `;
}

function buildFreqRows(freqMap, total, colors) {
  const sorted   = [...freqMap.entries()].sort((a, b) => b[1] - a[1]);
  const maxCount = sorted.length > 0 ? sorted[0][1] : 1;

  return sorted.map(([value, count], i) => {
    const pct    = ((count / total) * 100).toFixed(1);
    const barPct = ((count / maxCount) * 100).toFixed(1); 
    const color  = colors[i % colors.length];

    return `
      <div class="freq-row">
        <div class="freq-rank">${i + 1}</div>
        <div class="freq-label" title="${value}">${truncate(value, 38)}</div>
        <div class="freq-bar-wrap">
          <div class="freq-bar-track">
            <div class="freq-bar-fill" style="width:${barPct}%; background:${color}"></div>
          </div>
          <div class="freq-count">${count}</div>
          <div class="freq-pct">${pct}%</div>
        </div>
      </div>`;
  }).join("");
}