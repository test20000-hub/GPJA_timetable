import json
import os
import urllib.parse
import urllib.request
from datetime import datetime, timezone

KEY = os.environ.get('NEIS_API_KEY', '').strip()
BASE = 'https://open.neis.go.kr/hub/schoolSchedule'
OFFICE_CODE = 'J10'
SCHOOL_CODE = '7531272'
EXPECTED_SCHOOL_NAME = '군포중앙고등학교'


def school_year():
    now = datetime.now()
    return now.year if now.month >= 3 else now.year - 1


def fetch(page):
    params = {
        'KEY': KEY,
        'Type': 'json',
        'ATPT_OFCDC_SC_CODE': OFFICE_CODE,
        'SD_SCHUL_CODE': SCHOOL_CODE,
        'AY': str(school_year()),
        'pIndex': str(page),
        'pSize': '1000',
    }
    url = BASE + '?' + urllib.parse.urlencode(params)
    with urllib.request.urlopen(url, timeout=30) as response:
        return json.load(response)


def rows_from(payload):
    block = payload.get('schoolSchedule', [])
    if not isinstance(block, list) or len(block) < 2:
        return []
    return block[1].get('row', []) or []


def check_error(payload, page):
    block = payload.get('schoolSchedule', [])
    if not isinstance(block, list) or not block:
        return
    head = block[0].get('head', []) if isinstance(block[0], dict) else []
    for item in head if isinstance(head, list) else []:
        info = item.get('RESULT') if isinstance(item, dict) else None
        if isinstance(info, dict):
            code = str(info.get('CODE', '')).strip()
            message = str(info.get('MESSAGE', '')).strip()
            if code and code != 'INFO-000':
                raise RuntimeError(f'NEIS API error (page={page}): {code} {message}')


def normalize_date(value):
    value = str(value or '').strip()
    if len(value) == 8 and value.isdigit():
        return f'{value[:4]}-{value[4:6]}-{value[6:8]}'
    if len(value) == 10 and value[4] == '-' and value[7] == '-':
        return value
    return ''


def main():
    if not KEY:
        raise SystemExit('NEIS_API_KEY secret is required')

    rows = []
    for page in range(1, 101):
        payload = fetch(page)
        check_error(payload, page)
        batch = rows_from(payload)
        if batch:
            school_names = {str(r.get('SCHUL_NM', '')).strip() for r in batch if str(r.get('SCHUL_NM', '')).strip()}
            if school_names and school_names != {EXPECTED_SCHOOL_NAME}:
                raise RuntimeError(f'Unexpected school name: {sorted(school_names)}')
            rows.extend(batch)
        if len(batch) < 1000:
            break

    normalized = []
    seen = set()
    for r in rows:
        date = normalize_date(r.get('AA_YMD'))
        event_name = str(r.get('EVENT_NM', '') or '').strip()
        event_content = str(r.get('EVENT_CNTNT', '') or '').strip()
        if not date or not event_name:
            continue
        item = {
            'date': date,
            'event': event_name,
            'content': event_content,
            'grade1': str(r.get('ONE_GRADE_EVENT_YN', '') or '').strip(),
            'grade2': str(r.get('TW_GRADE_EVENT_YN', '') or '').strip(),
            'grade3': str(r.get('THREE_GRADE_EVENT_YN', '') or '').strip(),
            'weekday': str(r.get('DGHT_CRSE_EVENT_YN', '') or '').strip(),
        }
        key = tuple(item.items())
        if key not in seen:
            seen.add(key)
            normalized.append(item)

    normalized.sort(key=lambda x: (x['date'], x['event']))
    if not normalized:
        raise SystemExit('NEIS returned no usable academic schedule rows; refusing to overwrite data/schedule.json')

    dates = sorted({r['date'] for r in normalized})
    out = {
        'rows': normalized,
        'updatedAt': datetime.now(timezone.utc).strftime('%Y-%m-%d %H:%M UTC'),
        'source': 'NEIS 학사일정',
        'schoolYear': school_year(),
        'school': {'name': EXPECTED_SCHOOL_NAME, 'officeCode': OFFICE_CODE, 'schoolCode': SCHOOL_CODE},
        'dateRange': {'from': dates[0], 'to': dates[-1]},
    }

    os.makedirs('data', exist_ok=True)
    temp_path = 'data/schedule.json.tmp'
    with open(temp_path, 'w', encoding='utf-8') as f:
        json.dump(out, f, ensure_ascii=False, indent=2)
    os.replace(temp_path, 'data/schedule.json')

    print(f'Academic year: {school_year()}')
    print(f'Fetched {len(normalized)} academic schedule rows')
    print(f'Date range: {dates[0]} -> {dates[-1]}')


if __name__ == '__main__':
    main()
