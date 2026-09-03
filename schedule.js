const $ = s => document.querySelector(s);
let rows = [];
let monthOffset = 0;
let view = 'calendar';
let deferredPrompt = null;

const pad = n => String(n).padStart(2, '0');
const iso = d => `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
const esc = v => String(v ?? '').replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/\"/g, '&quot;').replace(/'/g, '&#39;');
const fmt = d => `${d.getFullYear()}년 ${d.getMonth() + 1}월`;
const weekdayNames = ['일', '월', '화', '수', '목', '금', '토'];

function showNotice(text) {
  const el = $('#notice');
  el.textContent = text;
  el.hidden = !text;
}

function monthContext() {
  const now = new Date();
  const base = new Date(now.getFullYear(), now.getMonth() + monthOffset, 1);
  return {
    now,
    base,
    year: base.getFullYear(),
    month: base.getMonth(),
    monthKey: `${base.getFullYear()}-${pad(base.getMonth() + 1)}`
  };
}

function eventHtml(r, compact = false) {
  const grade = [r.grade1, r.grade2, r.grade3]
    .map((v, i) => v === 'Y' ? `${i + 1}학년` : '')
    .filter(Boolean)
    .join(' · ') || '전교';

  return `<div class="calendar-event${compact ? ' calendar-event-compact' : ''}" title="${esc(r.event)}${r.content ? ` · ${esc(r.content)}` : ''}">
    <strong>${esc(r.event)}</strong>
    ${!compact && r.content ? `<span>${esc(r.content)}</span>` : ''}
    ${!compact ? `<small>${grade}</small>` : ''}
  </div>`;
}

function renderCalendar(monthRows, base, today) {
  const grouped = {};
  monthRows.forEach(r => {
    if (!r.date) return;
    (grouped[r.date] ||= []).push(r);
  });

  const first = new Date(base.getFullYear(), base.getMonth(), 1);
  const last = new Date(base.getFullYear(), base.getMonth() + 1, 0);
  const start = new Date(first);
  start.setDate(first.getDate() - first.getDay());
  const end = new Date(last);
  end.setDate(last.getDate() + (6 - last.getDay()));

  const cells = [];
  for (const d = new Date(start); d <= end; d.setDate(d.getDate() + 1)) {
    const date = iso(d);
    const inMonth = d.getMonth() === base.getMonth();
    const dayRows = grouped[date] || [];
    const isToday = date === today;
    const events = dayRows.slice(0, 3).map(r => eventHtml(r, true)).join('');
    const more = dayRows.length > 3 ? `<div class="calendar-more">+${dayRows.length - 3}개</div>` : '';

    cells.push(`<div class="calendar-cell${inMonth ? '' : ' outside-month'}${isToday ? ' calendar-today' : ''}" data-date="${date}">
      <div class="calendar-day-number">${d.getDate()}</div>
      <div class="calendar-events">${events}${more}</div>
    </div>`);
  }

  $('#calendar').innerHTML = `<div class="calendar-weekdays">${weekdayNames.map((name, i) => `<div class="calendar-weekday${i === 0 ? ' sunday' : i === 6 ? ' saturday' : ''}">${name}</div>`).join('')}</div><div class="calendar-grid">${cells.join('')}</div>`;
}

function renderList(monthRows, now) {
  if (!monthRows.length) {
    $('#schedule').innerHTML = '<div class="schedule-empty card">이 달에는 등록된 학사일정이 없습니다.</div>';
    return;
  }

  const grouped = {};
  monthRows.forEach(r => {
    if (!r.date) return;
    (grouped[r.date] ||= []).push(r);
  });

  const dates = Object.keys(grouped).sort();
  const today = iso(now);

  $('#schedule').innerHTML = dates.map(date => {
    const d = new Date(`${date}T00:00:00`);
    const weekday = weekdayNames[d.getDay()];
    const isToday = date === today;
    const items = grouped[date].map(r => {
      const grade = [r.grade1, r.grade2, r.grade3]
        .map((v, i) => v === 'Y' ? `${i + 1}학년` : '')
        .filter(Boolean)
        .join(' · ') || '전교';
      return `<div class="schedule-item"><div class="schedule-event">${esc(r.event)}</div>${r.content ? `<div class="schedule-content">${esc(r.content)}</div>` : ''}<div class="schedule-grade">${grade}</div></div>`;
    }).join('');
    return `<article class="schedule-day card${isToday ? ' schedule-today' : ''}"><div class="schedule-date"><strong>${d.getDate()}</strong><span>${weekday}</span></div><div class="schedule-events">${items}</div></article>`;
  }).join('');
}

function render() {
  const { now, base, monthKey } = monthContext();
  $('#monthLabel').textContent = fmt(base);
  const monthRows = rows.filter(r => String(r.date || '').startsWith(monthKey));

  if (view === 'calendar') {
    renderCalendar(monthRows, base, iso(now));
  } else {
    renderList(monthRows, now);
  }
}

function setView(nextView) {
  view = nextView;
  $('#calendar').hidden = view !== 'calendar';
  $('#schedule').hidden = view !== 'list';
  $('#calendarViewBtn').classList.toggle('active', view === 'calendar');
  $('#listViewBtn').classList.toggle('active', view === 'list');
  render();
}

async function load() {
  const url = new URL('./data/schedule.json', document.baseURI);
  url.searchParams.set('v', Date.now().toString());

  try {
    const res = await fetch(url.toString(), {
      cache: 'no-store',
      headers: { Accept: 'application/json' }
    });
    if (!res.ok) throw new Error(`HTTP ${res.status}`);

    const json = await res.json();
    if (!json || !Array.isArray(json.rows)) throw new Error('rows 형식 오류');

    rows = json.rows.filter(r => r && /^\d{4}-\d{2}-\d{2}$/.test(String(r.date || '')));
    $('#updatedLabel').textContent = json.updatedAt ? `업데이트 ${json.updatedAt}` : 'NEIS 학사일정';
    showNotice(rows.length ? '' : '학사일정 데이터가 없습니다.');
    render();
  } catch (e) {
    console.error('Schedule load failed:', e);
    rows = [];
    $('#updatedLabel').textContent = '불러오기 실패';
    showNotice('학사일정을 불러오지 못했습니다. 잠시 후 다시 시도해주세요.');
    $('#calendar').innerHTML = '<div class="schedule-empty">학사일정 데이터를 불러오지 못했습니다.</div>';
    $('#schedule').innerHTML = '<div class="schedule-empty card">학사일정 데이터를 불러오지 못했습니다.</div>';
  }
}

$('#prevMonth').addEventListener('click', () => { monthOffset--; render(); });
$('#nextMonth').addEventListener('click', () => { monthOffset++; render(); });
$('#todayBtn').addEventListener('click', () => { monthOffset = 0; render(); });
$('#calendarViewBtn').addEventListener('click', () => setView('calendar'));
$('#listViewBtn').addEventListener('click', () => setView('list'));

window.addEventListener('beforeinstallprompt', e => {
  e.preventDefault();
  deferredPrompt = e;
  $('#installBtn').hidden = false;
});

$('#installBtn').addEventListener('click', async () => {
  if (!deferredPrompt) return;
  deferredPrompt.prompt();
  deferredPrompt = null;
  $('#installBtn').hidden = true;
});

if ('serviceWorker' in navigator) {
  navigator.serviceWorker.register('./sw.js?v=4').catch(e => console.warn('SW registration failed', e));
}

setView('calendar');
load();
