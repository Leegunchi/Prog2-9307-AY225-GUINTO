class DataRecord {
  constructor(title, releaseDate, totalSales) {
    this.title         = title;
    this.releaseDate   = releaseDate;
    this.totalSales    = totalSales;
    this.movingAverage = -1; 
  }
}

function parseCSV(text) {

  function splitLine(line) {
    const result = [];
    let cur = '', inQuote = false;
    for (let i = 0; i < line.length; i++) {
      const ch = line[i];
      if (ch === '"') { inQuote = !inQuote; }
      else if (ch === ',' && !inQuote) { result.push(cur.trim()); cur = ''; }
      else { cur += ch; }
    }
    result.push(cur.trim());
    return result;
  }

  const lines = text.replace(/\r\n/g, '\n').replace(/\r/g, '\n').split('\n');

  if (lines.length < 2)
    throw new Error('CSV file is empty or has no data rows.');

  const headers  = splitLine(lines[0]).map(h => h.toLowerCase());
  const titleIdx = headers.indexOf('title');
  const dateIdx  = headers.indexOf('release_date');
  const salesIdx = headers.indexOf('total_sales');

  if (titleIdx === -1 || dateIdx === -1 || salesIdx === -1)
    throw new Error('Missing required columns: title, release_date, total_sales');

  const records = [];
  let skipped = 0;

  for (let i = 1; i < lines.length; i++) {
    if (!lines[i].trim()) continue;
    try {
      const cols  = splitLine(lines[i]);
      const title = cols[titleIdx];
      const date  = cols[dateIdx];
      const sales = parseFloat(cols[salesIdx]);

      if (!title || !date || isNaN(sales)) throw new Error('Invalid field');
      records.push(new DataRecord(title, date, sales));
    } catch {
      skipped++;
    }
  }

  if (records.length === 0)
    throw new Error('No valid records found in file.');

  console.log(`Loaded ${records.length} records, skipped ${skipped} malformed rows.`);
  return records;
}

function sortByDate(records) {
  records.sort((a, b) => new Date(a.releaseDate) - new Date(b.releaseDate));
}

function applyMovingAverage(records, windowSize = 3) {
  for (let i = 0; i < records.length; i++) {
    if (i >= windowSize - 1) {
      let sum = 0;
      for (let j = i - (windowSize - 1); j <= i; j++) {
        sum += records[j].totalSales;
      }
      records[i].movingAverage = sum / windowSize;
    }
  }
}

function validateFile(file) {
  if (!file)
    throw new Error('No file selected.');
  if (!file.name.toLowerCase().endsWith('.csv'))
    throw new Error(`"${file.name}" is not a .csv file.`);
  if (file.size === 0)
    throw new Error('File is empty.');
}

function showStatus(msg, type = 'error') {
  const el = document.getElementById('status');
  el.textContent = (type === 'error' ? '✗ ERROR: ' : '✓ ') + msg;
  el.className = type;
  el.style.display = 'block';
}

function displayResults(records) {
  const sales      = records.map(r => r.totalSales);
  const avgs       = records.filter(r => r.movingAverage >= 0).map(r => r.movingAverage);
  const overallAvg = sales.reduce((a, b) => a + b, 0) / sales.length;
  const maxSales   = Math.max(...sales);
  const maxAvg     = Math.max(...avgs, 1);

  document.getElementById('stat-records').textContent = records.length.toLocaleString();
  document.getElementById('stat-dates').textContent =
    `${records[0].releaseDate} → ${records[records.length - 1].releaseDate}`;
  document.getElementById('stat-avg').textContent = overallAvg.toFixed(2);
  document.getElementById('stat-top').textContent = maxSales.toFixed(2);
  document.getElementById('stats-bar').style.display = 'flex';

  const tbody = document.getElementById('table-body');
  tbody.innerHTML = '';

  records.forEach((r, i) => {
    const hasAvg   = r.movingAverage >= 0;
    const barWidth = hasAvg ? Math.round((r.movingAverage / maxAvg) * 80) : 0;

    const tr = document.createElement('tr');
    tr.innerHTML = `
      <td class="td-idx">${i + 1}</td>
      <td class="td-title" title="${r.title}">${r.title}</td>
      <td class="td-date">${r.releaseDate}</td>
      <td class="td-sales">${r.totalSales.toFixed(2)}</td>
      <td class="${hasAvg ? 'td-avg' : 'td-na'}">
        ${hasAvg
          ? `<div class="avg-bar-wrap">
               <span>${r.movingAverage.toFixed(2)}</span>
               <div class="avg-bar" style="width:${barWidth}px"></div>
             </div>`
          : 'N/A'}
      </td>`;
    tbody.appendChild(tr);
  });

  document.getElementById('table-wrap').style.display = 'block';
}

function handleFile(file) {
  try {
    validateFile(file);
  } catch (err) {
    showStatus(err.message, 'error');
    return;
  }

  const reader = new FileReader();

  reader.onerror = () =>
    showStatus('File could not be read. Check file permissions.', 'error');

  reader.onload = (e) => {
    try {
      const records = parseCSV(e.target.result);
      sortByDate(records);
      applyMovingAverage(records, 3);
      showStatus(`"${file.name}" loaded — ${records.length} records processed.`, 'success');
      displayResults(records);
    } catch (err) {
      showStatus(err.message, 'error');
    }
  };

  reader.readAsText(file);
}

const fileInput  = document.getElementById('file-input');
const uploadZone = document.getElementById('upload-zone');

fileInput.addEventListener('change', (e) => {
  if (e.target.files[0]) handleFile(e.target.files[0]);
});

uploadZone.addEventListener('dragover', (e) => {
  e.preventDefault();
  uploadZone.classList.add('drag-over');
});

uploadZone.addEventListener('dragleave', () => {
  uploadZone.classList.remove('drag-over');
});

uploadZone.addEventListener('drop', (e) => {
  e.preventDefault();
  uploadZone.classList.remove('drag-over');
  const file = e.dataTransfer.files[0];
  if (file) handleFile(file);
});