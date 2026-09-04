/* GPJA Smart Dashboard v2.0.0 */
(()=>{
  const $=s=>document.querySelector(s);
  const PERIOD_TIMES={1:['09:10','10:00'],2:['10:10','11:00'],3:['11:10','12:00'],4:['13:00','13:50'],5:['14:00','14:50'],6:['15:00','15:50'],7:['16:00','16:50']};
  let rows=[];
  const pad=n=>String(n).padStart(2,'0');
  const iso=d=>`${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())}`;
  const mins=s=>{const[a,b]=s.split(':').map(Number);return a*60+b};
  const selected=()=>({grade:localStorage.getItem('gpja-grade')||'1',className:localStorage.getItem('gpja-class')||'5'});
  const lessons=()=>{const s=selected(),today=iso(new Date());return rows.filter(r=>String(r.grade)===s.grade&&String(r.className)===s.className&&r.date===today).sort((a,b)=>Number(a.period)-Number(b.period));};
  function state(now=new Date()){
    const m=now.getHours()*60+now.getMinutes()+now.getSeconds()/60;
    let current=null,next=null;
    for(const [p,[start,end]] of Object.entries(PERIOD_TIMES)){if(m>=mins(start)&&m<mins(end))current=Number(p);if(m<mins(start)&&next===null)next=Number(p)}
    return {current,next,m};
  }
  function fmtCountdown(seconds){seconds=Math.max(0,Math.ceil(seconds));const h=Math.floor(seconds/3600),m=Math.floor((seconds%3600)/60),s=seconds%60;return h?`${h}시간 ${pad(m)}분`:m?`${m}분 ${pad(s)}초`:`${s}초`}
  function lessonFor(list,p){return list.find(r=>Number(r.period)===p)}
  function render(){
    const card=$('#smartDashboard');if(!card)return;
    const now=new Date(),st=state(now),list=lessons(),cur=lessonFor(list,st.current),next=lessonFor(list,st.next);
    const total=list.length,completed=st.current?Math.max(0,list.filter(r=>Number(r.period)<st.current).length):st.next?list.filter(r=>Number(r.period)<st.next).length:total;
    const progress=total?Math.min(100,Math.round(completed/total*100)):0;
    let mode='수업 전',title='오늘 수업을 준비하세요',subject='';
    if(st.current&&cur){mode=`${st.current}교시 진행 중`;title=cur.subject||'수업';subject=cur.teacher?`${cur.teacher}${cur.room?` · ${cur.room}`:''}`:(cur.room||'')}
    else if(st.next&&next){mode=`${st.next}교시까지`;title=next.subject||'다음 수업';subject=next.teacher?`${next.teacher}${next.room?` · ${next.room}`:''}`:(next.room||'')}
    else if(total){mode='오늘 수업 종료';title='수고하셨습니다';subject='내일 시간표는 내일 자동으로 안내됩니다.'}
    else {mode='오늘 시간표';title='수업 데이터 없음';subject='NEIS 시간표를 불러오는 중입니다.'}
    let countdown='';
    if(st.current){const end=mins(PERIOD_TIMES[st.current][1]);countdown=fmtCountdown(Math.max(0,end*60-(st.m%1)*60-(Math.floor(st.m)*60)));const endSec=end*60-(Math.floor(st.m)*60+Math.floor((st.m%1)*60));countdown=fmtCountdown(endSec)}
    else if(st.next){const start=mins(PERIOD_TIMES[st.next][0]);countdown=fmtCountdown(start*60-(Math.floor(st.m)*60+Math.floor((st.m%1)*60)))}
    else countdown='—';
    $('#smartMode').textContent=mode;$('#smartSubject').textContent=title;$('#smartMeta').textContent=subject;$('#smartCountdown').textContent=countdown;$('#smartProgress').style.width=`${progress}%`;$('#smartProgressText').textContent=total?`${completed}/${total}교시 완료`:'시간표 준비 중';
    const nextBox=$('#smartNext');if(nextBox)nextBox.textContent=st.next&&next?`다음 · ${st.next}교시 ${next.subject||'수업'}`:(st.current?'다음 교시 없음':'다음 수업 없음');
    card.classList.toggle('is-active',!!st.current);
  }
  async function load(){try{const u=new URL('data/timetable.json',document.baseURI);u.searchParams.set('v','2.0.0');const r=await fetch(u,{cache:'no-store'});if(!r.ok)throw 0;const j=await r.json();rows=Array.isArray(j.rows)?j.rows:[];render()}catch{render()}}
  window.addEventListener('storage',e=>{if(e.key==='gpja-grade'||e.key==='gpja-class')render()});
  document.addEventListener('DOMContentLoaded',()=>{render();load();setInterval(render,1000)});
})();
