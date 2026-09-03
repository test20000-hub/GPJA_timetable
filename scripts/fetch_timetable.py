import json, os, urllib.parse, urllib.request
from datetime import datetime, timezone

KEY=os.environ.get('NEIS_API_KEY','').strip()
BASE='https://open.neis.go.kr/hub/hisTimetable'
BASE_PARAMS={'KEY':KEY,'Type':'json','ATPT_OFCDC_SC_CODE':'J10','SD_SCHUL_CODE':'7530174','AY':str(datetime.now().year),'pSize':'1000'}

def fetch(page, semester):
    q=BASE_PARAMS|{'SEM':str(semester),'pIndex':str(page)}
    url=BASE+'?'+urllib.parse.urlencode(q)
    with urllib.request.urlopen(url,timeout=30) as r:return json.load(r)

def rows_from(payload):
    block=payload.get('hisTimetable',[])
    if len(block)>1:return block[1].get('row',[]) or []
    return []

def fetch_semester(semester):
    rows=[]
    for page in range(1,101):
        batch=rows_from(fetch(page,semester)); rows.extend(batch)
        if len(batch)<1000: break
    print(f'Semester {semester}: {len(rows)} rows')
    return rows

def main():
    if not KEY: raise SystemExit('NEIS_API_KEY secret is required')
    rows=[]
    for semester in (1,2): rows.extend(fetch_semester(semester))
    normalized=[]
    seen=set()
    for r in rows:
        date=r.get('ALL_TI_YMD',''); grade=r.get('GRADE',''); cls=r.get('CLASS_NM',''); period=r.get('PERIO','')
        if not date or not grade or not cls or not period: continue
        item={'date':date,'grade':str(grade),'className':str(cls),'period':str(period),'subject':r.get('ITRT_CNTNT','').strip(),'room':r.get('CLRM_NM','').strip(),'course':r.get('ORD_SC_NM','').strip(),'department':r.get('DDDEP_NM','').strip()}
        key=tuple(item.items())
        if key not in seen: seen.add(key); normalized.append(item)
    normalized.sort(key=lambda x:(x['date'],int(x['grade']) if x['grade'].isdigit() else x['grade'],x['className'],int(x['period']) if x['period'].isdigit() else x['period']))
    out={'rows':normalized,'updatedAt':datetime.now(timezone.utc).strftime('%Y-%m-%d %H:%M UTC'),'source':'NEIS 고등학교시간표','school':{'name':'군포중앙고등학교','officeCode':'J10','schoolCode':'7530174'}}
    os.makedirs('data',exist_ok=True)
    with open('data/timetable.json','w',encoding='utf-8') as f:json.dump(out,f,ensure_ascii=False,indent=2)
    print(f'Fetched {len(normalized)} timetable rows total')

if __name__=='__main__':main()
