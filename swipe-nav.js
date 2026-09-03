(()=>{
  if(innerWidth>700)return;
  const pages=['index.html','meal.html','schedule.html'];
  const bar=document.querySelector('.app-tabs');
  if(!bar)return;

  const path=location.pathname.split('/').pop()||'index.html';
  const current=Math.max(0,pages.indexOf(path));
  bar.style.setProperty('--nav-index',current);

  let sx=0,sy=0,drag=false,moved=false;
  const threshold=55;

  bar.addEventListener('touchstart',e=>{
    const t=e.changedTouches[0];
    sx=t.clientX; sy=t.clientY;
    drag=true; moved=false;
    bar.classList.add('is-dragging');
  },{passive:true});

  bar.addEventListener('touchmove',e=>{
    if(!drag)return;
    const t=e.changedTouches[0];
    const dx=t.clientX-sx,dy=t.clientY-sy;
    if(Math.abs(dx)<=Math.abs(dy))return;
    moved=true;

    const width=bar.clientWidth/3;
    const progress=dx/width;
    const target=Math.max(0,Math.min(2,current-progress));
    bar.style.setProperty('--nav-index',target);
  },{passive:true});

  bar.addEventListener('touchend',e=>{
    if(!drag)return;
    drag=false;
    bar.classList.remove('is-dragging');

    const t=e.changedTouches[0];
    const dx=t.clientX-sx,dy=t.clientY-sy;
    const horizontal=Math.abs(dx)>Math.abs(dy);

    if(!horizontal||!moved||Math.abs(dx)<threshold){
      bar.style.setProperty('--nav-index',current);
      return;
    }

    const next=dx<0?current+1:current-1;
    if(next<0||next>=pages.length){
      bar.style.setProperty('--nav-index',current);
      return;
    }

    // 손가락을 놓은 순간 목표 탭으로 자연스럽게 착지한 뒤 페이지 전환
    bar.style.setProperty('--nav-index',next);
    setTimeout(()=>{location.href=pages[next]},180);
  },{passive:true});

  bar.addEventListener('touchcancel',()=>{
    drag=false;
    bar.classList.remove('is-dragging');
    bar.style.setProperty('--nav-index',current);
  },{passive:true});
})();
