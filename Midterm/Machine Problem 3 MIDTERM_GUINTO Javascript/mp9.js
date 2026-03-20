"use strict";

function renderMP9(records) {
  const container = document.getElementById("mp9-results");
  if (!container) return;

  const total = records.length;
  let passCount = 0;
  let highScore = -Infinity, lowScore = Infinity;
  let topName = "", botName = "";
  let scoreSum = 0;

  const examMap = new Map();

  for (const r of records) {
    if (r.passed()) passCount++;

    if (r.score > highScore) { highScore = r.score; topName = r.candidate; }
    if (r.score < lowScore)  { lowScore  = r.score; botName = r.candidate; }

    scoreSum += r.score;

    
    if (!examMap.has(r.exam)) {
      examMap.set(r.exam, { count: 0, passCount: 0, scoreSum: 0 });
    }
    const b = examMap.get(r.exam);
    b.count++;
    if (r.passed()) b.passCount++;
    b.scoreSum += r.score;
  }

  const failCount = total - passCount;
  const avg       = scoreSum / total;
  const passRate  = (passCount / total) * 100;

  
  const examRows = [...examMap.entries()].sort((a, b) => b[1].count - a[1].count);

  
  container.innerHTML = `

    <!-- Section header -->
    <div class="section-header">
      <div class="section-title">Dataset Statistics</div>
      <div class="section-badge">MP09 · ${total} records</div>
    </div>

    <!-- Overall stat cards -->
    <div class="stat-grid">
      <div class="stat-card accent-card">
        <div class="stat-label">Total Records</div>
        <div class="stat-value">${total}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">Passed</div>
        <div class="stat-value">${passCount}</div>
        <div class="stat-sub">${passRate.toFixed(1)}% of total</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">Failed</div>
        <div class="stat-value">${failCount}</div>
        <div class="stat-sub">${(100 - passRate).toFixed(1)}% of total</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">Avg Score</div>
        <div class="stat-value">${avg.toFixed(1)}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">Distinct Exams</div>
        <div class="stat-value">${examMap.size}</div>
      </div>
    </div>

    <!-- Pass rate bar -->
    <div class="rate-bar-wrap">
      <div class="rate-bar-label">
        <span>Pass Rate</span>
        <span>${passRate.toFixed(1)}% passed &nbsp;·&nbsp; ${(100 - passRate).toFixed(1)}% failed</span>
      </div>
      <div class="rate-bar-track">
        <div class="rate-bar-fill" style="width: ${passRate}%"></div>
      </div>
    </div>

    <!-- High / Low score cards -->
    <div class="score-row">
      <div class="score-card high">
        <div class="stat-label">Highest Score</div>
        <div class="score-number">${highScore}</div>
        <div class="score-name">${truncate(topName, 28)}</div>
      </div>
      <div class="score-card low">
        <div class="stat-label">Lowest Score</div>
        <div class="score-number">${lowScore}</div>
        <div class="score-name">${truncate(botName, 28)}</div>
      </div>
    </div>

    <!-- Per-exam breakdown table -->
    <div class="data-table-wrap">
      <div class="data-table-title">Per-Exam Breakdown — sorted by takers</div>
      <table class="data-table">
        <thead>
          <tr>
            <th>Exam</th>
            <th class="num">Takers</th>
            <th class="num">Pass</th>
            <th class="num">Fail</th>
            <th class="num">Avg Score</th>
          </tr>
        </thead>
        <tbody>
          ${examRows.map(([exam, s]) => {
            const ePass = s.passCount;
            const eFail = s.count - s.passCount;
            const eAvg  = (s.scoreSum / s.count).toFixed(1);
            const ePct  = ((ePass / s.count) * 100).toFixed(0);
            return `
              <tr>
                <td><span class="exam-name" title="${exam}">${truncate(exam, 38)}</span></td>
                <td class="num">${s.count}</td>
                <td class="num">
                  <div class="mini-bar-wrap">
                    <div class="mini-bar-track">
                      <div class="mini-bar-fill" style="width:${ePct}%"></div>
                    </div>
                    <span>${ePass}</span>
                  </div>
                </td>
                <td class="num">${eFail}</td>
                <td class="num">${eAvg}</td>
              </tr>`;
          }).join("")}
        </tbody>
      </table>
    </div>
  `;
}