"use strict";

function renderMP10(records) {
  const container = document.getElementById("mp10-results");
  if (!container) return;

  const keyMap = new Map();
  for (const r of records) {
    const k = r.duplicateKey();
    if (!keyMap.has(k)) keyMap.set(k, []);
    keyMap.get(k).push(r);
  }

  const dupeGroups = [];
  let uniqueCount  = 0;

  for (const group of keyMap.values()) {
    if (group.length > 1) dupeGroups.push(group);
    else                  uniqueCount++;
  }

  const dupeFlagged = dupeGroups.reduce((sum, g) => sum + g.length, 0);
  const total       = records.length;

  let groupsHTML = "";

  if (dupeGroups.length === 0) {
    groupsHTML = `
      <div class="no-dupes">
        <div class="no-dupes-icon">✓</div>
        <div class="no-dupes-title">No duplicate records found</div>
        <div class="no-dupes-sub">All ${total} records have unique candidate–exam pairs.</div>
      </div>`;
  } else {
    groupsHTML = dupeGroups.map((group, idx) => `
      <div class="dupe-group">
        <div class="dupe-group-header">
          <div class="dupe-group-title">Duplicate Group #${idx + 1} — ${truncate(group[0].exam, 46)}</div>
          <div class="dupe-count-badge">${group.length} entries</div>
        </div>
        <table class="data-table">
          <thead>
            <tr>
              <th>Candidate</th>
              <th>Exam Date</th>
              <th class="num">Score</th>
              <th>Result</th>
              <th>Time Used</th>
            </tr>
          </thead>
          <tbody>
            ${group.map(r => `
              <tr>
                <td>${truncate(r.candidate, 24)}</td>
                <td>${r.examDate}</td>
                <td class="num">${r.score}</td>
                <td><span class="pass-chip ${r.passed() ? "pass" : "fail"}">${r.result}</span></td>
                <td>${r.timeUsed}</td>
              </tr>`).join("")}
          </tbody>
        </table>
      </div>`).join("");
  }

  container.innerHTML = `

    <!-- Section header -->
    <div class="section-header">
      <div class="section-title">Duplicate Record Detection</div>
      <div class="section-badge">MP10 · ${total} records checked</div>
    </div>

    <!-- Summary stats -->
    <div class="summary-box">
      <div class="summary-item">
        <div class="stat-label">Total Records</div>
        <div class="stat-value">${total}</div>
      </div>
      <div class="summary-item">
        <div class="stat-label">Unique Pairs</div>
        <div class="stat-value">${uniqueCount}</div>
      </div>
      <div class="summary-item">
        <div class="stat-label">Dupe Groups</div>
        <div class="stat-value" style="color:${dupeGroups.length > 0 ? 'var(--amber)' : 'var(--accent)'}">
          ${dupeGroups.length}
        </div>
      </div>
      <div class="summary-item">
        <div class="stat-label">Flagged Records</div>
        <div class="stat-value" style="color:${dupeFlagged > 0 ? 'var(--amber)' : 'var(--accent)'}">
          ${dupeFlagged}
        </div>
      </div>
    </div>

    <!-- Groups or clean state -->
    ${groupsHTML}
  `;
}