const GPJA_VERSION='2.0.4';
const API='https://kschoolinfo.com/api/v1/meals';
const EDU='J10';
const SCHOOL='7531272';
const meal=document.querySelector('#meal');
const dateLabel=document.querySelector('#dateLabel');
const updated=document.querySelector('#updatedLabel');
const notice=document.querySelector('#notice');
const todayBtn=document.querySelector('#todayBtn');
const pad=n=>String(n).padStart(2,'0');
const ymd=d=>`${d.getFullYear()}${pad(d.getMonth()+1)}${pad(d.getDate())}`;
const dayName=d=>['일','월','화','수','목','금','토'][d.getDay()];
const isToday=d=>{const n=new Date();return ymd(d)===ymd(n)};
const esc=s=>String(s??'').replace(/[&<>\"']/g,m=>({'&':'&amp;','<':'&lt;','>':'&gt;','\"':'&quot;',"'":'&#39;'}[m]));
const holidayName=d=>{const k=ymd(d),fixed={'0101':'신정','0301':'삼일절','0505':'어린이날','0606':'현충일','0717':'제헌절','0815':'광복절','1003':'개천절','1009':'한글날','1225':'성탄절'};const known={'20260101':'신정','20260301':'삼일절','20260302':'삼일절 대체공휴일','20260505':'어린이날','20260524':'부처님오신날','20260525':'부처님오신날 대체공휴일','20260606':'현충일','20260815':'광복절','20260817':'광복절 대체공휴일','20260924':'추석','20260925':'추석 연휴','20260926':'추석 연휴','20260928':'추석 대체공휴일','20261003':'개천절','20261005':'개천절 대체공휴일','20261009':'한글날','20261225':'성탄절'};return known[k]||fixed[k.slice(4)]||''};
const isWeekend=d=>d.getDay()===0||d.getDay()===6;
const normalizeMenu=menu=>Array.isArray(menu)?menu.map(item=>typeof item==='string'?item:item?.name).filter(Boolean).map(String):[];
const normalizeRows=data=>{if(!Array.isArray(data))return[];return data.filter(Boolean).map(x=>({date:x.date,type:x.type?.name||x.mealTypeName||x.type||'급식',menu:normalizeMenu(x.menu??x.menus),calories:x.calories??x.calorie??''})).filter(x=>x.menu.length||x.calories||x.type)};
const setNotice=(message,show=true)=>{if(!notice)return;notice.textContent=message;notice.hidden=!show};
function versionUi(){document.querySelector('meta[name="version"]')?.setAttribute('content',GPJA_VERSION);document.querySelector('.version')?.replaceChildren(document.createTextNode(`v${GPJA_VERSION}`));}
function renderDate(){const holiday=holidayName(current);dateLabel.innerHTML=`<span class="date-main">${current.getMonth()+1}월 ${current.getDate()}일</span><span class="date-day">${dayName(current)}요일${isToday(current)?' · 오늘':''}</span>`;todayBtn?.classList.toggle('is-today',isToday(current));todayBtn?.setAttribute('aria-label',isToday(current)?'오늘 날짜':'오늘로 이동');return holiday;}
let current=new Date();
let requestSerial=0;
async function load(){versionUi();const serial=++requestSerial;const holiday=renderDate();const key=ymd(current);meal.innerHTML='<section class="card meal-card"><div class="meal-loading"><span class="meal-spinner"></span><span>급식 정보를 불러오는 중</span></div></section>';setNotice('',false);try{const u=new URL(API);u.searchParams.set('eduCode',EDU);u.searchParams.set('schoolCode',SCHOOL);u.searchParams.set('date',key);u.searchParams.set('_',`${Date.now()}-${serial}`);const controller=new AbortController();const timer=setTimeout(()=>controller.abort(),10000);let r;try{r=await fetch(u.toString(),{cache:'no-store',headers:{Accept:'application/json'},signal:controller.signal})}finally{clearTimeout(timer)}if(!r.ok)throw new Error(`HTTP ${r.status}`);const j=await r.json();if(j?.ok!==true)throw new Error(j?.error?.message||'API error');if(serial!==requestSerial)return;const rows=normalizeRows(j.data);if(!rows.length){meal.innerHTML=`<section class="card meal-card meal-empty-card"><div class="meal-empty-icon">${holiday?'🎉':'🍽️'}</div><div class="meal-empty-title">${holiday?esc(holiday):'등록된 급식 정보가 없습니다.'}</div><div class="meal-empty-sub">${holiday?'오늘은 공휴일입니다.':'선택한 날짜에 등록된 급식 정보가 없습니다.'}</div></section>`;updated.textContent=holiday?`${holiday} · 급식 없음`:'급식 데이터 확인 완료 · 등록된 급식 없음';return}meal.innerHTML=rows.map(x=>{const items=x.menu;return `<section class="card meal-card"><div class="meal-head"><span class="meal-type"><span class="meal-type-dot"></span>${esc(x.type)}</span><strong>${items.length?`${items.length}가지 메뉴`:'메뉴 없음'}</strong></div><div class="meal-menu" role="list" aria-label="${esc(x.type)} 메뉴">${items.map((m,i)=>`<button class="menu-item" type="button" role="listitem" aria-label="${esc(m)} 메뉴 검색"><span class="menu-index">${String(i+1).padStart(2,'0')}</span><span class="menu-name">${esc(m)}</span></button>`).join('')}</div>${x.calories?`<div class="meal-cal"><span>오늘의 영양정보</span><strong>${esc(x.calories)}</strong></div>`:''}</section>`}).join('');updated.textContent=`${dayName(current)}요일 · ${rows.length}개 식단`;}catch(e){if(serial!==requestSerial)return;console.error('Meal load failed:',e);const message=e?.name==='AbortError'?'급식 서버 응답 시간이 초과되었습니다.':'급식 데이터 서버 연결에 실패했습니다.';meal.innerHTML='<section class="card meal-card meal-error-card"><div class="meal-empty-icon">⚠️</div><div class="meal-empty-title">급식 정보를 불러오지 못했습니다.</div><div class="meal-empty-sub">잠시 후 다시 시도해 주세요.</div><button id="mealRetry" class="meal-retry" type="button">다시 불러오기</button></section>';setNotice(message,true);updated.textContent='급식 연결 실패';document.querySelector('#mealRetry')?.addEventListener('click',load,{once:true});}}
function moveDay(direction){current.setDate(current.getDate()+direction);load()}
document.querySelector('#prevDay')?.addEventListener('click',()=>moveDay(-1));
document.querySelector('#nextDay')?.addEventListener('click',()=>moveDay(1));
todayBtn?.addEventListener('click',()=>{current=new Date();load()});
versionUi();
load();