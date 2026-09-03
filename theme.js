(()=>{
  const KEY='gpja-theme';
  const root=document.documentElement;
  const meta=document.querySelector('meta[name="theme-color"]');
  const media=window.matchMedia('(prefers-color-scheme: dark)');
  const systemDark=()=>media.matches;
  const apply=()=>{
    const saved=localStorage.getItem(KEY);
    const dark=saved==='dark'||(saved!=='light'&&systemDark());
    root.classList.toggle('dark',dark);
    if(meta)meta.setAttribute('content',dark?'#05070b':'#dcecff');
    const button=document.querySelector('#themeToggle');
    if(button){
      button.textContent=dark?'☀':'☾';
      button.setAttribute('aria-label',dark?'라이트모드로 전환':'다크모드로 전환');
      button.title=dark?'라이트모드로 전환':'다크모드로 전환';
    }
  };
  apply();
  const onSystemChange=()=>{if(!localStorage.getItem(KEY))apply()};
  if(media.addEventListener)media.addEventListener('change',onSystemChange);else media.addListener(onSystemChange);
  document.addEventListener('DOMContentLoaded',()=>{
    document.querySelector('#themeToggle')?.addEventListener('click',()=>{
      const dark=root.classList.contains('dark');
      localStorage.setItem(KEY,dark?'light':'dark');
      apply();
    });
  });
})();
