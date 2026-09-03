import json
import os
import re
import urllib.parse
import urllib.request
from collections import Counter
from datetime import datetime, timezone, timedelta

KEY = os.environ.get('NEIS_API_KEY', '').strip()
BASE = 'https://open.neis.go.kr/hub/hisTimetable'
OFFICE_CODE = 'J10'
SCHOOL_CODE = '7531272'
EXPECTED_SCHOOL_NAME = '군포중앙고등학교'

TEACHER_BY_SUBJECT = {
    '국어': '김미경', '정보': '김기현', '영어': '여국화', '수학': '송희영',
    '과학': '편문희', '진로': '김희정', '체육': '채승희', '사회': '김수진',
    '한국사': '서지연', '과학탐구실험': '손지영',
}


def school_year():
    now = datetime.now()
    return now.year if now.month >= 3 else now.year - 1

BASE_PARAMS = {
    'KEY': KEY, 'Type': 'json', 'ATPT_OFCDC_SC_CODE': OFFICE_CODE,
    'SD_SCHUL_CODE': SCHOOL_CODE, 'AY': str(school_year()), 'pSize': '1000',
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
    result = block[0].get('head', []) if isinstance(block[0], dict) else []
    if not isinstance(result, list):
        return
    for item in result:
        if not isinstance(item, dict):
            continue
        info = item.get('RESULT')
        if isinstance(info, dict):
            code = str(info.get('CODE', '')).strip()
            message = str(info.get('MESSAGE', '')).strip()
            if code and code != 'INFO-000':
                raise RuntimeError(f'NEIS API error (semester={semester}, page={page}): {code} {message}')


def validate_school(rows, semester, page):
    if not rows:
        return
    school_names = {str(r.get('SCHUL_NM', '')).strip() for r in rows if str(r.get('SCHUL_NM', '')).strip()}
    office_codes = {str(r.get('ATPT_OFCDC_SC_CODE', '')).strip() for r in rows if str(r.get('ATPT_OFCDC_SC_CODE', '')).strip()}
    school_codes = {str(r.get('SD_SCHUL_CODE', '')).strip() for r in rows if str(r.get('SD_SCHUL_CODE', '')).strip()}
    if school_names and school_names != {EXPECTED_SCHOOL_NAME}:
        raise RuntimeError(f'Unexpected NEIS school name (semester={semester}, page={page}): {sorted(school_names)}; expected {EXPECTED_SCHOOL_NAME}')
    if office_codes and office_codes != {OFFICE_CODE}:
        raise RuntimeError(f'Unexpected NEIS office code (semester={semester}, page={page}): {sorted(office_codes)}; expected {OFFICE_CODE}')
    if school_codes and school_codes != {SCHOOL_CODE}:
        raise RuntimeError(f'Unexpected NEIS school code (semester={semester}, page={page}): {sorted(school_codes)}; expected {SCHOOL_CODE}')
    if not school_names:
        raise RuntimeError(f'NEIS timetable response has no SCHUL_NM (semester={semester}, page={page})')


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


def normalize_subject(subject):
    value = re.sub(r'\s+', '', str(subject or '').strip())
    if not value:
        return ''
    if value.startswith('과학탐구실험'):
        return '과학탐구실험'
    value = re.sub(r'^(공통|통합)', '', value)
    value = re.sub(r'[12]+$', '', value)
    return value


def teacher_name(row, subject):
    for key in ('TCHR_NM', 'TEACHER_NM', 'TEACH_NM', 'TEACHER', 'TCHR'):
        value = str(row.get(key, '') or '').strip()
        if value:
            return value
    return TEACHER_BY_SUBJECT.get(normalize_subject(subject), '')


def load_previous(path):
    try:
        with open(path, 'r', encoding='utf-8') as f:
            previous = json.load(f)
        rows = previous.get('rows', [])
        return rows if isinstance(rows, list) else []
    except (FileNotFoundError, json.JSONDecodeError, OSError):
        return []


def timetable_key(row):
    return (str(row.get('date', '')), str(row.get('grade', '')), str(row.get('className', '')), str(row.get('period', '')))


def slot_key(row):
    try:
        weekday = datetime.strptime(str(row['date']), '%Y-%m-%d').weekday()
    except (KeyError, ValueError):
        weekday = -1
    return (weekday, str(row.get('grade', '')), str(row.get('className', '')), str(row.get('period', '')))


def signature(row):
    return (
        str(row.get('subject') or '').strip(),
        str(row.get('room') or '').strip(),
        str(row.get('teacher') or '').strip(),
    )


def build_weekly_baseline(normalized):
    groups = {}
    for row in normalized:
        groups.setdefault(slot_key(row), []).append(row)

    baseline = {}
    for key, rows in groups.items():
        by_date = {str(r.get('date')): r for r in rows}
        for row in rows:
            try:
                current_date = datetime.strptime(str(row['date']), '%Y-%m-%d')
            except (KeyError, ValueError):
                continue
            neighbors = []
            for weeks in (1, 2, 3):
                for delta in (-7 * weeks, 7 * weeks):
                    neighbor_date = (current_date + timedelta(days=delta)).strftime('%Y-%m-%d')
                    neighbor = by_date.get(neighbor_date)
                    if neighbor:
                        neighbors.append(neighbor)
            counts = Counter(signature(r) for r in neighbors)
            if not counts:
                continue
            best_sig, best_count = counts.most_common(1)[0]
            if best_count >= 2 and signature(row) != best_sig:
                baseline[timetable_key(row)] = best_sig
    return baseline


def add_change_metadata(normalized, previous_rows):
    baseline = build_weekly_baseline(normalized)
    changed = 0

    for row in normalized:
        key = timetable_key(row)
        old = baseline.get(key)

        if old is None:
            for previous in previous_rows:
                if timetable_key(previous) != key:
                    continue
                previous_change = previous.get('change') or {}
                if previous_change.get('previousSubject'):
                    old = (
                        str(previous_change.get('previousSubject') or '').strip(),
                        str(previous_change.get('previousRoom') or '').strip(),
                        str(previous_change.get('previousTeacher') or '').strip(),
                    )
                break

        if old is None:
            continue

        old_subject, old_room, old_teacher = old
        new_subject = str(row.get('subject') or '').strip()
        new_room = str(row.get('room') or '').strip()
        new_teacher = str(row.get('teacher') or '').strip()
        subject_changed = old_subject != new_subject
        room_changed = old_room != new_room
        teacher_changed = bool(old_teacher and new_teacher and old_teacher != new_teacher)

        if not (subject_changed or room_changed or teacher_changed):
            continue

        change_type = 'subject' if subject_changed else ('teacher' if teacher_changed else 'room')
        row['change'] = {
            'type': change_type,
            'previousSubject': old_subject,
            'previousRoom': old_room,
            'previousTeacher': old_teacher,
        }
        changed += 1

    return changed


def main():
    if not KEY:
        raise SystemExit('NEIS_API_KEY secret is required')

    output_path = 'data/timetable.json'
    previous_rows = load_previous(output_path)

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
            'teacher': teacher_name(r, subject),
            'course': str(r.get('ORD_SC_NM', '') or '').strip(),
            'department': str(r.get('DDDEP_NM', '') or '').strip(),
        }
        key = tuple(item.items())
        if key not in seen:
            seen.add(key)
            normalized.append(item)

    if not normalized:
        raise SystemExit('NEIS returned no usable timetable rows; refusing to overwrite data/timetable.json')

    normalized.sort(key=lambda x: (
        x['date'],
        int(x['grade']) if x['grade'].isdigit() else x['grade'],
        x['className'],
        int(x['period']) if x['period'].isdigit() else x['period'],
    ))
    changed_count = add_change_metadata(normalized, previous_rows)

    dates = sorted({r['date'] for r in normalized})
    out = {
        'rows': normalized,
        'updatedAt': datetime.now(timezone.utc).strftime('%Y-%m-%d %H:%M UTC'),
        'source': 'NEIS 고등학교시간표 + 지정 교사 매핑',
        'schoolYear': school_year(),
        'school': {
            'name': EXPECTED_SCHOOL_NAME,
            'officeCode': OFFICE_CODE,
            'schoolCode': SCHOOL_CODE,
        },
        'teacherSource': '사용자 지정 과목별 교사 매핑(컴시간 연동 전 임시 적용)',
        'dateRange': {'from': dates[0], 'to': dates[-1]},
        'changeCount': changed_count,
    }

    os.makedirs('data', exist_ok=True)
    temp_path = output_path + '.tmp'
    with open(temp_path, 'w', encoding='utf-8') as f:
        json.dump(out, f, ensure_ascii=False, indent=2)
    os.replace(temp_path, output_path)

    print(f'Academic year: {school_year()}')
    print(f'Fetched {len(normalized)} timetable rows')
    print(f'Change metadata: {changed_count} rows')
    print(f'Date range: {dates[0]} .. {dates[-1]}')


if __name__ == '__main__':
    main()
