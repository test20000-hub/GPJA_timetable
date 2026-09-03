import json
import os
import urllib.parse
import urllib.request
from datetime import datetime, timezone

KEY = os.environ.get('NEIS_API_KEY', '').strip()
BASE = 'https://open.neis.go.kr/hub/hisTimetable'
OFFICE_CODE = 'J10'
SCHOOL_CODE = '7531272'
EXPECTED_SCHOOL_NAME = '군포중앙고등학교'


def school_year():
    # NEIS AY follows the school year. In Korea the school year begins in March,
    # so Jan/Feb still belong to the previous academic year.
    now = datetime.now()
    return now.year if now.month >= 3 else now.year - 1


BASE_PARAMS = {
    'KEY': KEY,
    'Type': 'json',
    'ATPT_OFCDC_SC_CODE': OFFICE_CODE,
    'SD_SCHUL_CODE': SCHOOL_CODE,
    'AY': str(school_year()),
    'pSize': '1000',
}


def fetch(page, semester):
    q = BASE_PARAMS | {'SEM': str(semester), 'pIndex': str(page)}
    url = BASE + '?' + urllib.parse.urlencode(q)
    with urllib.request.urlopen(url, timeout=30) as response:
        return json.load(response)


def rows_from(payload):
    block = payload.get('hisTimetable', [])
    if not isinstance(block, list) or len(block) < 2:
        return []
    return block[1].get('row', []) or []


def check_neis_error(payload, semester, page):
    block = payload.get('hisTimetable', [])
    if not isinstance(block, list) or not block:
        return
    first = block[0] if isinstance(block[0], dict) else {}
    result = first.get('head', [])
    if not isinstance(result, list):
        return
    for item in result:
        if not isinstance(item, dict):
            continue
        result_info = item.get('RESULT')
        if isinstance(result_info, dict):
            code = str(result_info.get('CODE', '')).strip()
            message = str(result_info.get('MESSAGE', '')).strip()
            if code and code != 'INFO-000':
                raise RuntimeError(
                    f'NEIS API error (semester={semester}, page={page}): '
                    f'{code} {message}'
                )


def validate_school(rows, semester, page):
    if not rows:
        return

    school_names = {str(r.get('SCHUL_NM', '')).strip() for r in rows}
    school_names.discard('')
    office_codes = {str(r.get('ATPT_OFCDC_SC_CODE', '')).strip() for r in rows}
    office_codes.discard('')
    school_codes = {str(r.get('SD_SCHUL_CODE', '')).strip() for r in rows}
    school_codes.discard('')

    if school_names and school_names != {EXPECTED_SCHOOL_NAME}:
        raise RuntimeError(
            f'Unexpected NEIS school name (semester={semester}, page={page}): '
            f'{sorted(school_names)}; expected {EXPECTED_SCHOOL_NAME}'
        )
    if office_codes and office_codes != {OFFICE_CODE}:
        raise RuntimeError(
            f'Unexpected NEIS office code (semester={semester}, page={page}): '
            f'{sorted(office_codes)}; expected {OFFICE_CODE}'
        )
    if school_codes and school_codes != {SCHOOL_CODE}:
        raise RuntimeError(
            f'Unexpected NEIS school code (semester={semester}, page={page}): '
            f'{sorted(school_codes)}; expected {SCHOOL_CODE}'
        )

    if not school_names:
        raise RuntimeError(
            f'NEIS timetable response has no SCHUL_NM (semester={semester}, page={page})'
        )

    print(f'Validated NEIS school: {EXPECTED_SCHOOL_NAME} ({OFFICE_CODE}/{SCHOOL_CODE})')


def fetch_semester(semester):
    rows = []
    for page in range(1, 101):
        payload = fetch(page, semester)
        check_neis_error(payload, semester, page)
        batch = rows_from(payload)
        validate_school(batch, semester, page)
        rows.extend(batch)
        if len(batch) < 1000:
            break
    print(f'Semester {semester}: {len(rows)} rows')
    return rows


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
    for semester in (1, 2):
        rows.extend(fetch_semester(semester))

    normalized = []
    seen = set()
    for r in rows:
        date = normalize_date(r.get('ALL_TI_YMD'))
        grade = str(r.get('GRADE', '')).strip()
        cls = str(r.get('CLASS_NM', '')).strip()
        period = str(r.get('PERIO', '')).strip()
        subject = str(r.get('ITRT_CNTNT', '') or '').strip()
        if not date or not grade or not cls or not period:
            continue

        item = {
            'date': date,
            'grade': grade,
            'className': cls,
            'period': period,
            'subject': subject,
            'room': str(r.get('CLRM_NM', '') or '').strip(),
            'course': str(r.get('ORD_SC_NM', '') or '').strip(),
            'department': str(r.get('DDDEP_NM', '') or '').strip(),
        }
        key = tuple(item.items())
        if key not in seen:
            seen.add(key)
            normalized.append(item)

    if not normalized:
        raise SystemExit('NEIS returned no usable timetable rows; refusing to overwrite data/timetable.json')

    normalized.sort(
        key=lambda x: (
            x['date'],
            int(x['grade']) if x['grade'].isdigit() else x['grade'],
            x['className'],
            int(x['period']) if x['period'].isdigit() else x['period'],
        )
    )

    target_rows = [
        r for r in normalized if r['grade'] == '1' and r['className'] == '5'
    ]
    if not target_rows:
        raise SystemExit('No timetable rows found for required default class 1학년 5반')

    dates = sorted({r['date'] for r in normalized})
    out = {
        'rows': normalized,
        'updatedAt': datetime.now(timezone.utc).strftime('%Y-%m-%d %H:%M UTC'),
        'source': 'NEIS 고등학교시간표',
        'schoolYear': school_year(),
        'school': {
            'name': EXPECTED_SCHOOL_NAME,
            'officeCode': OFFICE_CODE,
            'schoolCode': SCHOOL_CODE,
        },
        'dateRange': {'from': dates[0], 'to': dates[-1]},
    }

    os.makedirs('data', exist_ok=True)
    output_path = 'data/timetable.json'
    temp_path = output_path + '.tmp'
    with open(temp_path, 'w', encoding='utf-8') as f:
        json.dump(out, f, ensure_ascii=False, indent=2)
    os.replace(temp_path, output_path)

    print(f'Academic year: {school_year()}')
    print(f'Fetched {len(normalized)} timetable rows total')
    print(f'Date range: {dates[0]} -> {dates[-1]}')
    print(f'1학년 5반 rows: {len(target_rows)}')


if __name__ == '__main__':
    main()
