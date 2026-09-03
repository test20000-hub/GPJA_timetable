const $ = s => document.querySelector(s);
let rows = [];
let monthOffset = 0;
let deferredPrompt = null;

const pad = n => String(n).padStart(2, '0');
const iso = d => `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
const esc = v => String(v ?? '').replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/\"/g, '&quot;').replace(/'/g, '&#39;');
const fmt = d => `${d.getFullYear()}년 ${d.getMonth() + 1}월`;

function showNotice(text) {
  $('#notice').textContent = text;
  $('#notice').hidden = !text;
}

function render() {
  const now = new Date();
  const base = new Date(now.getFullYear(), now.getMonth() + monthOffset, 1);
  const year = base.getFullYear();
  const month = base.getMonth();
  const monthKey = `${year}-${pad(month + 1)}`;
  $('#monthLabel').textContent = fmt(base);

  const monthRows = rows.filter(r => r.date.startsWith(monthKey));
  if (!monthRows.length) {
    $('#schedule').innerHTML = '<div class="schedule-empty card">이 달에는 등록된 학사일정이 없습니다.</div>';
    return;
  }

  const grouped = {};
  monthRows.forEach(r => (grouped[r.date] ||= []).push(r));
  const dates = Object.keys(grouped).sort();
  const today = iso(now);

  $('#schedule').innerHTML = dates.map(date => {
    const d = new Date(`${date}T00:00:00`);
    const weekday = ['일', '월', '화', '수', '목', '금', '토'][d.getDay()];
    const isToday = date === today;
    const items = grouped[date].map(r => `
      <div class="schedule-item">
        <div class="schedule-event">${esc(r.event)}</div>
        ${r.content ? `<div class="schedule-content">${esc(r.content)}</div>` : ''}
        <div class="schedule-grade">${[r.grade1, r.grade2, r.grade3].map((v, i) => v === 'Y' ? `${i + 1}학년` : '').filter(Boolean).join(' · ') || '전교'}</div>
      </div>
    `).join('');

    return `<article class="schedule-day card${isToday ? ' schedule-today' : ''}">
      <div class="schedule-date"><strong>${d.getDate()}</strong><span>${weekday}</span></div>
      <div class="schedule-events">${items}</div>
    </article>`;
  }).join('');
}

async function load() {
  const url = new URL('data/schedule.json', document.baseURI);
  url.searchParams.set('v', Date.now());
  try {
    const res = await fetch(url.toString(), { cache: 'no-store', headers: { Accept: 'application/json' } });
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    const json = await res.json();
    if (!Array.isArray(json.rows)) throw new Error('rows 형식 오류');
    rows = json.rows;
    $('#updatedLabel').textContent = json.updatedAt ? `업데이트 ${json.updatedAt}` : 'NEIS 학사일정';
    showNotice(rows.length ? '' : '학사일정 데이터가 없습니다.');
    render();
  } catch (e) {
    console.error('Schedule load failed:', e);
    $('#updatedLabel').textContent = '불러오기 실패';
    showNotice('학사일정을 불러오지 못했습니다. 새로고침 후 다시 시도해주세요.');
    $('#schedule').innerHTML = '<div class="schedule-empty card">학사일정 데이터를 불러오지 못했습니다.</div>';
  }
}

$('#prevMonth').onclick = () => { monthOffset--; render(); };
$('#nextMonth').onclick = () => { monthOffset++; render(); };
$('#todayBtn').onclick = () => { monthOffset = 0; render(); };
window.addEventListener('beforeinstallprompt', e => {
  e.preventDefault();
  deferredPrompt = e;
  $('#installBtn').hidden = false;
});
$('#installBtn').onclick = async () => {
  if (!deferredPrompt) return;
  deferredPrompt.prompt();
  deferredPrompt = null;
  $('#installBtn').hidden = true;
};
if ('serviceWorker' in navigator) navigator.serviceWorker.register('sw.js?v=3').catch(e => console.warn('SW registration failed', e));
load();
