# 군포중앙고등학교 시간표

NEIS 고등학교시간표를 이용하는 군포중앙고 전용 모바일 시간표/PWA입니다.

## 구성

- GitHub Pages 정적 웹앱
- GitHub Actions가 NEIS API에서 시간표를 주기적으로 수집
- NEIS API 키는 `NEIS_API_KEY` GitHub Actions Secret으로만 사용
- 학년/반 선택
- 주간 시간표 및 오늘 시간표
- PWA 설치 및 마지막 데이터 오프라인 캐시

학교 정보: 시도교육청 코드 `J10`, 학교 코드 `7530174`.

## 최초 설정

1. 저장소의 **Settings → Secrets and variables → Actions**에서 `NEIS_API_KEY`라는 Repository secret을 만들고 NEIS 인증키를 저장합니다.
2. **Settings → Pages**에서 Source를 **GitHub Actions**로 설정합니다.
3. **Actions → Update timetable and deploy Pages → Run workflow**를 실행합니다.

NEIS 공식 개발자 가이드에 따라 Open API 인증키가 필요합니다. 고등학교시간표 데이터셋은 매일 적재되며 2025학년도 이후 데이터는 기존 방식으로 사용할 수 있습니다.
